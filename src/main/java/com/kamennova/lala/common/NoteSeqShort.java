package com.kamennova.lala.common;

import java.util.List;

public class NoteSeqShort {
    public List<Integer> notes;

    public NoteSeqShort(List<Integer> notes) {
        this.notes = notes;
    }

    public List<Integer> get() {
        return notes;
    }
}