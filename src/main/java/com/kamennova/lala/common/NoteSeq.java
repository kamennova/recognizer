package com.kamennova.lala.common;

import java.util.List;

public class NoteSeq {
    public List<Note> notes;

    public NoteSeq(List<Note> notes) {
        this.notes = notes;
    }

    public List<Note> get() {
        return notes;
    }
}
