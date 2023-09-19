package com.example.websocketproxy;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class RestEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(RestEndpoint.class);

    private final WebClient webClient;

    public RestEndpoint() {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)) // to unlimited memory size
                .build();

        this.webClient = WebClient.builder()
                .exchangeStrategies(exchangeStrategies) // set exchange strategies
                .build();
    }

    @RequestMapping("/jupyter/**")
    public void jupyterProxyPass2(HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        // 인증 (IP, 유저) O
        // 서버 상태
        // 로깅 O

        // 소켓
        // 인증 (IP, 유저) O
        // 로깅 X)

        // request header 복사
        Map<String, String> requestHeadersMap = copyRequestHeaders(request);

        // request를 proxy
        ClientResponse clientResponse = proxyRequest(request, requestHeadersMap);

        // proxy response 받기
        Mono<byte[]> dataBufferMono = clientResponse.bodyToMono(byte[].class);
        byte[] block = dataBufferMono.block();

        // proxy response header를 origin response에 복사
        setResponseHeaders(response, clientResponse);

        // proxy response body를 origin response에 복사
        writeResponse(response, block);
    }

    private static Map<String, String> copyRequestHeaders(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            map.put(headerName, request.getHeader(headerName));
        }
        map.put("pragma", "no-cache");
        map.put("cache-control", "no-cache");
        map.remove("if-modified-since");
        return map;
    }

    private ClientResponse proxyRequest(HttpServletRequest request, Map<String, String> requestHeadersMap) throws IOException {
        String uri = "http://211.42.179.152:50002" + request.getRequestURI();
        logger.info("requested! {}", uri);

        ClientResponse clientResponse =
                webClient.method(HttpMethod.valueOf(request.getMethod()))
                        .uri(uri)
                        .headers(httpHeaders -> {
                            for (String key : requestHeadersMap.keySet()) {
                                httpHeaders.add(key, requestHeadersMap.get(key));
                            }
                        })
                        .body(BodyInserters.fromResource(new InputStreamResource(request.getInputStream())))
                        .exchange().block();
        return clientResponse;
    }

    private static void setResponseHeaders(HttpServletResponse response, ClientResponse clientResponse) {
        HashMap<String, String> map = new HashMap<>();
        for (String header : clientResponse.headers().asHttpHeaders().keySet()) {
            map.put(header, String.join(";", Objects.requireNonNull(clientResponse.headers().asHttpHeaders().get(header))));
        }
        for (String s : map.keySet()) {
            response.setHeader(s, map.get(s));
        }
    }

    private static void writeResponse(HttpServletResponse response, byte[] block) throws IOException {
        if (block == null) {
            logger.info("block null");
            return;
        }
        response.getOutputStream().write(block);
    }
}
