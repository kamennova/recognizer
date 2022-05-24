package com.kamennova.lala.common;

import java.util.List;
import java.util.Set;

public class ChordSeq {
    public List<Set<Note>> chords;

    public ChordSeq(List<Set<Note>> ch) {
        this.chords = ch;
    }

    public List<Set<Note>> get() {
        return chords;
    }
}
