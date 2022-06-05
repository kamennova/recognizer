package com.kamennova.lala;

import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.Persistence;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Recognizer extends MusicProcessor {
    private BiFunction<String, String, Double> rateFunc = MusicProcessor::comparePatternsStrict;

    public List<Result> process(ChordSeq track) {
        processInput(track);
        List<Result> piecesResult = recognizeMixed();
        return piecesResult;
    }

    public List<Result> recognizeBySequence(Map<List<Integer>, Integer> store) {
        List<String> selected = getSequencesToPersist(store).stream() // todo rename func
                .map(MusicProcessor::getPatternString)
                .collect(Collectors.toList());

        Map<String, Double> result = persistence.findPiecesByNotePatterns(selected, rateFunc);

        List<Map.Entry<String, Double>> list = new ArrayList<>(result.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

        Map<String, Double> limited = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            limited.put(list.get(i).getKey(), list.get(i).getValue());
        }

        return limited.entrySet().stream()
                .map(entry -> new Result(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private double rateMixed(String base, String second) {
        double rate1 = MusicProcessor.comparePatternsDiff(base, second);
        double rate2 = MusicProcessor.comparePatternsSkip(base, second);
        double rateEqual = MusicProcessor.comparePatternsStrict(base, second);

        return rate2 * 1.5 + rate1; // 54.761904761904766 21.428571428571427
    }

    public List<Result> recognizeMixed() {
        setRateFunc(this::rateMixed);
        return recognizeBySequence(store3);
//        List<String> selected = getSequencesToPersist(this.store3).stream() // todo rename func
//                .map(MusicProcessor::getPatternString)
//                .collect(Collectors.toList());
//
//        Map<String, Double> result = persistence.findPiecesByNotePatterns(selected, rateFunc);
//
//        List<Map.Entry<String, Double>> list = new ArrayList<>(result.entrySet());
//        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));
//
//        Map<String, Double> limited = new LinkedHashMap<>();
//        for (int i = 0; i < list.size(); i++) {
//            limited.put(list.get(i).getKey(), list.get(i).getValue());
//        }
//
//        return limited.entrySet().stream()
//                .map(entry -> new Result(entry.getKey(), entry.getValue()))
//                .collect(Collectors.toList());
    }

//    public List<Result> getLimitedResults(){
//        Map<String, Double> limited = new LinkedHashMap<>();
//        for (int i = 0; i < Math.min(5, list.size()); i++) {
//            limited.put(list.get(i).getKey(), list.get(i).getValue());
//        }
//
//        return limited.entrySet().stream()
//                .map(entry -> new Result(entry.getKey(), entry.getValue()))
//                .collect(Collectors.toList());
//    }

    public void setRateFunc(BiFunction<String, String, Double> func) {
        this.rateFunc = func;
    }

    public Recognizer(Persistence persistence) {
        super(persistence);
    }

    public static class Result {
        public String pieceName;
        public Double precision;

        Result(String name, Double p) {
            pieceName = name;
            precision = p;
        }
    }
}
