package com.kamennova.lala.endpoint;

import com.kamennova.lala.persistence.Persistence;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class InfoHandler extends RequestHandler implements HttpHandler {
    InfoHandler(Persistence p) {
        super(p);
    }

    private String getJson(long learned) {
        return "{\"learnedNum\": " + learned + "}";
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        String error = validateRequest(httpExchange);

        if (error != null) {
            log("error", error);
            handleResponse(httpExchange, 400, getErrorString(error));
            return;
        }

        String jsonRes = getJson(persistence.getLearnedNum());

        handleResponse(httpExchange, 200, jsonRes);
    }

    private String validateRequest(HttpExchange httpExchange) {
        String method = httpExchange.getRequestMethod();
        if (!method.equals("GET")) {
            return "method not allowed";
        }

        return null;
    }
}
