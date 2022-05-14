package com.kamennova.lala.common;

import java.util.List;

public class ChordSeqFull {
    public List<List<RNote>> chords;

    public ChordSeqFull(List<List<RNote>> ch) {
        this.chords = ch;
    }

    public List<List<RNote>> get() {
        return chords;
    }
}
