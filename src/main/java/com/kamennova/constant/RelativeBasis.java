package com.kamennova.constant;

import com.kamennova.constant.patterns.Interval;

import java.util.List;

// note, chord or rest
public class RelativeBasis {
    private List<Interval> intervals;
    private short duration; // relative duration

    public RelativeBasis(List<Interval> intervals, int duration) {
        this.intervals = intervals;
        this.duration = (short) duration;
    }

    public RelativeBasis(Interval interval, int duration) {
        this.intervals = List.of(interval);
        this.duration = (short) duration;
    }

    public List<Interval> get() {
        return intervals;
    }

    public short getDuration() {
        return this.duration;
    }
}

