package com.kamennova.lala.endpoint;

import com.kamennova.lala.Learner;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.Persistence;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LearnHandler extends RequestHandler implements HttpHandler {
    List<Learner> learners = new ArrayList<>();
    String currPieceName;
    private static final int TRACK_SIZE_MIN = 10;

    LearnHandler(Persistence p) {
        super(p);
    }

    private boolean isFinishRequest(){
        return true; // todo
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // todo

        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            httpExchange.sendResponseHeaders(204, -1);
            return;
        }

        String error = validateRequest(httpExchange);

        if (error != null) {
            handleResponse(httpExchange, 400, getErrorString(error));
            return;
        }

        String pieceName = getURIParameters(httpExchange).get(PIECE_NAME_PROPERTY);
        String pieceErr = validatePieceName(pieceName);

        if (pieceErr != null) {
            handleResponse(httpExchange, 400, getErrorString(pieceErr));
            return;
        }

        this.currPieceName = pieceName;
        System.out.println(pieceName);
        Learner learnEntity = getLearnEntity(currPieceName);

        try {
            ChordSeq track = getTrackFromAudioInput(httpExchange);

            if (track.chords.size() <= TRACK_SIZE_MIN) {
                handleResponse(httpExchange, 200,
                        "{\"message\": \"Not enough sounds recorded, please check your microphone\", \"isSuccess\": false}");
                return;
            }

            int learnLevel = learnEntity.process(track);
            learnEntity.finishLearn(); // todo remove

            String jsonRes = "{\"isSuccess\": true, \"level\": " + learnLevel + "}";
            handleResponse(httpExchange, 200, jsonRes);
        } catch (Exception e) {
            e.printStackTrace();
            handleResponse(httpExchange, 400, getErrorString("An unexpected error occurred: " + e.getMessage()));
        }
    }

    private Learner getLearnEntity(String pieceName) {
        Optional<Learner> found = learners.stream().filter(l -> l.getPieceName().equals(pieceName)).findAny();

        if (found.isPresent()) {
            return found.get();
        }

        Learner newLearner = new Learner(pieceName, persistence);
        learners.add(newLearner);
        return newLearner;
    }

    private String validateRequest(HttpExchange httpExchange) {
        String method = httpExchange.getRequestMethod();

        if (!method.equals("POST")) {
            return "method not accepted";
        }

        return null;
    }
}