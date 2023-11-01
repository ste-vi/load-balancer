package org.stevi.balancer.http.model;

import lombok.Builder;
import lombok.Getter;
import org.stevi.balancer.http.enumeration.HttpMethod;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Getter
@Builder
public class HttpRequest {

    private final HttpMethod httpMethod;
    private final URI uri;
    private final Map<String, List<String>> requestHeaders;
    private final InputStream bodyInputStream;

}
