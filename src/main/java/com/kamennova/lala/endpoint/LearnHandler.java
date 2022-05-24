package com.kamennova.lala.endpoint;

import com.kamennova.lala.Learner;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.Persistence;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class LearnHandler extends RequestHandler implements HttpHandler {
    Learner currLearn;
    String currPieceName;
    private static final String PIECE_NAME_PROPERTY = "pieceName";

    LearnHandler(Persistence p) {
        super(p);
    }

    private boolean isStartRequest(HttpExchange httpExchange) {
        return httpExchange.getRequestURI().toString().contains("start");
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // todo
        String error = validateRequest(httpExchange);

        if (error != null) {
            handleResponse(httpExchange, getErrorString(error));
            return;
        }

        if (isStartRequest(httpExchange)) {
            try {
                Map<String, String> params = getBodyParameters(httpExchange.getRequestBody());
                this.currPieceName = params.get(PIECE_NAME_PROPERTY);

                handleResponse(httpExchange, "{\"status\": \"ok\"}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            String pathToFile = downloadRecording(httpExchange.getRequestBody());
            ChordSeq track = getTrackFromAudioInput(pathToFile); // todo single method?
            Learner learnEntity = getLearnEntity(currPieceName);

            int learnLevel = learnEntity.process(track);
            learnEntity.finishLearn(); // todo remove

            String jsonRes = "{\"level\": " + learnLevel + "}";
            handleResponse(httpExchange, jsonRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Learner getLearnEntity(String pieceName) {
        if (this.currLearn == null) {
            System.out.println("create");
            this.currLearn = new Learner(pieceName, persistence);
        } else if (!this.currLearn.getPieceName().equals(pieceName)) {
            System.out.println("new");
            this.currLearn.finishLearn();
            this.currLearn.clear();
            this.currLearn.setNewPiece(pieceName); // todo new entity ??
        } else {
            System.out.println("old");
        }

        return this.currLearn;
    }

    private String validateRequest(HttpExchange httpExchange) {
        String method = httpExchange.getRequestMethod();

        if (!method.equals("POST")) {
            return "method not accepted";
        }

        return null;
    }

    private String validateParameters(Map<String, String> params) {
        if (params.get("pathToFile") == null) {
            return "Could not parse recording file";
        } else if (params.get("pieceName") == null) {
            return "Piece name is required";
        } else if (params.get("pieceName").length() < 2) {
            return "Piece name should be at least 2 symbols long";
        }

        return null;
    }
}