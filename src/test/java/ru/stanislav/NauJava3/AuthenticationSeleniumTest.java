package ru.stanislav.NauJava3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.stanislav.NauJava3.entity.AppUser;
import ru.stanislav.NauJava3.repository.AppUserRepository;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "runSeleniumTests", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationSeleniumTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebDriver driver;
    private WebDriverWait wait;
    private AppUser testUser;
    private String username;
    private final String password = "TestPassword123";

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 10);
        username = "selenium_" + suffix;
        testUser = appUserRepository.save(new AppUser(
                username,
                username + "@example.com",
                passwordEncoder.encode(password),
                "Selenium Test User"
        ));

        String browser = System.getProperty("selenium.browser", "chrome");
        if (browser.equalsIgnoreCase("edge")) {
            EdgeOptions options = new EdgeOptions();
            options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new EdgeDriver(options);
        } else if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
        } else {
            throw new IllegalArgumentException("Unsupported Selenium browser: " + browser);
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (testUser != null) {
            appUserRepository.deleteById(testUser.getId());
        }
    }

    @Test
    void validCredentialsOpenTransactionsPage() {
        login();

        assertTrue(driver.getCurrentUrl().contains("/transactions-page"));
        assertEquals("Финансовые транзакции", driver.findElement(By.tagName("h1")).getText());
        assertTrue(driver.findElement(By.cssSelector("form[action='/logout'] button")).isDisplayed());
    }

    @Test
    void logoutRedirectsToLoginAndProtectsPrivatePage() {
        login();

        driver.findElement(By.cssSelector("form[action='/logout'] button")).click();
        wait.until(ExpectedConditions.urlContains("/login"));

        assertEquals("Вход в систему", driver.findElement(By.tagName("h1")).getText());
        assertTrue(driver.getCurrentUrl().contains("logout"));

        driver.get("http://localhost:" + port + "/transactions-page");
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    void invalidCredentialsShowError() {
        driver.get("http://localhost:" + port + "/login");
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys("incorrectPassword");
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/login?error"));
        assertTrue(driver.findElement(By.className("error")).getText().contains("Неверный логин"));
    }

    private void login() {
        driver.get("http://localhost:" + port + "/login");
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("form button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/transactions-page"));
    }
}
