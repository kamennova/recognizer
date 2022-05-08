package com.kamennova.lala;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LaLaLearn extends LaLa{
    private short key;
    private short keyPrecision = 0;
    private String pieceName;

    public String getPieceName() {
        return this.pieceName;
    }

    private HashMap<List<Integer>, Integer> store4 = new HashMap<>();

    public LaLaLearn(String pieceName, Persistence persistence) {
        super(persistence);
        this.pieceName = pieceName;
    }

    private List<List<Integer>> getMotives() {
        // get most common motives from store
        // transcribe to key
        if (keyPrecision > 0.8) {
            // todo??
        }

        return new ArrayList<>();
    }

    private void persistMotives(List<List<Integer>> motives, short key) {
        // todo
    }

    public int process(LaLa.ChordSeqFull notes) throws Exception {
        super.processInput(notes);
        return 0;
    }

    public void finishLearn() {
        Stream
                <Map.Entry<List<Integer>, Integer>> top =
                store3.entrySet().stream().filter(entry -> entry.getValue() > 1)
                        .sorted(java.util.Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(5);

        top.forEach(entry -> {
            this.persistence.addPattern(this.pieceName, entry.getKey());
        });
//        List<List<Integer>> motives = getMotives();
//        persistMotives(motives, key);
    }

    public void clear() {
        pieceName = null;
        store3 = new HashMap<>();
        store4 = new HashMap<>();
        rhythmStore = new ArrayList<>();
    }

    public void setNewPiece(String pieceName) {
//        this.clear();
        this.pieceName = pieceName;
    }

    private static void log(String str, Object obj) {
        System.out.println(str);
        System.out.println(obj);
    }
}
