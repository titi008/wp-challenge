package com.warpaint.challengeservice.dataprovider;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
@Slf4j
public class HttpHandler {

    private static int MAX_CONNECTIONS = 100;

    private final HttpClient httpClient;

    @Getter
    private final BasicCookieStore cookieStore;

    public HttpHandler(@Value("${com.warpaint.marketdata.yahoo.timeout:5}") int timeoutSeconds) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeoutSeconds * 1000)
                .setConnectionRequestTimeout(timeoutSeconds * 1000)
                .setSocketTimeout(timeoutSeconds * 1000).build();

        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(MAX_CONNECTIONS)
                .setMaxConnPerRoute(MAX_CONNECTIONS/2)
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(config).build();
    }

    public HttpResponse fetchResponse(HttpUriRequest request) {
        try {
            log.debug("{}: {}", request.getMethod(), request.getURI());
            HttpResponse response = httpClient.execute(request);
            log.debug("{}({}): {}", request.getMethod(), response.getStatusLine().getStatusCode(), request.getURI());
            return response;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed " + request.getMethod() + ": " + request.getURI(), e);
        }
    }

    public static String urlEncodeString(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding", e);
        }
    }

}
