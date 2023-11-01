package org.stevi.balancer.http.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HttpStatusCode {

    OK(200, "OK"),
    NOT_FOUND(404, "Not Found");

    private final int code;
    private final String message;
}
