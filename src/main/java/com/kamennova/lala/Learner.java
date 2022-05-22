package com.kamennova.lala;

import com.kamennova.lala.common.ChordSeqFull;
import com.kamennova.lala.persistence.Persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Learner extends LaLa {
    private short key;
    private short keyPrecision = 0;
    private String pieceName;
    public static final int SEQUENCES_PERSIST_LIMIT = 15;

    public String getPieceName() {
        return this.pieceName;
    }

    public Learner(String pieceName, Persistence persistence) {
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

    public int process(ChordSeqFull notes) {
        super.processInput(notes);

        return getLearnRate();
    }

    private int getLearnRate() {
        long commonSeqCount = getCommonSequences(store3, 2).count();

        return commonSeqCount < 3 ? 0 : (int) Math.min(commonSeqCount, 10);
    }

    private int getOffset(List<List<Integer>> singleSequences) {
        return 3;
    }

    public List<List<Integer>> getSequencesToPersist(Map<List<Integer>, Integer> store) {
        List<List<Integer>> mostCommon = getCommonSequences(store, 2)
                .limit(SEQUENCES_PERSIST_LIMIT)
                .map(Map.Entry::getKey).collect(Collectors.toList());


        if (mostCommon.size() < SEQUENCES_PERSIST_LIMIT) {
            List<List<Integer>> singleSequences = store.entrySet().stream()
                    .filter(entry -> entry.getValue() == 1).map(Map.Entry::getKey).collect(Collectors.toList());

            int sequencesLeft = Math.min(15, singleSequences.size()) - mostCommon.size();

            if (sequencesLeft > 0) {
                int offset = getOffset(singleSequences);

                List<List<Integer>> selectedSingle = IntStream.range(0, sequencesLeft)
                        .mapToObj(i -> singleSequences.get(i * offset))
                        .collect(Collectors.toList());

                return Stream.concat(mostCommon.stream(), selectedSingle.stream()).collect(Collectors.toList());
            }
        }

        return mostCommon;
    }

    public void finishLearn() {
        this.persistence.addPiece(this.pieceName);
        this.persistSequences(store3);
        this.persistSequences(store4);
        this.persistSequences(store5);
    }

    private void persistSequences(Map<List<Integer>, Integer> store) {
        List<List<Integer>> best = getSequencesToPersist(store);
        best.forEach(seq -> this.persistence.addPattern(pieceName, LaLa.getPatternString(seq)));
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
}
