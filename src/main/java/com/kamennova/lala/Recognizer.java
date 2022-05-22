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

        HashMap<String, Integer> piecesResult = recognizeByNoteSequences();
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

    private HashMap<String, Integer> recognizeByNoteSequences(){
        List<List<Integer>> filteredPatterns3 = getCommonSequences(store4, 2)
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());


        List<List<Integer>> filteredPatterns4 = getCommonSequences(store3, 2)
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

//        if (filteredPatterns.size() == 0) {
//            filteredPatterns = new ArrayList<>(store3.keySet());
//        }

        System.out.println("patterns");
        System.out.println(filteredPatterns3);
        System.out.println(filteredPatterns4);

        HashMap<String, Integer> result3 = persistence.findPiecesByNotePatterns3(filteredPatterns3);
        HashMap<String, Integer> result4 = persistence.findPiecesByNotePatterns4(filteredPatterns3);
        HashMap<String, Integer> result5 = persistence.findPiecesByNotePatterns5(filteredPatterns3);

        System.out.println("-----");
        System.out.println(result3);
        System.out.println(result4);
        return result3;
    }

    private HashMap<String, Integer> recognizeByRhythm(){
        return new HashMap<>();
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
