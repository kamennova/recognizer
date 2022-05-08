package com.kamennova.lala;

import java.util.HashMap;
import java.util.List;

public class LaLaRecognize extends LaLa {
    private HashMap<List<Integer>, Integer> store3 = new HashMap<>();

    public Result process(LaLa.ChordSeqFull track) throws Exception {
        super.processInput(track);

        Result r = new Result();
        return r;
    }

    LaLaRecognize(Persistence persistence) {
        super(persistence);
    }

    public static class Result {
        public String pieceName;
        public Float precision;


    }
}
