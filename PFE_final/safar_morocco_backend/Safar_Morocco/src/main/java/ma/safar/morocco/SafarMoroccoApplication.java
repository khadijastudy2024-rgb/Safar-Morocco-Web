package ma.safar.morocco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SafarMoroccoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SafarMoroccoApplication.class, args);
    }
}