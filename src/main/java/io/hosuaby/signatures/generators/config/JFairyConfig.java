package io.hosuaby.signatures.generators.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.codearte.jfairy.Fairy;

/**
 * Beans of JFairy (fake identity generator).
 */
@Configuration
public class JFairyConfig {

    /**
     * @return fairy object
     */
    @Bean
    public Fairy fairy() {
        // TODO: add some useful params
        return Fairy.create();
    }

}
