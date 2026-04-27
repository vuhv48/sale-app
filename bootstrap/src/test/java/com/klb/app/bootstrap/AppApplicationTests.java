package com.klb.app.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestCacheConfig.class)
@ActiveProfiles("test")
class AppApplicationTests {

	@Test
	void contextLoads() {
	}
}
