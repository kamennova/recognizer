package com.kamennova.lala.persistence;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public interface Persistence {
    public void addPiece(String name);

    public void addPattern(String pieceName, String pattern);

    public List<String> findPiecesWithPattern(String pattern, BiFunction<String, String, Integer> compFunc);

    public Map<String, Integer> findPiecesByNotePatterns(List<String> patterns, BiFunction<String, String, Integer> compFunc);

//    public HashMap<String, Integer> findPiecesWithRhythm(List<List<Integer>> patterns);
}
