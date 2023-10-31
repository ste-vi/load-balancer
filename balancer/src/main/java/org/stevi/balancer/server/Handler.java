package org.stevi.balancer.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.stevi.balancer.enumeration.HttpMethod;
import org.stevi.balancer.enumeration.HttpStatusCode;
import org.stevi.balancer.enumeration.HttpVersion;
import org.stevi.balancer.model.DiscoveredService;
import org.stevi.balancer.model.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class Handler {

    private final HttpRequestReader httpRequestReader = new HttpRequestReader();

    private final Map<String, Set<DiscoveredService>> pathToDiscoveredServices = new ConcurrentHashMap<>();

    @SneakyThrows
    public void handle(Socket clientSocket) {
        InputStream clientInputStream = clientSocket.getInputStream();
        OutputStream clientOutputStream = clientSocket.getOutputStream();

        HttpRequest httpRequest = httpRequestReader.decodeRequest(clientInputStream).orElseThrow();
        String requestPath = httpRequest.getUri().getPath();

        if (HttpMethod.POST.equals(httpRequest.getHttpMethod()) && requestPath.contains("/discover")) {
            discoverNewService(clientSocket, requestPath);
            acknowledgeDiscoveryRequest(clientOutputStream);
        } else {
            log.info("New incoming request {}", requestPath);
            DiscoveredService availableService = findAvailableService(requestPath);
            log.info("Balanced service: {}", availableService.toString());
            availableService.incrementActiveConnection();
            forwardHttpRequestToService(httpRequest, clientOutputStream, availableService);
            availableService.decrementActiveConnection();
        }
    }

    private void discoverNewService(Socket clientSocket, String requestPath) {
        String path = requestPath.split("/discover")[1];

        int port = clientSocket.getPort();
        String address = clientSocket.getInetAddress().getHostAddress();

        Set<DiscoveredService> discoveredServices = pathToDiscoveredServices.get(path);
        if (discoveredServices == null) {
            var newService = new DiscoveredService(1, 8080, address);

            discoveredServices = new HashSet<>();
            discoveredServices.add(newService);

            pathToDiscoveredServices.put(path, discoveredServices);
        }

        log.info("Discovered new service with path {} and port {}", path, port);
    }

    private void acknowledgeDiscoveryRequest(OutputStream clientOutputStream) throws IOException {
        String responseHeaderInfo = "%s %s %s\r\n".formatted(HttpVersion.HTTP_1_1.getValue(), HttpStatusCode.OK.getCode(), HttpStatusCode.OK.name());

        clientOutputStream.write(responseHeaderInfo.getBytes());
        clientOutputStream.write("Connection: Close\r\n".getBytes());
        clientOutputStream.write("\r\n".getBytes());
    }

    private DiscoveredService findAvailableService(String requestPath) {
        Set<DiscoveredService> discoveredServices = pathToDiscoveredServices.get(requestPath);
        if (discoveredServices == null) {
            throw new RuntimeException("Path not found");
        }

        return discoveredServices.stream()
                .min(Comparator.comparingInt(DiscoveredService::getActiveConnection))
                .orElseThrow();
    }

    @SneakyThrows
    private void forwardHttpRequestToService(HttpRequest httpRequest, OutputStream clientOutputStream, DiscoveredService availableService) {
        Socket destinationSocket = new Socket(availableService.getAddress(), availableService.getPort());
        InputStream destinationInputStream = destinationSocket.getInputStream();
        OutputStream destinationOutputStream = destinationSocket.getOutputStream();

        processRequest(httpRequest, destinationOutputStream);
        processResponse(clientOutputStream, destinationInputStream);

        destinationSocket.close();
    }

    private static void processRequest(HttpRequest httpRequest, OutputStream destinationOutputStream) throws IOException {
        String requestHeaderInfo = "%s %s %s\r\n".formatted(httpRequest.getHttpMethod(), httpRequest.getUri().getPath(), HttpVersion.HTTP_1_1.getValue());
        destinationOutputStream.write(requestHeaderInfo.getBytes());
        destinationOutputStream.write("Accept: */*\r\n".getBytes());
        destinationOutputStream.write("Host: localhost:8000\r\n".getBytes());
        destinationOutputStream.write("Connection: Close\r\n".getBytes());
        destinationOutputStream.write("\r\n".getBytes());

        //httpRequest.getBodyInputStream().transferTo(destinationOutputStream);
        destinationOutputStream.flush();
    }

    private static void processResponse(OutputStream clientOutputStream, InputStream destinationInputStream) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[1024];

        while ((bytesRead = destinationInputStream.read(buffer)) != -1) {
            clientOutputStream.write(buffer, 0, bytesRead);
        }
        clientOutputStream.flush();
    }

}
