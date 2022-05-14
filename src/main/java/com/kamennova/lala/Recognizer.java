package com.kamennova.lala;

import com.kamennova.lala.common.ChordSeqFull;
import com.kamennova.lala.persistence.Persistence;

import java.util.*;
import java.util.stream.Collectors;

public class Recognizer extends LaLa {
    private static final Result NO_RESULT = new Result(null, 0F);

    public Result process(ChordSeqFull track) {
        processInput(track);

        System.out.println(store3.size());

        List<List<Integer>> filteredPatterns = store3.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (filteredPatterns.size() == 0) {
            filteredPatterns = new ArrayList<>(store3.keySet());
        }

        System.out.println("patterns");
        System.out.println(filteredPatterns);

        HashMap<String, Integer> piecesResult = persistence.findPiecesWithPatterns(filteredPatterns);
        System.out.println("here");
        System.out.println(piecesResult);

        if (piecesResult.size() == 0) { // todo or else
            return NO_RESULT;
        }

        Map.Entry<String, Integer> piece = piecesResult.entrySet().stream()
                .min(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .get();
        return new Result(piece.getKey(), (float) piece.getValue() + 0.0F);
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
