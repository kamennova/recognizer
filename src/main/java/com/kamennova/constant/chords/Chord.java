package com.kamennova.constant.chords;

import com.kamennova.constant.patterns.Interval;

import java.util.List;

public class Chord {
    public List<Interval> notes;

    public Chord(List<Interval> intervals) {
        this.notes = intervals;
    }
}
