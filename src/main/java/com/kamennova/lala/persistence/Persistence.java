package com.kamennova.lala.persistence;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public interface Persistence {
    void addPiece(String name);

    void addPattern(String pieceName, String pattern);

    Map<String, Double> findPiecesByNotePatterns(List<String> patterns, BiFunction<String, String, Double> compFunc);

    void clearAll();

    boolean pieceExists(String name);
}
