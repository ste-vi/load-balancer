package org.stevi.balancer.http.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.stevi.balancer.http.enumeration.HttpStatusCode;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Builder
public class HttpResponse implements Serializable {

    private HttpStatusCode statusCode;
    private Object entity;
    private final Map<String, List<String>> responseHeaders;
    private InputStream responseBodyStream;

}
