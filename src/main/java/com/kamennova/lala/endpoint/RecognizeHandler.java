package com.kamennova.lala.endpoint;

import com.kamennova.lala.Recognizer;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.Persistence;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;

public class RecognizeHandler extends RequestHandler implements HttpHandler {
    Recognizer recognizer;
    Recognizer prevRecognizer;

    RecognizeHandler(Persistence p) {
        super(p);
    }

    private String resultToJson(Recognizer.Result result) {
        HashMap<String, Object> obj = new HashMap<>();

        String resString = result.pieceName == null ? "null" :
                "{\"name\": \"" + result.pieceName + "\", \"precision\": " + result.precision + "}";
        String successString = result.precision > 0 ? "\"true\"" : "\"false\"";
        return "{\"result\": " + resString + ", \"isSuccess\": " + successString + "}";
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String jsonRes = "";
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            httpExchange.sendResponseHeaders(204, -1);
            return;
        }

        String error = validateRequest(httpExchange);

        if (error != null) {
            log("error", error);
            handleResponse(httpExchange, getErrorString(error));
            return;
        }

        if (false) {
            // todo maybeNew??
            //  Map<String, String> params = getBodyParameters(httpExchange.getRequestBody());

        } else {
            try {
                String pathToFile = downloadRecording(httpExchange.getRequestBody());
                ChordSeq track = getTrackFromAudioInput(pathToFile);
                updateRecognizeEntity(false);

                Recognizer.Result result = this.recognizer.process(track);
                handleResponse(httpExchange, resultToJson(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        handleResponse(httpExchange, jsonRes);
    }

    private void updateRecognizeEntity(boolean mightNeedNew) {
        if (this.recognizer == null) {
            System.out.println("create recognizer");
            this.recognizer = new Recognizer(persistence);
        } else if (mightNeedNew) {
            prevRecognizer = this.recognizer;
            this.recognizer = new Recognizer(persistence);
        }
    }

    private String validateRequest(HttpExchange httpExchange) {
        String method = httpExchange.getRequestMethod();
        if (!method.equals("POST") && !method.equals("OPTIONS")) {
            return "method not accepted";
        }

        return null;
    }
}
