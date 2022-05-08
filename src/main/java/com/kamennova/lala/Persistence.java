package com.kamennova.lala;

import java.util.List;

public interface Persistence {
    public void addPiece(String name);

    public void addPattern(String pieceName, List<Integer> pattern );
}
