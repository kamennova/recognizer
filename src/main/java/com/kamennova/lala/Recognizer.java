package com.kamennova.lala;

import com.kamennova.lala.common.ChordSeqFull;
import com.kamennova.lala.persistence.Persistence;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Recognizer extends LaLa {
    private static final Result NO_RESULT = new Result(null, 0F);
    private BiFunction<String, String, Integer> rateFunc = LaLa::comparePatternsStrict;

    public Result process(ChordSeqFull track) {
        processInput(track);

        System.out.println(store3.size());

        HashMap<String, Integer> piecesResult = new HashMap<>();
//        HashMap<String, Integer> piecesResult = recognizeBySequence(store3);

        if (piecesResult.size() == 0) { // todo or else
            return NO_RESULT;
        }

        Map.Entry<String, Integer> piece = piecesResult.entrySet().stream()
                .min(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .get();
        return new Result(piece.getKey(), (float) piece.getValue() + 0.0F);
    }

    public List<Result> recognizeByRhythm() {
        List<String> selected = getCommonSequences(new HashMap<>(), 2)
                .filter(entry -> entry.getValue() > 1)
                .map(entry -> LaLa.getPatternString(entry.getKey()))
                .collect(Collectors.toList());

        Map<String, Integer> result = persistence.findPiecesByNotePatterns(selected, rateFunc);
        return result.entrySet().stream()
                .map(entry -> new Result(entry.getKey(), entry.getValue() + 0.0F))
                .collect(Collectors.toList());
    }

    public List<Result> recognizeBySequence(Map<List<Integer>, Integer> store) {
        List<String> selected = getSequencesToPersist(store).stream() // todo rename func
                .map(LaLa::getPatternString)
                .collect(Collectors.toList());

        Map<String, Integer> result = persistence.findPiecesByNotePatterns(selected, rateFunc);
        return result.entrySet().stream()
                .map(entry -> new Result(entry.getKey(), entry.getValue() + 0.0F))
                .collect(Collectors.toList());
    }

    public void setRateFunc(BiFunction<String, String, Integer> func) {
        this.rateFunc = func;
    }

    public Recognizer(Persistence persistence) {
        super(persistence);
    }

    public static class Result {
        public String pieceName;
        public Float precision;

        Result(String name, Float p) {
            pieceName = name;
            precision = p;
        }
    }
}
