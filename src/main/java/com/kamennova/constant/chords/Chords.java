package com.kamennova.constant.chords;

import com.kamennova.constant.patterns.Interval;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Chords {
    private static Chord addToChord(Chord base, Interval note) {
        return new Chord(Stream.concat(base.notes.stream(), List.of(note).stream())
                .collect(Collectors.toList()));
    }

    // TRIADS
    public static Chord MAJOR_TRIAD = new Chord(List.of(Interval.Root, Interval.MajorThird, Interval.PerfectFifth));

    public static Chord MINOR_TRIAD = new Chord(List.of(Interval.Root, Interval.MinorThird, Interval.PerfectFifth));

    public static Chord AUGMENTED_TRIAD = new Chord(List.of(Interval.Root, Interval.MajorThird, Interval.AugmentedFifth));

    public static Chord DIMINISHED_TRIAD = new Chord(List.of(Interval.Root, Interval.MinorThird, Interval.DiminishedFifth));

    // SEVENTH
    public static Chord DOMINANT_SEVENTH = addToChord(MAJOR_TRIAD, Interval.MinorSeventh);

    public static Chord MAJOR_SEVENTH = addToChord(MAJOR_TRIAD, Interval.MajorSeventh);

    public static Chord MINOR_SEVENTH = addToChord(MINOR_TRIAD, Interval.MinorSeventh);

    public static Chord HALF_DIMINISHED_SEVENTH = addToChord(DIMINISHED_TRIAD, Interval.MinorSeventh);

    public static Chord DIMINISHED_SEVENTH = addToChord(DIMINISHED_TRIAD, Interval.DiminishedSeventh);

    public static Chord MINOR_MAJOR_SEVENTH = addToChord(MINOR_TRIAD, Interval.MajorSeventh);

    public static Chord AUGMENTED_MAJOR_SEVENTH = addToChord(AUGMENTED_TRIAD, Interval.MajorSeventh);

    public static Chord AUGMENTED_SEVENTH = addToChord(AUGMENTED_TRIAD, Interval.MinorSeventh);




    public static void main(String[] args) {
        System.out.println(DOMINANT_SEVENTH.notes);
    }
}
