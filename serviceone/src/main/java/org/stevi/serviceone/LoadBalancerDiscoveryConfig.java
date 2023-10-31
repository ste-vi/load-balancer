package org.stevi.serviceone;

import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
/*import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;*/
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@EnableScheduling
//@EnableRetry
@Configuration
public class LoadBalancerDiscoveryConfig {

    @SneakyThrows
   // @Retryable
    @EventListener(ApplicationReadyEvent.class)
    public void pingLoadBalancerServer() {
        restTemplate().postForLocation(new URI("http://localhost:8000/discover/books"), "");
    }

    @Scheduled(fixedRate = 30000)
    public void schedulePingLoadBalancer() {
        pingLoadBalancerServer();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
