package io.hosuaby.signatures.generators.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for producers of signatures.
 */
@Configuration
@EnableScheduling
public class ProducersConfig {

    /** Rate of production */
    public static final long PRODUCTION_RATE = 5000;

    /** Base directory for signatures lists */
    public static final String BASE_DIR = "/tmp/signatures/inbox/";

}
