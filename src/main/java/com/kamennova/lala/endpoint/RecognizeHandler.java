package com.kamennova.lala.endpoint;

import com.kamennova.lala.Recognizer;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.Persistence;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecognizeHandler extends RequestHandler implements HttpHandler {
    Recognizer recognizer;
    Recognizer prevRecognizer;

    private static final int MIN_TRACK_SIZE = 10;

    RecognizeHandler(Persistence p) {
        super(p);
    }

    private String resultToJson(List<Recognizer.Result> result) {
        String resString = "[" + result.stream().map(res ->
                "{\"name\": \"" + res.pieceName + "\", \"precision\": " + res.precision + "}").collect(Collectors.joining(","))
                + "]";

        return "{\"results\": " + resString + ", \"isSuccess\": true}";
    }

    private boolean isCorrectionRequest(HttpExchange httpExchange) {
        return httpExchange.getRequestURI().toString().contains("correct");
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
            handleResponse(httpExchange, 400, getErrorString(error));
            return;
        }

        if (isCorrectionRequest(httpExchange)) {
            String pieceName = getURIParameters(httpExchange).get(PIECE_NAME_PROPERTY);
            String nameErr = validatePieceName(pieceName);

            if (nameErr != null) {
                handleResponse(httpExchange, 200, getErrorString(nameErr));
                return;
            }

            recognizer.makeCorrection(pieceName);
            handleResponse(httpExchange, 200, "{\"isSuccess\": true}");
            return;
        }

        if (false) {
            // todo maybeNew??
            //  Map<String, String> params = getBodyParameters(httpExchange.getRequestBody());

        } else {
            try {
                ChordSeq track = getTrackFromAudioInput(httpExchange);
                List<Recognizer.Result> result = new ArrayList<>();

                if (track.chords.size() >= MIN_TRACK_SIZE) {
                    updateRecognizeEntity(false);
                    result = this.recognizer.process(track);
                }

                if (result.isEmpty()) {
                    handleResponse(httpExchange, 200,
                            "{\"message\": \"Not enough sounds recorded, please check your microphone\", \"isSuccess\": false}");
                    return;
                }

                System.out.println(result);

                handleResponse(httpExchange, 200, resultToJson(result));
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        handleResponse(httpExchange, 200, jsonRes);
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
        if (!method.equals("POST")) {
            return "method not allowed";
        }

        return null;
    }
}
