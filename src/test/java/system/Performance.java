package system;

import com.kamennova.lala.*;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.RedisPersistence;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.junit.jupiter.api.Test;
import unit.AudioFileCutter;
import unit.BaseTest;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Performance extends BaseTest {
    private final String SEQUENCE3 = "sequence3";
    private final String SEQUENCE4 = "sequence4";
    private final String SEQUENCE5 = "sequence5";

    private final List<String> dataOptions = Arrays.asList(SEQUENCE3, SEQUENCE4, SEQUENCE5);
    private final List<String> rateFuncOptions = Arrays.asList("strictlyEqual", "canSkip", "canDiff", "mixed");

    public void testBestFormat() {

    }

    public void testSoundQualityRequired() {
        //
    }

    @Test
    public void cutFull() {
        this.persistence = new RedisPersistence();
        File[] performanceFiles = new File("src/test/resources/performance/full").listFiles();

        // learning
        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];

            if (!recording.isFile()) {
                continue;
            }

            try {
                AudioFile audioFile = AudioFileIO.read(recording);
                double duration = ((MP3AudioHeader) audioFile.getAudioHeader()).getPreciseTrackLength();

                int limit = 90;
                double cuts = Math.ceil(duration / limit);

                for (int a = 0; a < cuts; a++) {
                    int cutDur = a == cuts - 1 ? (int) (duration % limit) : limit;
                    String cut = getCutFilename(recording.getPath(), a);
                    AudioFileCutter.cutMp3File(a * limit, cutDur, recording.getPath(), cut);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void cutRandom() {
        this.persistence = new RedisPersistence();

        File[] performanceFiles = new File("src/test/resources/performance/full").listFiles();

        // learning
        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];

            if (!recording.isFile()) {
                continue;
            }

            try {
                AudioFile audioFile = AudioFileIO.read(recording);
                double duration = ((MP3AudioHeader) audioFile.getAudioHeader()).getPreciseTrackLength();

                long start = 10 + Math.round(Math.random() * Math.max(duration - 30, 0));
                String cut = getCutFilename(recording.getPath(), null);
                AudioFileCutter.cutMp3File((int) start, 15, recording.getPath(), cut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void transcribeCut() {
        this.persistence = new RedisPersistence();

        File[] performanceFiles = new File("src/test/resources/performance/cut").listFiles();

        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];
            String pieceName = getPieceName(recording.getName());

            if (!recording.isFile() || i == 0) {
                continue;
            }
            System.out.println(pieceName);

            try {
                String midiPath = getMidiPath(pieceName);
                Mp3ToMidiTranscriber.transcribeToMidi(recording.getPath(), midiPath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void transcribeRandom() {
        this.persistence = new RedisPersistence();

        File[] performanceFiles = new File("src/test/resources/performance/random").listFiles();

        // learning
        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];
            String pieceName = getPieceName(recording.getName());

            if (!recording.isFile() || i == 0) {
                continue;
            }
            System.out.println(pieceName);

            try {
                String midiPath = getMidiPath(pieceName);
                Mp3ToMidiTranscriber.transcribeToMidi(recording.getPath(), midiPath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getNameFromCut(String cut) {
        String[] parts = cut.split("\\.");
        return parts[0].substring(0, parts[0].length() - 5);
    }

    private Map<Integer, Integer> getStats(Stream<Map.Entry<List<Integer>, Integer>> storeStream) {
        Map<Integer, Integer> res = new HashMap<>();
        storeStream.forEach(entry -> {
            Integer update = res.getOrDefault(entry.getValue(), 0) + 1;
            res.put(entry.getValue(), update);
        });

        return res;
    }

    /**
     * Compares combinations of data:
     * - sequence 3
     * - sequence 4
     * - sequence 5
     * - rhythm sequence 5
     * <p>
     * With rate functions:
     * - equal only
     * - skip or add allowed
     * - different symbol allowed
     * - allowed multiple skip and diff
     */
    @Test
    void testLearnAndRecognition() {
        this.persistence = new RedisPersistence();
        persistence.clearAll();

        File[] performanceFiles = new File("src/test/resources/performance/mid").listFiles();
        Arrays.sort(performanceFiles);

        // learning
        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];
            String pieceName = getNameFromCut(recording.getName());

            if (!recording.isFile()) {
                continue;
            }

            try {
                List<ChordSeq> tracks = MidiParser.getNotesFromMidi(recording.getPath());
                ChordSeq track = MusicUtils.getNormalizedMelodyTrack(tracks);

                Learner learnEntity = getLearnEntity(pieceName);
                int learnLevel = learnEntity.process(track);

                // learn stats
                // notes in melody retrieved, length
                // num of sequences > 2

                Map<Integer, Integer> stats3 = getStats(learnEntity.getCommonSequences(learnEntity.getStore3(), 2));
                Map<Integer, Integer> stats4 = getStats(learnEntity.getCommonSequences(learnEntity.getStore4(), 2));
                Map<Integer, Integer> stats5 = getStats(learnEntity.getCommonSequences(learnEntity.getStore5(), 2));

//                System.out.println(stats3);
//                System.out.println(stats4);
//                System.out.println(stats5);

                if (i == performanceFiles.length - 1 || !performanceFiles[i + 1].getName().contains(pieceName)) {
                    learnEntity.finishLearn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // performance result
        performanceFiles = new File("src/test/resources/performance/guess").listFiles();
//        Map<String, List<List<PerformanceResult>>> results = new HashMap<>();
        Map<String, PerformanceResult> results = new HashMap<>();
        Arrays.sort(performanceFiles);
final int num = performanceFiles.length;
/*
        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];
            if (!recording.isFile()) {
                continue;
            }

            try {
                List<ChordSeq> tracks = MidiParser.getNotesFromMidi(recording.getPath());
                ChordSeq track = MusicUtils.getNormalizedMelodyTrack(tracks);
                String pieceName = getPieceName(recording.getName());

                Recognizer recognizeEntity = getRecognizeEntity(true);
                recognizeEntity.processInput(track);

                for (int d = 0; d < dataOptions.size(); d++) {
                    String data = dataOptions.get(d);
//                    System.out.println("For data: " + data);

                    for (int r = 0; r < rateFuncOptions.size(); r++) {
                        String rate = rateFuncOptions.get(r);
//                        System.out.println("=== with rate function " + rate);



                        recognizeEntity.setRateFunc(getRateFunc(rate));
//                        System.out.println(pieceName);
                        List<Recognizer.Result> pieceResults = recognizer.recognizeBySequence(getStore(data, recognizeEntity));
//                        System.out.println(pieceResults.stream().map(entry -> (entry.pieceName.equals(pieceName) ?
//                              "+++" : "-") + " " + entry.precision).collect(Collectors.joining("\n")));

                        String resultKey = data + "+" + rate;
                        boolean isEmpty = pieceResults.isEmpty();

                        double topRes = isEmpty ? 0 : pieceResults.get(0).precision;
                        int strictFailure = (!isEmpty && pieceResults.get(0).pieceName.equals(pieceName)) ? 0 : 1;
                        int notStrictFailure = (!isEmpty && pieceResults.stream().limit(5).map(p -> p.pieceName).anyMatch(n -> n.equals(pieceName))) ? 0 : 1;
if (notStrictFailure > 0) {
    System.out.println(pieceResults.stream().limit(5).map(p -> p.pieceName).collect(Collectors.joining(", ")) + " " + pieceName);
}
//                        if (isEmpty || !pieceResults.get(0).pieceName.equals(pieceName) ) {
//                            System.out.println(pieceName + " " + resultKey);
//                            System.out.println(pieceResults.get(1).pieceName.equals(pieceName));
//                        }
                        double avg = isEmpty ? 0 : (pieceResults.stream().skip(1).map(res -> res.precision).reduce(0D, Double::sum) / (pieceResults.size() - 1));
                        System.out.println(avg);
                        double avgDiff = isEmpty ? 0 : (pieceResults.size() > 1 ? (topRes - avg) / topRes:1);
                        double prevDiff = isEmpty ? 0 : (pieceResults.size() > 1 ? (topRes - pieceResults.get(1).precision) / topRes : 1);

PerformanceResult old = results.get(resultKey);
                        if (old == null) {
                            PerformanceResult p = new PerformanceResult();
                            p.previousDiff = prevDiff;
                            p.avgDiff = avgDiff;
                            p.failures = strictFailure;
                            p.strictFailures = notStrictFailure;
                            results.put(resultKey, p);
                        } else {
//                            results.put()
                            old.previousDiff += prevDiff;
                            old.avgDiff += avgDiff;
                            old.failures += strictFailure;
                            old.strictFailures += notStrictFailure;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(results.entrySet().stream().map(res -> res.getKey() + ": failure " +
                ((res.getValue().failures + 0.0) / num * 100) + " " + ((res.getValue().strictFailures + 0.0) / num * 100) + "% avgDiff " + res.getValue().avgDiff / num * 100 + "% prevDiff " +
                res.getValue().previousDiff / num * 100 + "% ").collect(Collectors.joining("\n")));
*/

        int strictFailures = 0;
        int failures = 0;

        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];
            if (!recording.isFile()) {
                continue;
            }

            try {
                List<ChordSeq> tracks = MidiParser.getNotesFromMidi(recording.getPath());
                ChordSeq track = MusicUtils.getNormalizedMelodyTrack(tracks);
                String pieceName = getPieceName(recording.getName());

                Recognizer recognizeEntity = getRecognizeEntity(true);
                List<Recognizer.Result> pieceResults = recognizeEntity.process(track);

                boolean isEmpty = pieceResults.isEmpty();

                double topRes = isEmpty ? 0 : pieceResults.get(0).precision;
                int strictFailure = (!isEmpty && pieceResults.get(0).pieceName.equals(pieceName)) ? 0 : 1;
                int notStrictFailure = (!isEmpty && pieceResults.stream().limit(5).map(p -> p.pieceName).anyMatch(n -> n.equals(pieceName))) ? 0 : 1;
                if (notStrictFailure > 0) {
                    System.out.println(pieceResults.stream().limit(5).map(p -> p.pieceName).collect(Collectors.joining(", ")) + " " + pieceName);
                }
//                        if (isEmpty || !pieceResults.get(0).pieceName.equals(pieceName) ) {
//                            System.out.println(pieceName + " " + resultKey);
//                            System.out.println(pieceResults.get(1).pieceName.equals(pieceName));
//                        }
                double avg = isEmpty ? 0 : (pieceResults.stream().skip(1).map(res -> res.precision).reduce(0D, Double::sum) / (pieceResults.size() - 1));
                System.out.println(avg);
                double avgDiff = isEmpty ? 0 : (pieceResults.size() > 1 ? (topRes - avg) / topRes:1);
                double prevDiff = isEmpty ? 0 : (pieceResults.size() > 1 ? (topRes - pieceResults.get(1).precision) / topRes : 1);

                strictFailures += strictFailure;
                failures += notStrictFailure;
            } catch (Exception e) {
            }
        }

        System.out.println((strictFailures + 0.0) / num * 100 + " " + (failures + 0.0) / num * 100);
    }


    private String getCutFilename(String path, Integer index) {
        String postfix = index == null ? "" : index + "";
        String[] parts = path.split(Pattern.quote(File.separator));
        String filename = getPieceName(parts[parts.length - 1]) + "-tmp" + postfix + ".mp3";

        List<String> newParts = new ArrayList<>(Arrays.asList(parts)).subList(0, parts.length - 2);
        newParts.add("random");
        newParts.add(filename);
        return String.join(File.separator, newParts);
    }

    private String getCutFilename(String path) {
        return getCutFilename(path, null);
    }

    private String getMidiPath(String pieceName) {
        return "src/test/resources/performance/guess/" + pieceName + ".mid";
    }

    private String getPostCutFilename(String path) {
        String[] parts = path.split(Pattern.quote(File.separator));
        String filename = getPieceName(parts[parts.length - 1]) + "-tmp.mp3";

        List<String> newParts = new ArrayList<>(Arrays.asList(parts)).subList(0, parts.length - 2);
        newParts.add("guess");
        newParts.add(filename);
        return String.join(File.separator, newParts);
    }

    private String getPieceName(String filename) {
        String[] parts = filename.split("\\.");
        return parts[0];
    }

    private BiFunction<String, String, Double> getRateFunc(String funcName) {
        switch (funcName) {
            case "strictlyEqual":
                return MusicProcessor::comparePatternsStrict;
            case "canSkip":
                return MusicProcessor::comparePatternsSkip;
            case "canDiff":
                return MusicProcessor::comparePatternsDiff;
            case "mixed":
                return MusicProcessor::comparePatternsMixed;
            default:
                return MusicProcessor::comparePatternsStrict;
        }
    }

    private Map<List<Integer>, Integer> getStore(String desc, Recognizer entity) {
        if (desc.contains("3")) {
            return entity.getStore3();
        } else if (desc.contains("4")) {
            return entity.getStore4();
        } else if (desc.contains("5")) {
            return entity.getStore5();
        }
        return entity.getStore3();
    }

    private static class PerformanceResult {
        public double avgDiff;
        public double previousDiff;
//        public boolean nsed;
        public int failures;
        public int strictFailures;
//        public long searchMs;

//        PerformanceResult(double o, boolean is, long ms) {
//            overlap = o;
//            isRight = is;
//            searchMs = ms;
//        }
    }
}
