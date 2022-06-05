package com.kamennova.lala;

import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.common.Note;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MusicUtils {
    public static ChordSeq getNormalizedMelodyTrack(List<ChordSeq> tracks) { // todo no separate func??
        if (tracks.isEmpty()) {
            return new ChordSeq(Collections.emptyList());
        }

        ChordSeq melodyTrack = tracks.size() == 1 ? separateMelodyPart(tracks.get(0)) :
                getMelodyTrack(tracks);
        return normalizeTrack(melodyTrack);
    }

    public static ChordSeq normalizeTrack(ChordSeq track) {
        track.chords = track.chords.stream()
                .map(chord -> Collections.singletonList(chord.stream().max(Comparator.comparing(n -> n.interval)).get()))
                .map(chord -> chord.stream()
                        .map(note -> new Note(note.interval % 12, note.duration))
                        .collect(Collectors.toSet()))
                .collect(Collectors.toList());

        return track;
    }

    public static ChordSeq separateMelodyPart(ChordSeq track) {
        double avgKey = getAvgKey(track); // todo hands reach
        short min = track.chords.stream().flatMap(notes -> notes.stream().map(n -> n.interval))
                .min(Comparator.comparing(Integer::valueOf))
                .get();
        double lowestKey = min + (avgKey - min) / 2;

        track.chords = track.chords.stream()
                .map(chord -> chord.stream().filter(note -> note.interval > lowestKey).collect(Collectors.toSet()))
                .filter(chord -> chord.size() > 0) // todo filters out pause
                .collect(Collectors.toList());

        return track;
    }

    public static ChordSeq getMelodyTrack(List<ChordSeq> tracks) {
        ChordSeq highest = tracks.get(0);
        double highestMid = 0;

        for (ChordSeq track : tracks) {
            double avg = getAvgKey(track);

            if (avg > highestMid) {
                highestMid = avg;
                highest = track;
            }
        }

        return highest;
    }

    public static double getAvgKey(ChordSeq track) {
        return track.chords.stream()
                .map(chord -> (chord.stream()
                        .map(note -> (int) note.interval).reduce(0, Integer::sum) + 0.0) / chord.size())
                .reduce(0D, Double::sum) / track.chords.size();
    }
}
