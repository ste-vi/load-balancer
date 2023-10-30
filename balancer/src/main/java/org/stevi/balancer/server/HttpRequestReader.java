package org.stevi.balancer.server;

import lombok.SneakyThrows;
import org.stevi.balancer.enumeration.HttpMethod;
import org.stevi.balancer.enumeration.HttpVersion;
import org.stevi.balancer.model.HttpRequest;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpRequestReader {

    @SneakyThrows
    public Optional<HttpRequest> decodeRequest(InputStream inputStream) {
        List<String> metadata = readHttpMetadata(inputStream);
        return buildRequest(metadata, inputStream);
    }

    @SneakyThrows
    private List<String> readHttpMetadata(InputStream inputStream) {
        List<String> metadata = new ArrayList<>();

        StringBuilder lineBuilder = new StringBuilder();
        int bytesRead;
        boolean isMetadataHeaderEndLine = false;

        while ((bytesRead = inputStream.read()) >= 0) {
            if (bytesRead == '\r') {
                int next = inputStream.read();
                if (next == '\n') {
                    if (isMetadataHeaderEndLine) {
                        break;
                    }
                    isMetadataHeaderEndLine = true;
                    metadata.add(lineBuilder.toString());
                    lineBuilder.setLength(0);
                }
            } else {
                lineBuilder.append((char) bytesRead);
                isMetadataHeaderEndLine = false;
            }
        }

        return metadata;
    }

    private Optional<HttpRequest> buildRequest(List<String> metadata, InputStream bodyInputStream) {
        if (metadata.isEmpty()) {
            return Optional.empty();
        }

        String info = metadata.get(0);
        String[] httpInfo = info.split(" ");

        if (httpInfo.length != 3) {
            return Optional.empty();
        }

        String method = httpInfo[0];
        String uri = httpInfo[1];
        String protocolVersion = httpInfo[2];

        if (!HttpVersion.HTTP_1_1.getValue().equals(protocolVersion)) {
            return Optional.empty();
        }

        try {
            var httpRequest = HttpRequest
                    .builder()
                    .httpMethod(HttpMethod.valueOf(method))
                    .uri(new URI(uri))
                    .requestHeaders(resolveRequestHeaders(metadata))
                    .bodyInputStream(bodyInputStream)
                    .build();

            return Optional.of(httpRequest);
        } catch (URISyntaxException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Map<String, List<String>> resolveRequestHeaders(List<String> httpLines) {
        Map<String, List<String>> requestHeaders = new HashMap<>();

        if (httpLines.size() > 1) {
            for (int i = 1; i < httpLines.size(); i++) {
                String header = httpLines.get(i);
                int colonIndex = header.indexOf(':');

                if (!(colonIndex > 0 && header.length() > colonIndex + 1)) {
                    break;
                }

                String headerName = header.substring(0, colonIndex);
                String headerValue = header.substring(colonIndex + 1);

                requestHeaders.compute(headerName, (key, values) -> {
                    if (values == null) {
                        values = new ArrayList<>();
                    }
                    values.add(headerValue.trim());

                    return values;
                });
            }
        }

        return requestHeaders;
    }
}
