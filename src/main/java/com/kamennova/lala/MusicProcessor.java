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

public class MusicProcessor {
    MusicProcessor(Persistence persistence) {
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

    public static String getPatternString(List<Integer> pattern) {
        return pattern.stream().map(MusicProcessor::getNoteChar).collect(Collector.of(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append,
                StringBuilder::toString));
    }

    private static char getNoteChar(Integer note) {
        switch (note) {
            case 0:
                return 'c';
            case 2:
                return 'd';
            case 4:
                return 'e';
            case 5:
                return 'f';
            case 7:
                return 'g';
            case 9:
                return 'a';
            case 11:
                return 'b';
            case 1:
                return 'C';
            case 3:
                return 'D';
            case 6:
                return 'F';
            case 8:
                return 'G';
            case 10:
                return 'A';
            default:
                return ' ';
        }
    }

    public static double comparePatternsStrict(String base, String second) {
        return base.equals(second) ? 1 : 0;
    }

    // ex (abcd, abdf) -> 1 skip
    //
    public static double comparePatternsSkip(String base, String second) {
        int skipInBase = 0;
        int skipInSecond = 0;
        int len = base.length();

        for (int i = 0; i < len; i++) {
            int baseIndex = i + skipInBase;
            int secondIndex = i + skipInSecond;

            if (base.charAt(baseIndex) != second.charAt(secondIndex)) {
                // i = 1, base = abcd, second = acdf
                if (base.length() > baseIndex + 1 && base.charAt(baseIndex + 1) == second.charAt(secondIndex)) {
                    skipInBase++;
                    len--;
                } else if (second.length() > secondIndex + 1 && base.charAt(baseIndex) == second.charAt(secondIndex + 1)) {
                    skipInSecond++;
                    len--;
                } else {
                    return 0;
                }
            }

            if (skipInBase + skipInSecond > 1) {
                return 0;
            }
        }

        return base.length() - (skipInBase + skipInSecond) * 2;
    }

    // ex: (abcd, abcd) -> 4
    // (abcd, abfd) -> 2
    // (abcd, abpt) -> 0
    public static double comparePatternsDiff(String base, String second) {
        int diff = 0;
        for (int i = 0; i < base.length(); i++) {
            if (base.charAt(i) != second.charAt(i)) {
                diff++;
            }

            if (diff == 2) {
                return 0;
            }
        }

        return base.length() - diff * 2;
    }

    public static double comparePatternsMixed(String base, String second) {
        return base.equals(second) ? 1 : 0;
    }

    public void processInput(ChordSeq notes) {
        List<NoteSeq> sequences3 = MusicProcessor.getSequences(notes, 3);
        List<NoteSeq> sequences4 = MusicProcessor.getSequences(notes, 4);
        List<NoteSeq> sequences5 = MusicProcessor.getSequences(notes, 5);
        storeSequences(store3, sequences3);
        storeSequences(store4, sequences4);
        storeSequences(store5, sequences5);
        List<Integer> rhythm = MusicProcessor.getRhythm(notes);

        storeRhythm(rhythm);
    }

    // 7, 9
    private int getOffset(List<List<Integer>> singleSequences, int left) {
        return singleSequences.size() / left; // todo
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
                int offset = getOffset(singleSequences, sequencesLeft);

                List<List<Integer>> selectedSingle = IntStream.range(0, sequencesLeft)
                        .mapToObj(i -> singleSequences.get(i * offset))
                        .collect(Collectors.toList());

                return Stream.concat(mostCommon.stream(), selectedSingle.stream()).collect(Collectors.toList());
            }
        }

        return mostCommon;
    }

    public Stream<Map.Entry<List<Integer>, Integer>> getCommonSequences(Map<List<Integer>, Integer> store,
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

    public static Tonality getTonality(List<List<Note>> notes) {
        HashMap<Integer, Integer> semiMap = new HashMap<>();
        List<Integer> semi = notes.stream().flatMap(Collection::stream)
                .map(note -> note.interval % 12)
                .filter(MusicProcessor::isNoteSemi)
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

    public static double getTonalityScore(Tonality tonality, List<Integer> semi) {
        // 1. how much out of them contains (percent)
        // 2. how much in semi and not in sharps (percent to semi)

        int tonalityContains = (int) tonality.sharps.stream().filter(semi::contains).count();
        double semiOutPercent = (0.0 + semi.size() - tonalityContains) / semi.size();
        double tonalityPercent = (0.0 + tonalityContains) / tonality.sharps.size();

        return tonalityPercent - semiOutPercent;
    }

    public static boolean isNoteSemi(int note) {
        int rest = note % 12;
        return rest == 1 || rest == 3 || rest == 6 || rest == 8 || rest == 10;
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

    public static boolean areAllNotesSame(NoteSeq notes) {
        return notes.notes.stream().map(n -> n.interval).distinct().count() == 1;
    }

    public static List<Integer> getRhythm(ChordSeq seq) { // todo pause
        return seq.chords.stream().map(ch -> ch.iterator().next().duration / 55).collect(Collectors.toList());
    }

    protected static void log(String str, Object obj) {
        System.out.println(str);
        System.out.println(obj);
    }

    public void persist(String pieceName) {
        if (!persistence.pieceExists(pieceName)) {
            this.persistence.addPiece(pieceName);
        }
        this.persistSequences(store3, pieceName);
        this.persistSequences(store4, pieceName);
        this.persistSequences(store5, pieceName);
    }

    private void persistSequences(Map<List<Integer>, Integer> store, String pieceName) {
        List<List<Integer>> best = getSequencesToPersist(store);
        best.forEach(seq -> this.persistence.addPattern(pieceName, MusicProcessor.getPatternString(seq)));
    }
}
