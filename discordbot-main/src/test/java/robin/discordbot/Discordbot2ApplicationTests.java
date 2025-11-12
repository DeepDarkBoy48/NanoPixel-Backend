package robin.discordbot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Discordbot2ApplicationTests {
	@Value("${openai.api.key}")
	private String openaiApiKey;

	@Test
	void contextLoads() {
		System.out.println(openaiApiKey);
	}

}
