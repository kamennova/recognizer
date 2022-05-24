package com.kamennova.lala;

import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.common.NoteSeq;
import com.kamennova.lala.common.Note;
import com.kamennova.lala.common.Tonality;
import com.kamennova.lala.persistence.Persistence;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LaLa {
    LaLa(Persistence persistence) {
        this.persistence = persistence;
    }

    protected Persistence persistence;
    protected HashMap<List<Integer>, Integer> store3 = new HashMap<>();
    protected HashMap<List<Integer>, Integer> store4 = new HashMap<>();
    protected HashMap<List<Integer>, Integer> store5 = new HashMap<>();
    public static final int SEQUENCES_PERSIST_LIMIT = 15;

    public Map<List<Integer>, Integer> getStore3() {
        return store3;
    }

    public Map<List<Integer>, Integer> getStore4() {
        return store4;
    }

    public HashMap<List<Integer>, Integer> getStore5() {
        return store5;
    }

    protected List<List<Integer>> rhythmStore = new ArrayList<>();

    public Map<List<Integer>, Integer> getSequenceStore() {
        return store3;
    }

    public static String getPatternString(List<Integer> pattern) {
        return pattern.stream().map(note -> ((char) (65 + note))).collect(Collector.of(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append,
                StringBuilder::toString));
    }

    public static int comparePatternsStrict(String base, String second) {
        return base.equals(second) ? 1 : 0;
    }

    public static int comparePatternsSkip(String base, String second) {
        int diff = 0;
        for (int i = 0; i < base.length(); i++) {
            if (base.charAt(i) != second.charAt(i)) {
                diff++;
            }

            if (diff == 2) {
                return 0;
            }
        }

        return 3 - diff;
    }

    public static int comparePatternsDiff(String base, String second) {
        int diff = 0;
        for (int i = 0; i < base.length(); i++) {
            if (base.charAt(i) != second.charAt(i)) {
                diff++;
            }

            if (diff == 2) {
                return 0;
            }
        }

        return 3 - diff;
    }

    public static int comparePatternsMixed(String base, String second) {
        return base.equals(second) ? 1 : 0;
    }

    public static ChordSeq getNormalizedMelodyTrack(List<ChordSeq> tracks) { // todo no separate func??
        ChordSeq melodyTrack = tracks.size() == 1 ? separateMelodyPart(tracks.get(0)) :
                getMelodyTrack(tracks);
        return normalizeTrack(melodyTrack);
    }

    private static ChordSeq separateMelodyPart(ChordSeq track) {
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

    private static ChordSeq getMelodyTrack(List<ChordSeq> tracks) {
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

    private static double getAvgKey(ChordSeq track) {
        return track.chords.stream()
                .map(chord -> (chord.stream()
                        .map(note -> (int) note.interval).reduce(0, Integer::sum) + 0.0) / chord.size())
                .reduce(0D, Double::sum) / track.chords.size();
    }

    public void processInput(ChordSeq notes) {
        List<NoteSeq> sequences3 = LaLa.getSequences(notes, 3);
        List<NoteSeq> sequences4 = LaLa.getSequences(notes, 4);
        List<NoteSeq> sequences5 = LaLa.getSequences(notes, 5);
        storeSequences(store3, sequences3);
        storeSequences(store4, sequences4);
        storeSequences(store5, sequences5);
        List<Integer> rhythm = LaLa.getRhythm(notes);

        storeRhythm(rhythm);
    }


    private int getOffset(List<List<Integer>> singleSequences) {
        return 3; // todo
    }

    public List<List<Integer>> getSequencesToPersist(Map<List<Integer>, Integer> store) {
        List<List<Integer>> mostCommon = getCommonSequences(store, 2)
                .limit(SEQUENCES_PERSIST_LIMIT)
                .map(Map.Entry::getKey).collect(Collectors.toList());

        if (mostCommon.size() < SEQUENCES_PERSIST_LIMIT) {
            List<List<Integer>> singleSequences = store.entrySet().stream()
                    .filter(entry -> entry.getValue() == 1).map(Map.Entry::getKey).collect(Collectors.toList());

            int sequencesLeft = Math.min(15, singleSequences.size()) - mostCommon.size();

            if (sequencesLeft > 0) {
                int offset = getOffset(singleSequences);

                List<List<Integer>> selectedSingle = IntStream.range(0, sequencesLeft)
                        .mapToObj(i -> singleSequences.get(i * offset))
                        .collect(Collectors.toList());

                return Stream.concat(mostCommon.stream(), selectedSingle.stream()).collect(Collectors.toList());
            }
        }

        return mostCommon;
    }

    protected Stream<Map.Entry<List<Integer>, Integer>> getCommonSequences(Map<List<Integer>, Integer> store,
                                                                           int repeatMin) {
        return store.entrySet().stream()
                .filter(entry -> entry.getValue() >= repeatMin)
                .sorted(java.util.Map.Entry.comparingByValue(Comparator.reverseOrder()));
    }

    private void storeSequences(Map<List<Integer>, Integer> store, List<NoteSeq> seqs) {
        seqs.stream()
                .map(seq -> seq.notes.stream()
                        .map(n -> Math.toIntExact(n.interval))
                        .collect(Collectors.toList()))
                .forEach(notes -> store.put(notes, store.getOrDefault(notes, 0) + 1));
    }

    private void storeRhythm(List<Integer> r) {
        rhythmStore.add(r);
    }

    public static void printRhythm(List<Integer> r) {
        log("rhythm", r.stream().map(("-")::repeat).collect(Collectors.joining(" ")));
    }

    private static Tonality getTonality(List<List<Note>> notes) {
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

    private static List<NoteSeq> chordToNoteSeq(ChordSeq seq) {
        return combineNotesStep(seq, 0);
    }

    private static List<NoteSeq> combineNotesStep(ChordSeq chords, int chordIndex) {
        Set<Note> currChord = chords.get().get(chordIndex);

        if (chordIndex == chords.get().size() - 1) {
            return currChord.stream().map(n -> new NoteSeq(Collections.singletonList(n)))
                    .collect(Collectors.toList());
        }

        List<NoteSeq> prevs = combineNotesStep(chords, chordIndex + 1);

        return currChord.stream().flatMap(note -> prevs.stream().map(prSeq -> {
            List<Note> conc = Stream.concat(Stream.of(note), prSeq.get().stream()).collect(Collectors.toList());
            return new NoteSeq(conc);
        })).collect(Collectors.toList());
    }

    private String formatInts(List<Integer> ints) {
        return ints.stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    private static ChordSeq normalizeTrack(ChordSeq track) {
        track.chords = track.chords.stream()
                .map(chord -> Collections.singletonList(chord.stream().max(Comparator.comparing(n -> n.interval)).get()))
                .map(chord -> chord.stream()
                        .map(note -> new Note(note.interval % 12, note.duration))
                        .collect(Collectors.toSet()))
                .collect(Collectors.toList());

        return track;
    }

    public static List<NoteSeq> getSequences(ChordSeq track, Integer size) { // todo make one without shift to save???
        List<NoteSeq> seqs = new ArrayList<>();
        List<Note> notes = track.chords.stream()
                .map(chord -> chord.stream().max(Comparator.comparing(n -> n.interval)).get())
                .collect(Collectors.toList());

        for (int i = 0; i < notes.size() - size; i++) {
            NoteSeq seq = new NoteSeq(notes.subList(i, i + size));

            if (!areAllNotesSame(seq)) {
                seqs.add(seq);
            }
        }

        return seqs;
    }

    private static boolean areAllNotesSame(NoteSeq notes) {
        return notes.notes.stream().map(n -> n.interval).distinct().count() == 1;
    }

    public static List<Integer> getRhythm(ChordSeq seq) { // todo pause
        return seq.chords.stream().map(ch -> ch.iterator().next().duration / 55).collect(Collectors.toList());
    }

    protected static void log(String str, Object obj) {
        System.out.println(str);
        System.out.println(obj);
    }
}
