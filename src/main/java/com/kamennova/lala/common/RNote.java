package com.kamennova.lala.common;

public class RNote {
    public short duration;
    public short interval;

    public RNote(int interval, int duration) {
        this.interval = (short) interval;
        this.duration = (short) duration;
    }
}