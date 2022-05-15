package com.kamennova.lala;

import com.kamennova.lala.common.ChordSeqFull;
import com.kamennova.lala.common.NoteSeqFull;
import com.kamennova.lala.common.RNote;
import com.kamennova.lala.common.Tonality;
import com.kamennova.lala.persistence.Persistence;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LaLa {
    LaLa(Persistence persistence) {
        this.persistence = persistence;
    }

    protected Persistence persistence;
    protected HashMap<List<Integer>, Integer> store3 = new HashMap<>();
    protected List<List<Integer>> rhythmStore = new ArrayList<>();

    public static ChordSeqFull getNormalizedMelodyTrack(List<ChordSeqFull> tracks) { // todo no separate func??
        ChordSeqFull melodyTrack = getMelodyTrack(tracks);
        log("sizer", tracks.size());
//        log("track1", tracks.get(0).chords.get(0).get(0).interval);
//        log("track2", tracks.get(1).chords.get(0).get(0).interval);
        log("melody", melodyTrack.chords.get(0).get(0).interval);
        return normalizeTrack(melodyTrack);
    }

    private static ChordSeqFull getMelodyTrack(List<ChordSeqFull> tracks) {
        ChordSeqFull highest = tracks.get(0);
        double highestMid = 0;

        for (ChordSeqFull track : tracks) {
            double avg = track.chords.stream()
                    .map(chord -> (chord.stream()
                            .map(note -> (int) note.interval).reduce(0, Integer::sum) + 0.0) / chord.size())
                    .reduce(0D, Double::sum) / track.chords.size();

            if (avg > highestMid) {
                highestMid = avg;
                highest = track;
            }
        }

        return highest;
    }

    public void processInput(ChordSeqFull notes) {
        List<NoteSeqFull> sequences3 = LaLa.getSequences(notes, 3);
        storeSequences(sequences3);
        List<Integer> rhythm = LaLa.getRhythm(notes);
        storeRhythm(rhythm);
    }

    private void storeSequences(List<NoteSeqFull> seqs) {
        seqs.stream().map(seq -> seq.notes.stream().map(n -> Math.toIntExact(n.interval)).collect(Collectors.toList()))
                .forEach(notes -> store3.put(notes, store3.getOrDefault(notes, 0) + 1));

        log("all", store3.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList()));
    }

    private void storeRhythm(List<Integer> r) {
        rhythmStore.add(r);
    }

    private static void printRhythm(List<Integer> r) {
        log("rhythm", r.stream().map(("-")::repeat).collect(Collectors.joining(" ")));
    }

    private static Tonality getTonality(List<List<RNote>> notes) {
        HashMap<Integer, Integer> semiMap = new HashMap<>();
        List<Integer> semi = notes.stream().flatMap(Collection::stream)
                .map(note -> note.interval % 12)
                .filter(LaLa::isNoteSemi)
                .collect(Collectors.toList());

        semi.forEach(n -> semiMap.put(n, semiMap.getOrDefault(n, 0) + 1));

        Tonality best = Constants.TONALITIES.get(0);
        double bestScore = -1;

        for (int i = 0; i < Constants.TONALITIES.size(); i++) {
            Tonality curr = Constants.TONALITIES.get(i);
            double score = getTonalityScore(curr, new ArrayList<>(semiMap.keySet()));
            if (score > bestScore) {
                bestScore = score;
                best = curr;
            }
        }

        return best;
    }

    private static double getTonalityScore(Tonality tonality, List<Integer> semi) {
        // 1. how much out of them contains (percent)
        // 2. how much in semi and not in sharps (percent to semi)

        int tonalityContains = (int) tonality.sharps.stream().filter(semi::contains).count();
        double semiOutPercent = (0.0 + semi.size() - tonalityContains) / semi.size();
        double tonalityPercent = (0.0 + tonalityContains) / tonality.sharps.size();

        return tonalityPercent - semiOutPercent;
    }

    private static boolean isNoteSemi(int note) {
        int rest = note % 12;
        return rest == 1 || rest == 3 || rest == 6 || rest == 8 || rest == 10;
    }

    private static List<NoteSeqFull> chordToNoteSeq(ChordSeqFull seq) {
        return combineNotesStep(seq, 0);
    }

    private static List<NoteSeqFull> combineNotesStep(ChordSeqFull chords, int chordIndex) {
        List<RNote> currChord = chords.get().get(chordIndex);

        if (chordIndex == chords.get().size() - 1) {
            return currChord.stream().map(n -> new NoteSeqFull(Collections.singletonList(n)))
                    .collect(Collectors.toList());
        }

        List<NoteSeqFull> prevs = combineNotesStep(chords, chordIndex + 1);

        return currChord.stream().flatMap(note -> prevs.stream().map(prSeq -> {
            List<RNote> conc = Stream.concat(Stream.of(note), prSeq.get().stream()).collect(Collectors.toList());
            return new NoteSeqFull(conc);
        })).collect(Collectors.toList());
    }

    private String formatInts(List<Integer> ints) {
        return ints.stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    private static ChordSeqFull normalizeTrack(ChordSeqFull track) {
        // 1. choose tops??

        // 2. to notes
//        track.chords = track.chords
//                .map(chord -> chord.stream().map(note -> new RNote(note. % 12).collect(Collectors.toList()))
//                .map(chord -> List.of(chord.get(0)))
//                .subList(6, // todo
//                        track.chords.size() - 1);

        track.chords.forEach(ch -> ch.forEach(n -> n.interval = (short) (n.interval % 12)));
        return track;
    }

    public static List<NoteSeqFull> getSequences(ChordSeqFull track, Integer size) { // todo make one without shift to save???
        List<NoteSeqFull> seqs = new ArrayList<>();
        List<RNote> notes = track.chords.stream()
                .map(chord -> chord.stream().max(Comparator.comparing(n -> n.interval)).get())
                .collect(Collectors.toList());

        for (int i = 0; i < notes.size() - size; i++) {
            NoteSeqFull seq = new NoteSeqFull(notes.subList(i, i + size));
            log("s", seq.notes.stream().map(n -> n.interval).collect(Collectors.toList()));

            if (!(seq.notes.get(0).interval == seq.notes.get(1).interval && seq.notes.get(1).interval == seq.notes.get(2).interval)) {
                seqs.add(seq);
            }
        }

        return seqs;
    }

    public static List<Integer> getRhythm(ChordSeqFull seq) { // todo pause
        return seq.chords.stream().map(ch -> ch.get(0).duration / 55).collect(Collectors.toList());
    }

    protected static void log(String str, Object obj) {
        System.out.println(str);
        System.out.println(obj);
    }
}
