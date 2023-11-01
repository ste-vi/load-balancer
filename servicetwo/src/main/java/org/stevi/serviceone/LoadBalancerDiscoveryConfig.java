package org.stevi.serviceone;

import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@EnableScheduling
@Configuration
public class LoadBalancerDiscoveryConfig {

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public void registerSelfInLoadBalancer() {
        restTemplate().postForLocation(new URI("http://localhost:8000/register/authors"), "");
    }

    @Scheduled(fixedRate = 90000)
    public void schedulePingLoadBalancer() {
        registerSelfInLoadBalancer();
    }

    @SneakyThrows
    @PreDestroy
    public void unregisterSelfInLoadBalancer() {
        RestTemplate template = new RestTemplate();
        template.delete(new URI("http://localhost:8000/register/authors"));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
