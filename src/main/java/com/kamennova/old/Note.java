package com.kamennova.old;

public class Note extends Basis {
    public Short pitch;

    public Note(Short pitch, String duration) {
        super(duration);
        this.pitch = pitch;
    }
}
