package com.kamennova.lala;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class LaLaLearn {
    private short key;
    private short keyPrecision = 0;
    private String pieceName;

    private HashMap<List<Integer>, Integer> store3 = new HashMap<>();
    private HashMap<List<Integer>, Integer> store4 = new HashMap<>();
    private List<List<Integer>> rhythmStore = new ArrayList<>();

    public LaLaLearn(String pieceName) {
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

    public void processInput(LaLa.ChordSeqFull notes, List<Integer> r) throws Exception {
        List<LaLa.NoteSeqFull> sequences3 = getSequences(notes, 3);
        log("seq", sequences3.get(0));
        log("size", sequences3.size());
        storeSequences(sequences3);
        storeRhythm(r);
    }

    public static List<LaLa.NoteSeqFull> getSequences(LaLa.ChordSeqFull track, Integer size) {
                List<LaLa.NoteSeqFull> seqs = new ArrayList<>();
        List<LaLa.RNote> notes = track.chords.stream()
                .map(chord -> chord.stream().max(Comparator.comparing(n -> n.interval)).get())
                .collect(Collectors.toList());

        for (int i = 0; i < notes.size() - size; i++) {
            LaLa.NoteSeqFull seq = new LaLa.NoteSeqFull(notes.subList(i, i + size));
            log("s", seq.notes.stream().map(n -> n.interval).collect(Collectors.toList()));
            seqs.add(seq);
        }

        return seqs;
    }


    private void storeSequences(List<LaLa.NoteSeqFull> seqs) {
        seqs.stream().map(seq ->seq.notes.stream().map(n-> Math.toIntExact(n.interval)).collect(Collectors.toList()))
                .forEach(notes -> store3.put(notes, store3.getOrDefault(notes, 0) + 1));

        log("all", store3.entrySet().stream().sorted(Comparator.comparing(se -> se.getValue())).collect(Collectors.toList()));
    }

    private void storeRhythm(List<Integer> r) {
        rhythmStore.add(r);
    }


    public void finishLearn() {
        log("all", store3.entrySet());
//        List<List<Integer>> motives = getMotives();
//        persistMotives(motives, key);
    }


    private static void log(String str, Object obj) {
        System.out.println(str);
        System.out.println(obj);
    }

}
