package org.stevi.balancer.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.stevi.balancer.enumeration.HttpMethod;
import org.stevi.balancer.enumeration.HttpStatusCode;
import org.stevi.balancer.enumeration.HttpVersion;
import org.stevi.balancer.model.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class LoadBalancerServer {

    private final ServerSocket serverSocket;

    private volatile boolean isStarted = true;

    private final HttpRequestReader httpRequestReader = new HttpRequestReader();

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

    private void handleSocketConnection(Socket clientSocket) throws IOException {
        try (clientSocket) {
            Runnable runnable = () -> {
                try {
                    InputStream clientInputStream = clientSocket.getInputStream();
                    OutputStream clientOutputStream = clientSocket.getOutputStream();

                    HttpRequest httpRequest = httpRequestReader.decodeRequest(clientInputStream).orElseThrow();

                    if (HttpMethod.POST.equals(httpRequest.getHttpMethod())
                            && "discover".equals(httpRequest.getUri().getPath())) {

                        clientSocket.getInetAddress();

                        String responseHeaderInfo = "%s %s %s\r\n".formatted(HttpVersion.HTTP_1_1.getValue(), HttpStatusCode.OK.getCode(), HttpStatusCode.OK.name());

                        clientOutputStream.write(responseHeaderInfo.getBytes());
                        clientOutputStream.write("Connection: Close\r\n".getBytes());
                        clientOutputStream.write("\r\n".getBytes());

                    } else {
                        Socket destinationSocket = new Socket("127.0.0.1", 8080);
                        InputStream destinationInputStream = destinationSocket.getInputStream();
                        OutputStream destinationOutputStream = destinationSocket.getOutputStream();

                        String requestHeaderInfo = "%s %s %s\r\n".formatted(httpRequest.getHttpMethod(), httpRequest.getUri().getPath(), HttpVersion.HTTP_1_1.getValue());
                        destinationOutputStream.write(requestHeaderInfo.getBytes());
                        destinationOutputStream.write("Accept: */*\r\n".getBytes());
                        destinationOutputStream.write("Host: localhost:8000\r\n".getBytes());
                        destinationOutputStream.write("Connection: Close\r\n".getBytes());
                        destinationOutputStream.write("\r\n".getBytes());

                        //httpRequest.getBodyInputStream().transferTo(destinationOutputStream);
                        destinationOutputStream.flush();

                        int bytesRead;
                        byte[] buffer = new byte[1024];

                        while ((bytesRead = destinationInputStream.read(buffer)) != -1) {
                            clientOutputStream.write(buffer, 0, bytesRead);
                        }
                        clientOutputStream.flush();

                        clientInputStream.close();
                        clientOutputStream.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            Thread.ofVirtual().start(runnable);
        }
    }

    public void stop() {
        isStarted = false;
    }


}
