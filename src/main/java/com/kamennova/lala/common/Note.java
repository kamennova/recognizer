package com.kamennova.lala.common;

public class Note {
    public short duration;
    public short interval;

    public Note(int interval, int duration) {
        this.interval = (short) interval;
        this.duration = (short) duration;
    }
}