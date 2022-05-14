package com.kamennova.lala.common;

import java.util.List;

public class NoteSeq {
    public List<Integer> notes;

    public NoteSeq(List<Integer> notes) {
        this.notes = notes;
    }

    public List<Integer> get() {
        return notes;
    }
}