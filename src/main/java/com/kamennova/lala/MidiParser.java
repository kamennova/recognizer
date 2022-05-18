package com.kamennova.lala;

import com.kamennova.lala.common.ChordSeqFull;
import com.kamennova.lala.common.RNote;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.sound.midi.ShortMessage.NOTE_ON;

// todo speed optimizations
public class MidiParser {
    // notes within interval of this number of ticks considered to be fired at the same time
    private static final int tickPrecision = 9; // todo dynamic?

    private static boolean arePlayedSimultaneously(long tick1, long tick2) {
        return Math.abs(tick1 - tick2) <= tickPrecision;
    }

    public static List<ChordSeqFull> getNotesFromMidi(String fileName) throws Exception {
        return getNotesFromMidi(fileName, true);
    }

    /**
     * - event comes in
     * - ticks same
     * - on - add to curr played, curr tick
     * - off - remove
     * - diff tick
     * - on - flush currTick, add to curr played, add currPlayed to same tick
     * - off - flush currTick, remove from currPlayed
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static List<ChordSeqFull> getNotesFromMidi(String fileName, boolean isLegato) throws Exception {
        var sequence = MidiSystem.getSequence(new File(fileName));

        List<ChordSeqFull> tracks = new ArrayList<>();

        for (Track track : sequence.getTracks()) {
            if (track.size() == 0) continue;

            ChordSeqFull notes = new ChordSeqFull(new ArrayList<>());

//            List<Integer> currPlayedNotes = new ArrayList<>(); // keys played in current moment
//            List<Integer> currTickNotes = new ArrayList<>(); // keys played in same, last tracked,  tick
            Map<Integer, Long> currKeys = new HashMap<>();
            long previousTick = -1;
            long lastTick = 0; // tick of the last played note

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);

                if (!(event.getMessage() instanceof ShortMessage)) {
                    continue;
                }

                // todo pause
                ShortMessage message = (ShortMessage) event.getMessage();
                int key = message.getData1();
                int command = message.getCommand();
                boolean isNoteOff = message.getData2() == 0;
                boolean falseNoteOff = isNoteOff && !checkNoteOff(track, i);
                boolean isNoteOn = !isNoteOff && command == NOTE_ON;
                boolean falseNoteOn = isNoteOn && currKeys.containsKey(key);  // note was falsely off before
                long currTick = event.getTick();

                if (falseNoteOff || falseNoteOn) {
                    continue;
                }

                if (isNoteOff) {
//                    System.out.println(key + " off " + currTick);
                } else if (isNoteOn) {
//                    System.out.println(key + " on " + currTick);
                }

                if (!arePlayedSimultaneously(currTick, lastTick)) {
                    if (isNoteOn) {
                        flushChord(currKeys, notes, Math.toIntExact(lastTick - currTick), isLegato);
                    } else if (isNoteOff) {
                        // play if contains key not flushed before
                        if (currKeys.containsValue(lastTick)) {
                            flushChord(currKeys, notes, Math.toIntExact(lastTick - currTick), isLegato);
                        }
                    }

                    previousTick = lastTick;
                    lastTick = currTick;
                } else {
                    if (isNoteOff) {
                        currKeys.remove(key);
                    } else if (isNoteOn) {
                        currKeys.put(key, currTick);
                    }
                }
            }

            if (notes.chords.size() > 0) {
                tracks.add(notes);
            }
        }

        return tracks;
    }

    // todo pause
    public static List<ChordSeqFull> getNotesFromMidiStaccato(String fileName) throws Exception {
        var sequence = MidiSystem.getSequence(new File(fileName));

        List<ChordSeqFull> tracks = new ArrayList<>();

        for (Track track : sequence.getTracks()) {
            if (track.size() == 0) continue;

            ChordSeqFull notes = new ChordSeqFull(new ArrayList<>());
            Map<Integer, Long> currKeys = new HashMap<>();
            long lastTick = 0; // tick of the last played note

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);

                if (!(event.getMessage() instanceof ShortMessage)) {
                    continue;
                }

                ShortMessage message = (ShortMessage) event.getMessage();
                int key = message.getData1();
                int command = message.getCommand();
                boolean isNoteOff = message.getData2() == 0;
                boolean isNoteOn = !isNoteOff && command == NOTE_ON;
                boolean falseNoteOn = isNoteOn && currKeys.containsKey(key);  // note was falsely off before
                long currTick = event.getTick();

                if (!isNoteOn || falseNoteOn) {
                    continue;
                }

                if (!arePlayedSimultaneously(currTick, lastTick)) {
                    flushChord(currKeys, notes, Math.toIntExact(lastTick - currTick), false);
                    lastTick = currTick;
                }
                currKeys.put(key, currTick);
            }

            if (notes.chords.size() == 0) {
                continue;
            }

            if (notes.chords.get(0).size() == 0) {
                notes.chords = notes.chords.subList(1, notes.chords.size());
            }
            tracks.add(notes);

        }

        return tracks;
    }

    private static void flushChord(Map<Integer, Long> currKeys, ChordSeqFull notes, int dur, boolean legato) {
        List<RNote> chord = currKeys.keySet().stream().map(n -> new RNote(n, dur))
                .collect(Collectors.toList());
        notes.chords.add(chord);

        if (!legato) {
            currKeys.clear();
        }
    }

    private static boolean checkNoteOff(Track track, int eventIndex) {
        MidiEvent event = track.get(eventIndex);
        ShortMessage message = (ShortMessage) event.getMessage();

        if (eventIndex == track.size() - 1) { // last event
            return true;
        }

        int currEventIndex = eventIndex + 1;
        List<MidiEvent> sameTickEvents = new ArrayList<>();
        while (track.get(currEventIndex).getTick() == event.getTick()) {
            sameTickEvents.add(track.get(currEventIndex));
            currEventIndex++;
        }
        return !containsKeyOn(sameTickEvents, message.getData1());
    }

    private static boolean containsKeyOn(List<MidiEvent> events, int key) {
        return events.size() > 0 && events.stream().anyMatch(event -> {
            if (!(event.getMessage() instanceof ShortMessage)) {
                return false;
            }

            ShortMessage message = (ShortMessage) event.getMessage();
            int eKey = message.getData1();
            int eCommand = message.getCommand();
            return eCommand == NOTE_ON && eKey == key;
        });
    }

    private static void log(String name, Object obj) {
        System.out.println(name + ": " + obj);
    }
}
