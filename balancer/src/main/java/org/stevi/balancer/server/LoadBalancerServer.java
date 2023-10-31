package org.stevi.balancer.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class LoadBalancerServer {

    private final Handler handler = new Handler();
    private final ServerSocket serverSocket;
    private volatile boolean isStarted = true;

    @SneakyThrows
    public LoadBalancerServer(int port) {
        this.serverSocket = new ServerSocket(port);
    }

    @SneakyThrows
    public void start() {
        log.info("Starting server on port {}", serverSocket.getLocalPort());
        while (isStarted) {
            Socket socket = this.serverSocket.accept();
            handleSocketConnection(socket);
        }
    }

    private void handleSocketConnection(Socket clientSocket) {
        Runnable runnable = () -> {
            try (clientSocket) {
                handler.handle(clientSocket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Thread.ofVirtual().start(runnable);
    }

    public void stop() {
        isStarted = false;
    }

}
