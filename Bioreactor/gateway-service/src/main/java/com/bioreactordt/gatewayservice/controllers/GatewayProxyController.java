package com.bioreactordt.gatewayservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
public class GatewayProxyController {

    private final RestTemplate restTemplate;

    private static final Set<String> BLOCKED_HEADERS = Set.of(
            "host", "content-length", "transfer-encoding", "connection",
            "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailers", "upgrade"
    );

    @Value("${services.twin-url}")
    private String twinUrl;

    @Value("${services.shadow-url}")
    private String shadowUrl;

    @Value("${services.models-url}")
    private String modelsUrl;

    @Value("${services.strain-url}")
    private String strainUrl;

    @Value("${services.physical-url}")
    private String physicalUrl;


    @RequestMapping("/api/twin/**")
    public ResponseEntity<String> proxyTwin(HttpServletRequest req) {
        return forward(req, twinUrl, "/api/twin");
    }

    @RequestMapping("/api/shadow/**")
    public ResponseEntity<String> proxyShadow(HttpServletRequest req) {
        return forward(req, shadowUrl, "/api/shadow");
    }

    @RequestMapping("/api/models/**")
    public ResponseEntity<String> proxyModels(HttpServletRequest req) {
        return forward(req, modelsUrl, "/api/models");
    }

    @RequestMapping("/api/strain/**")
    public ResponseEntity<String> proxyStrain(HttpServletRequest req) {
        return forward(req, strainUrl, "/api/strain");
    }

    @RequestMapping("/api/physical/**")
    public ResponseEntity<String> proxyPhysical(HttpServletRequest req) {
        return forward(req, physicalUrl, "/api/physical");
    }


    private ResponseEntity<String> forward(HttpServletRequest req, String baseUrl, String stripPrefix) {

        //Build target URL
        String path = req.getRequestURI().substring(stripPrefix.length());
        String query = req.getQueryString();
        String targetUrl = baseUrl + stripPrefix + path + (query != null ? "?" + query : "");

        HttpMethod method = HttpMethod.valueOf(req.getMethod());

        String body = "";
        try {
            body = req.getReader().lines().collect(Collectors.joining());
        } catch (Exception ignored) {}

        // Copy only safe headers
        HttpHeaders headers = new HttpHeaders();
        Collections.list(req.getHeaderNames()).forEach(name -> {
            if (!BLOCKED_HEADERS.contains(name.toLowerCase())) {
                headers.put(name, Collections.list(req.getHeaders(name)));
            }
        });

        // Ensure content-type is set for requests with a body
        if (!body.isEmpty() && !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<String> entity = new HttpEntity<>(body.isEmpty() ? null : body, headers);

        try {
            return restTemplate.exchange(targetUrl, method, entity, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    }
