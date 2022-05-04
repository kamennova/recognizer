package com.kamennova.lala;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LaLa {

    public static void main(String[] args) throws Exception {
        // learn
        learnMode("Sonat111");
//        recognitionMode();
    }

    private void onProcess(String inputPath) throws Exception {
        List<LaLa.ChordSeqFull> tracks = MidiParser.getNotesFromMidi(inputPath);
        LaLa.ChordSeqFull melodyTrack = tracks.get(1);
        System.out.println(melodyTrack.chords.get(1));

        Tonality t = getTonality(melodyTrack.chords);
        LaLa.ChordSeqFull normalized = normalize(melodyTrack);

        System.out.println(melodyTrack.chords.stream().map(ch -> ch.stream().map(
                n -> Constants.NOTE.getNoteName(n.interval) + " " + n.duration)
                .collect(Collectors.toList())).collect(Collectors.toList()));

    }


    private static ChordSeqFull getNormalizedTrack(String inputPath) throws Exception {
        List<LaLa.ChordSeqFull> tracks = MidiParser.getNotesFromMidi(inputPath);
        LaLa.ChordSeqFull melodyTrack = getMelodyTrack(tracks);
        log("sizer", tracks.size());
//        log("track1", tracks.get(0).chords.get(0).get(0).interval);
//        log("track2", tracks.get(1).chords.get(0).get(0).interval);
        log("melody", melodyTrack.chords.get(0).get(0).interval);
        return normalize(melodyTrack);
    }

    private static ChordSeqFull getMelodyTrack(List<ChordSeqFull> tracks) {
        ChordSeqFull highest = tracks.get(0);
        double highestMid = 0;

        for (ChordSeqFull track : tracks){
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

    private static void learnMode(String pieceName) throws Exception {
        LaLaLearn l = new LaLaLearn(pieceName);

        learnModeOnInput(l, "src/main/resources/lmlyd.mid");
//        learnModeOnInput(l,"src/main/resources/Path1.mid");
//        learnModeOnInput(l, "src/main/resources/Always1.mid");
        l.finishLearn();
    }

    private static void learnModeOnInput(LaLaLearn l, String inputPath) throws Exception {
        ChordSeqFull normalized = getNormalizedTrack(inputPath);
        List<Integer> rhythm = getRhythm(normalized);
        printRhythm(rhythm);
        l.processInput(normalized, rhythm);
    }

    private static void recognitionMode() throws Exception {
        LaLaRecognize recognize = new LaLaRecognize();
        recognitionModeOnInput(recognize, "src/main/resources/Path1.mid");
        recognitionModeOnInput(recognize, "src/main/resources/Always1.mid");
    }

    private static void recognitionModeOnInput(LaLaRecognize r, String inputPath) throws Exception {
        ChordSeqFull normalized = getNormalizedTrack(inputPath);
        r.processInput(normalized);

    }

    public static class ChordSeq {
        public List<List<Integer>> chords;

        public ChordSeq(List<List<Integer>> ch) {
            this.chords = ch;
        }
    }

    public static class ChordSeqFull {
        public List<List<RNote>> chords;

        public ChordSeqFull(List<List<RNote>> ch) {
            this.chords = ch;
        }

        public List<List<RNote>> get() {
            return chords;
        }
    }

    public static class NoteSeq {
        public List<Integer> notes;

        public NoteSeq(List<Integer> notes) {
            this.notes = notes;
        }

        public List<Integer> get() {
            return notes;
        }
    }

    public static class NoteSeqFull {
        public List<RNote> notes;

        public NoteSeqFull(List<RNote> notes) {
            this.notes = notes;
        }

        public List<RNote> get() {
            return notes;
        }
    }

    private static void printRhythm(List<Integer> r) {
        log("rhythm", r.stream().map(("-")::repeat).collect(Collectors.joining(" ")));
    }

    public static class RNote {
        public short duration;
        public short interval;

        public RNote(int interval, int duration) {
            this.interval = (short) interval;
            this.duration = (short) duration;
        }
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

    public static List<NoteSeqFull> getSequences(ChordSeqFull seq, Integer size) {
        int startsNum = Math.min(size, seq.chords.size() - 1);

        List<NoteSeqFull> noteSeqs = new ArrayList<>();

        for (int i = 0; i < startsNum; i++) {
            List<NoteSeqFull> curr = batches(seq.get().subList(i, seq.get().size()), size)
                    .flatMap(chordSeq -> chordToNoteSeq(new ChordSeqFull(chordSeq)).stream()) // todo
                    .collect(Collectors.toList());
            noteSeqs.addAll(curr);
        }

        return noteSeqs;
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

    private static <T> Stream<List<T>> batches(List<T> source, int length) {
        if (length <= 0)
            throw new IllegalArgumentException("length = " + length);
        int size = source.size();
        if (size <= 0)
            return Stream.empty();
        int fullChunks = (size - 1) / length;
//        log("full chunks", fullChunks);
        return IntStream.range(0, fullChunks + 1).mapToObj(
                n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
    }

    private void findSimilar(List<Integer> notes) {

    }

    private String formatInts(List<Integer> ints) {
        return ints.stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    private static ChordSeqFull normalize(ChordSeqFull track) {
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

    private static List<Integer> getRhythm(ChordSeqFull seq) {
        return seq.chords.stream().map(ch -> ch.get(0).duration / 55).collect(Collectors.toList());
    }

    private static void log(String str, Object obj) {
        System.out.println(str);
        System.out.println(obj);
    }
}
