package com.ibm.ada.model;

import lombok.AllArgsConstructor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@AllArgsConstructor(staticName = "apply")
public class HttpEndpoint {

    private final URL baseUrl;

    public URI uri() {
        try {
            return baseUrl.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public URL url() {
        return baseUrl;
    }

    public HttpEndpoint resolve(String uri) {
        try {
            return apply(new URL(baseUrl + uri));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("%s is not a valid URI", uri));
        }
    }

}
