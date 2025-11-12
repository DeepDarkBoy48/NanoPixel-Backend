package robin.discordbot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class main {
	public static void main(String[] args) {
		SpringApplication.run(main.class, args);
	}
}

