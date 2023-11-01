package org.stevi.balancer;

import org.stevi.balancer.server.LoadBalancerServer;

public class Main {

    public static void main(String[] args) {
        LoadBalancerServer server = new LoadBalancerServer(8000);
        server.start();
    }

}
