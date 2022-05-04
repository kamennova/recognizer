package com.kamennova.lala;

import com.kamennova.lala.LaLa.ChordSeqFull;
import com.kamennova.lala.LaLa.RNote;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.sound.midi.ShortMessage.NOTE_ON;

// todo speed optimizations
public class MidiParser {
    public static List<ChordSeqFull> getNotesFromMidi(String fileName) throws Exception {
        var sequence = MidiSystem.getSequence(new File(fileName));

        List<ChordSeqFull> tracks = new ArrayList<>();

        for (Track track : sequence.getTracks()) {
            if (track.size() == 0) continue;

            ChordSeqFull notes = new ChordSeqFull(new ArrayList<>());

            List<Integer> currNotes = new ArrayList<>(); // indexes of notes played in current moment
            List<Integer> sameTickNotes = new ArrayList<>(); // indexes of notes played in same tick

            long lastTick = 0; // current tick index

//            System.out.println("track" + );
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);

                if (!(event.getMessage() instanceof ShortMessage)) {
                    continue;
                }

                ShortMessage message = (ShortMessage) event.getMessage();
                int key = message.getData1();
                int command = message.getCommand();
                boolean isNoteOff = message.getData2() == 0;
                long currTick = event.getTick();

                if (command != NOTE_ON || isNoteOff) {
                    if (isNoteOff) {
                        currNotes.remove(Integer.valueOf(key));
                    }

                    continue;
                }

                currNotes.add(key);

                if (currTick != lastTick) {
//                    log("1", sameTickNotes); log("2", currNotes);
                    int dur = Math.toIntExact(currTick - lastTick);
                    List<RNote> chord = sameTickNotes.stream().map(n -> new RNote(n, dur))
                            .collect(Collectors.toList());
                    if (chord.size() > 0 ){
                        notes.chords.add(chord);
                    }
                    sameTickNotes = new ArrayList<>();
                    lastTick = currTick;
                }

                sameTickNotes.add(key);
            }

            if (notes.chords.size() > 0) {
                tracks.add(notes);
            }
        }

        return tracks;
    }

    private static void log(String name, Object obj) {
        System.out.println(name + ": " + obj);
    }
}
