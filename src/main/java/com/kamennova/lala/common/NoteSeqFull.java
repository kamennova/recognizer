package com.kamennova.lala.common;

import java.util.List;

public class NoteSeqFull {
    public List<RNote> notes;

    public NoteSeqFull(List<RNote> notes) {
        this.notes = notes;
    }

    public List<RNote> get() {
        return notes;
    }
}
