package org.stevi.balancer;

import lombok.SneakyThrows;
import org.stevi.balancer.server.LoadBalancerServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    public static void main(String[] args) {

        Map<String, String> pathToServerUrlMap = new ConcurrentHashMap<>();
        pathToServerUrlMap.put("/api", "http://locahost:8080");

        // 1. listen to services to register itself
        // some post endpoint with prefix in which we will initialize into the map
        // allow more than one url to be with the same prefix (means replica)
        // 2. get incoming request
        // should I keep request open here? does response also returns via balancer
        // read path
        // get number of active connections of each replica and choose the lowest
        // forward request to the one and increase counter of active conns.
        // 3. add fallback for miss request

        LoadBalancerServer server = new LoadBalancerServer(8000);
        server.start();
    }

}
