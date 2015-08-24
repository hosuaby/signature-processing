package io.hosuaby.signatures;

import java.util.HashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);

        /* Disable automatic job run on startup */
        app.setDefaultProperties(new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("spring.batch.job.enabled", new Boolean(false));
            }
        });

        app.run(args);
    }

}
