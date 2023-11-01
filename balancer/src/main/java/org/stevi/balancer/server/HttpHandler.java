package org.stevi.balancer.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.stevi.balancer.http.enumeration.HttpStatusCode;
import org.stevi.balancer.http.enumeration.HttpVersion;
import org.stevi.balancer.http.exceptions.HttpRequestException;
import org.stevi.balancer.http.model.HttpRequest;
import org.stevi.balancer.http.model.RegisteredService;
import org.stevi.balancer.http.util.HttpRequestReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class HttpHandler implements Handler {

    private final HttpRequestReader httpRequestReader;

    public HttpHandler() {
        httpRequestReader = new HttpRequestReader();
    }

    public void handle(Socket clientSocket) throws IOException {
        InputStream clientInputStream = clientSocket.getInputStream();
        OutputStream clientOutputStream = clientSocket.getOutputStream();

        try {
            HttpRequest httpRequest = parseHttpRequest(clientInputStream);
            String requestPath = httpRequest.getUri().getPath();

            if (requestPath.contains("/register")) {
                handleServiceRegistry(httpRequest, requestPath, clientOutputStream);
            } else {
                handleServiceBalancing(requestPath, httpRequest, clientOutputStream);
            }
        } catch (HttpRequestException exception) {
            handleHttpRequestException(exception, clientOutputStream);
        }
    }

    private HttpRequest parseHttpRequest(InputStream clientInputStream) {
        return httpRequestReader
                .decodeRequest(clientInputStream)
                .orElseThrow(() -> new HttpRequestException(HttpStatusCode.OK, ""));
    }

    private void handleServiceRegistry(HttpRequest httpRequest,
                                       String requestPath,
                                       OutputStream clientOutputStream) throws IOException {
        switch (httpRequest.getHttpMethod()) {
            case POST -> {
                ServiceRegistry.registerService(requestPath, 8080, "127.0.0.1");
                acknowledgeDiscoveryRequest(clientOutputStream);
            }
            case DELETE -> ServiceRegistry.unregisterService(requestPath, 8080, "127.0.0.1");
        }
    }

    private void acknowledgeDiscoveryRequest(OutputStream clientOutputStream) throws IOException {
        String responseHeaderInfo = "%s %s %s\r\n".formatted(HttpVersion.HTTP_1_1.getValue(), HttpStatusCode.OK.getCode(), HttpStatusCode.OK.name());

        clientOutputStream.write(responseHeaderInfo.getBytes());
        clientOutputStream.write("Connection: Close\r\n".getBytes());
        clientOutputStream.write("\r\n".getBytes());
    }

    private void handleServiceBalancing(String requestPath, HttpRequest httpRequest, OutputStream clientOutputStream) {
        log.info("Incoming request {}", requestPath);
        RegisteredService availableService = ServiceRegistry.findAvailableService(requestPath)
                .orElseThrow(() -> new HttpRequestException(HttpStatusCode.NOT_FOUND, "Path %s not found".formatted(requestPath)));
        log.info("Balanced service: {}", availableService.toString());

        availableService.incrementActiveConnection();
        forwardHttpRequestToService(httpRequest, clientOutputStream, availableService);
        availableService.decrementActiveConnection();
    }

    @SneakyThrows
    private void forwardHttpRequestToService(HttpRequest httpRequest,
                                             OutputStream clientOutputStream,
                                             RegisteredService availableService) {
        Socket destinationSocket = new Socket(availableService.getAddress(), availableService.getPort());
        InputStream destinationInputStream = destinationSocket.getInputStream();
        OutputStream destinationOutputStream = destinationSocket.getOutputStream();

        forwardRequest(httpRequest, destinationOutputStream);
        forwardResponse(clientOutputStream, destinationInputStream);

        destinationSocket.close();
    }

    private void forwardRequest(HttpRequest httpRequest, OutputStream destinationOutputStream) throws IOException {
        String requestHeaderInfo = "%s %s %s\r\n".formatted(httpRequest.getHttpMethod(), httpRequest.getUri().getPath(), HttpVersion.HTTP_1_1.getValue());
        destinationOutputStream.write(requestHeaderInfo.getBytes());
        destinationOutputStream.write("Accept: */*\r\n".getBytes());
        destinationOutputStream.write("Host: localhost:8000\r\n".getBytes());
        destinationOutputStream.write("Connection: Close\r\n".getBytes());
        destinationOutputStream.write("\r\n".getBytes());

        //httpRequest.getBodyInputStream().transferTo(destinationOutputStream);
        destinationOutputStream.flush();
    }

    private void forwardResponse(OutputStream clientOutputStream, InputStream destinationInputStream) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[1024];

        while ((bytesRead = destinationInputStream.read(buffer)) != -1) {
            clientOutputStream.write(buffer, 0, bytesRead);
        }
        clientOutputStream.flush();
    }

    @SneakyThrows
    private void handleHttpRequestException(HttpRequestException exception, OutputStream clientOutputStream) {
        String responseHeaderInfo = "%s %s %s\r\n".formatted(HttpVersion.HTTP_1_1.getValue(), exception.getStatusCode().getCode(), exception.getStatusCode().name());

        clientOutputStream.write(responseHeaderInfo.getBytes());
        clientOutputStream.write("Content-Type: application/json\r\n".getBytes());
        clientOutputStream.write("Connection: Close\r\n".getBytes());
        clientOutputStream.write("\r\n".getBytes());
        clientOutputStream.write(exception.getErrorMessage().toString().getBytes());
    }

}
