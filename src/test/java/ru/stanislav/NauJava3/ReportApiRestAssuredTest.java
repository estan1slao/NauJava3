package ru.stanislav.NauJava3;

import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.stanislav.NauJava3.entity.AppUser;
import ru.stanislav.NauJava3.entity.Report;
import ru.stanislav.NauJava3.entity.ReportStatus;
import ru.stanislav.NauJava3.repository.AppUserRepository;
import ru.stanislav.NauJava3.repository.ReportRepository;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReportApiRestAssuredTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private SessionFilter session;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 10);
        String username = "api_" + suffix;
        String password = "TestPassword123";
        testUser = appUserRepository.save(new AppUser(
                username,
                username + "@example.com",
                passwordEncoder.encode(password),
                "API Test User"
        ));

        session = new SessionFilter();
        String loginPage = given()
                .port(port)
                .filter(session)
                .when()
                .get("/login")
                .then()
                .statusCode(200)
                .extract().asString();

        String csrf = csrfToken(loginPage);

        given()
                .port(port)
                .filter(session)
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("username", username)
                .formParam("password", password)
                .formParam("_csrf", csrf)
                .when()
                .post("/login")
                .then()
                .statusCode(302)
                .header("Location", containsString("/transactions-page"));
    }

    @AfterEach
    void tearDown() {
        reportRepository.deleteAll();
        if (testUser != null) {
            appUserRepository.deleteById(testUser.getId());
        }
    }

    @Test
    void getCompletedReportReturnsHtml() {
        Report report = reportRepository.save(new Report(ReportStatus.COMPLETED, "<h1>Report ready</h1>"));

        given()
                .port(port)
                .filter(session)
                .when()
                .get("/api/reports/{id}", report.getId())
                .then()
                .statusCode(200)
                .header("Content-Type", startsWith("text/html"))
                .body(containsString("Report ready"));
    }

    @Test
    void getCreatedReportReturnsAccepted() {
        Report report = reportRepository.save(new Report(ReportStatus.CREATED, null));

        given()
                .port(port)
                .filter(session)
                .when()
                .get("/api/reports/{id}", report.getId())
                .then()
                .statusCode(202)
                .header("Content-Type", startsWith("text/plain"));
    }

    @Test
    void getFailedReportReturnsServerError() {
        Report report = reportRepository.save(new Report(ReportStatus.ERROR, "database-error"));

        given()
                .port(port)
                .filter(session)
                .when()
                .get("/api/reports/{id}", report.getId())
                .then()
                .statusCode(500)
                .body(containsString("database-error"));
    }

    @Test
    void getMissingReportReturnsNotFound() {
        given()
                .port(port)
                .filter(session)
                .when()
                .get("/api/reports/{id}", Long.MAX_VALUE)
                .then()
                .statusCode(404)
                .header("Content-Type", startsWith("text/plain"));
    }

    @Test
    void anonymousRequestIsRedirectedToLogin() {
        given()
                .port(port)
                .redirects().follow(false)
                .when()
                .get("/api/reports/{id}", 1L)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    void createReportReturnsIdAndFinishesInDatabase() throws InterruptedException {
        String reportsPage = given()
                .port(port)
                .filter(session)
                .when()
                .get("/reports")
                .then()
                .statusCode(200)
                .extract().asString();

        String csrf = csrfToken(reportsPage);
        String csrfHeader = csrfHeader(reportsPage);

        Response response = given()
                .port(port)
                .filter(session)
                .header(csrfHeader, csrf)
                .when()
                .post("/api/reports")
                .then()
                .statusCode(202)
                .contentType(ContentType.JSON)
                .extract().response();

        Long reportId = response.jsonPath().getLong("reportId");
        assertNotNull(reportId);
        assertTrue(reportId > 0);

        Report report = null;
        for (int attempt = 0; attempt < 100; attempt++) {
            report = reportRepository.findById(reportId).orElseThrow();
            if (report.getStatus() != ReportStatus.CREATED) {
                break;
            }
            Thread.sleep(100);
        }

        assertNotNull(report);
        assertEquals(ReportStatus.COMPLETED, report.getStatus());
        assertNotNull(report.getContent());
        assertTrue(report.getContent().contains("<html"));
    }

    private static String csrfToken(String html) {
        return attributeFromElement(html, "_csrf", "(?:value|content)");
    }

    private static String csrfHeader(String html) {
        return attributeFromElement(html, "_csrf_header", "content");
    }

    private static String attributeFromElement(String html, String name, String attribute) {
        Pattern elementPattern = Pattern.compile(
                "<[^>]+\\bname=[\"']" + Pattern.quote(name) + "[\"'][^>]*>",
                Pattern.CASE_INSENSITIVE
        );
        Matcher element = elementPattern.matcher(html);
        assertTrue(element.find(), "Не найден элемент CSRF: " + name);
        Pattern attributePattern = Pattern.compile(
                "\\b" + attribute + "=[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE
        );
        Matcher value = attributePattern.matcher(element.group());
        assertTrue(value.find(), "Не найден атрибут CSRF: " + attribute);
        return value.group(1);
    }
}
