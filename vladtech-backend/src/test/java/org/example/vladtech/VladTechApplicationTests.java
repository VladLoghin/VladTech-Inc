package org.example.vladtech;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Disabled("Conflicts with OAuth2/MongoDB config in CI - context is validated by other integration tests")
class VladTechApplicationTests {

	@Test
	void contextLoads() {
	}

}
