package com.kamennova.lala.persistence;

import java.util.HashMap;
import java.util.List;

public interface Persistence {
    public void addPiece(String name);

    public void addPattern(String pieceName, List<Integer> pattern );

    public List<String> findPiecesWithPattern(List<Integer> pattern);

    public HashMap<String, Integer> findPiecesByNotePatterns(List<List<Integer>> patterns);

//    public HashMap<String, Integer> findPiecesWithRhythm(List<List<Integer>> patterns);
}
