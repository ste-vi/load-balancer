package org.stevi.balancer.http.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RegisteredService {

    private int activeConnection;
    private int port;
    private String address;

    public synchronized void incrementActiveConnection() {
        activeConnection = activeConnection + 1;
    }

    public synchronized void decrementActiveConnection() {
        if (activeConnection > 0) {
            activeConnection = activeConnection - 1;
        }
    }
}