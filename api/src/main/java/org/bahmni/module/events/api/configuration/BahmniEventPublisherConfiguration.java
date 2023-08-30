package org.bahmni.module.events.api.configuration;

import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BahmniEventPublisherConfiguration {

    @Bean
    public BahmniEventPublisher bahmniEventPublisher() {
        return new BahmniEventPublisher();
    }
}