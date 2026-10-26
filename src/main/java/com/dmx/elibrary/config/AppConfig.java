package com.dmx.elibrary.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** Injectable clock so services stay testable with frozen time. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
