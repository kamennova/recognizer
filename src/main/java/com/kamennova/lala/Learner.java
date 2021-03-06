package com.kamennova.lala;

import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.Persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Learner extends MusicProcessor {
    private short key;
    private short keyPrecision = 0;
    private String pieceName;

    public String getPieceName() {
        return this.pieceName;
    }

    public List<Integer> getRhythm(){
        return rhythmStore.get(0);
    }

    public Learner(String pieceName, Persistence persistence) {
        super(persistence);
        this.pieceName = pieceName;
    }

    public int process(ChordSeq notes) {
        super.processInput(notes);

        return getLearnRate();
    }

    private int getLearnRate() {
        long commonSeqCount = getCommonSequences(store3, 2).count();

        return commonSeqCount < 3 ? 0 : (int) Math.min(commonSeqCount, 10);
    }

    public void finishLearn(){
        this.persist(pieceName);
    }

    private void clear() {
        pieceName = null;
        store3 = new HashMap<>();
        store4 = new HashMap<>();
        store5 = new HashMap<>();
        rhythmStore = new ArrayList<>();
    }

    public void setNewPiece(String pieceName) {
        this.clear();
        this.pieceName = pieceName;
    }
}
