package org.stevi.balancer.http.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum HttpVersion {

    HTTP_1_1("HTTP/1.1");

    private final String value;

    private static Map<String, HttpVersion> VALUES_MAP;

    static {
        VALUES_MAP = Arrays.stream(values()).collect(Collectors.toMap(HttpVersion::getValue, v -> v));
    }

    private static HttpVersion byValue(String string) {
        HttpVersion httpVersion = VALUES_MAP.get(string);
        if (httpVersion == null) {
            throw new RuntimeException("No enum found by the given value string: " + string);
        }
        return httpVersion;
    }
}
