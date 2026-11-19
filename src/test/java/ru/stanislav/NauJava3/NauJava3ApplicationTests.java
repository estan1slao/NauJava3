package ru.stanislav.NauJava3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.test-mode=true")
class NauJava3ApplicationTests {

	@Test
	void contextLoads() {
	}
}
