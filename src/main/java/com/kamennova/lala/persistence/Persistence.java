package com.kamennova.lala.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public interface Persistence {
    public void addPiece(String name);

    public void addPattern(String pieceName, List<Integer> pattern);

    public List<String> findPiecesWithPattern(List<Integer> pattern);

    public HashMap<String, Integer> findPiecesByNotePatterns3(List<List<Integer>> patterns);

    public HashMap<String, Integer> findPiecesByNotePatterns4(List<List<Integer>> patterns);

    public HashMap<String, Integer> findPiecesByNotePatterns5(List<List<Integer>> patterns);

    public HashMap<String, Integer> findPiecesByLinguisticPatterns3(List<String> patterns,
                                                                    BiFunction<String, String, Integer> compFunc);

    public HashMap<String, Integer> findPiecesByLinguisticPatterns4(List<String> patterns);

    public HashMap<String, Integer> findPiecesByLinguisticPatterns5(List<String> patterns);

//    public HashMap<String, Integer> findPiecesWithRhythm(List<List<Integer>> patterns);
}
