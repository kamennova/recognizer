import com.kamennova.lala.*;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.RedisPersistence;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Performance extends BaseTest {
    private final String SEQUENCE3 = "sequence3";
    private final String SEQUENCE4 = "sequence4";
    private final String SEQUENCE5 = "sequence5";
    private final String RHYTHM8 = "rhythm8";

    private final List<String> dataOptions = Arrays.asList(SEQUENCE3, SEQUENCE4, SEQUENCE5, RHYTHM8);
    private final List<String> rateFuncOptions = Arrays.asList("strictlyEqual", "canSkip", "canDiff", "mixed");

    public void testBestFormat() {

    }

    public void testSoundQualityRequired() {
        //
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

        File[] performanceFiles = new File("src/test/resources/performance/full").listFiles();

        // learning
        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];
            String pieceName = getPieceName(recording.getName());

            if (!recording.isFile()) {
                continue;
            }

            try {
                String cut = getCutFilename(recording.getPath());
                AudioFileCutter.cutMp3File(0, 50, recording.getPath(), cut);

                String midiPath = getMidiPath(pieceName);
                Mp3ToMidiTranscriber.transcribeToMidi(cut, midiPath);
                List<ChordSeq> tracks = MidiParser.getNotesFromMidi(midiPath);
                ChordSeq track = LaLa.getNormalizedMelodyTrack(tracks);
                System.out.println(track.chords.stream().map(n -> n.iterator().next().interval + " " +
                        n.iterator().next().duration).collect(Collectors.toList()));

                Learner learnEntity = getLearnEntity(recording.getName());
                int learnLevel = learnEntity.process(track);
                LaLa.printRhythm(learnEntity.getRhythm());
                learnEntity.finishLearn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // performance result
        Map<String, List<List<PerformanceResult>>> results = new HashMap<>();
/*
        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];
            if (!recording.isFile()) {
                continue;
            }

            try {
                String cutFileName = getCutFilename(recording.getAbsolutePath());
                int startSecond = (int) Math.round(Math.random() * 90);
                System.out.println(startSecond);
//                AudioFileCutter.cutMp3File(startSecond, 15, recording.getAbsolutePath(), cutFileName);

                String midiPath = getMidiPath(getPieceName(recording.getName()));
                //                Mp3ToMidiTranscriber.transcribeToMidi(cut, midiPath);
                List<ChordSeqFull> tracks = MidiParser.getNotesFromMidi(midiPath);
                ChordSeqFull track = LaLa.getNormalizedMelodyTrack(tracks);

                Recognizer recognizeEntity = getRecognizeEntity(false);
                recognizeEntity.processInput(track);

                for (int d = 0; d < dataOptions.size(); d++) {
                    String data = dataOptions.get(d);
                    System.out.println("For data: " + data);

                    for (int r = 0; r < rateFuncOptions.size(); r++) {
                        String rate = rateFuncOptions.get(r);
                        System.out.println("with rate function " + rate);

                        recognizeEntity.setRateFunc(getRateFunc(rate));
                        List<Recognizer.Result> pieceResults = new ArrayList<>();

                        if (data.equals(RHYTHM8)) {
                            pieceResults = recognizer.recognizeByRhythm();
                        } else {
                            pieceResults = recognizer.recognizeBySequence(getStore(data, recognizeEntity));
                        }
System.out.println(pieceResults.get(0).pieceName + " " + pieceResults.get(0).precision);
                        List<PerformanceResult> converted = pieceResults.stream()
                                .map(res -> new PerformanceResult(res.precision,
                                        res.pieceName.equals(recording.getName()),
                                        100))
                                .collect(Collectors.toList());
                        String resultKey = data + "+" + rate;

                        List<List<PerformanceResult>> old = results.get(resultKey);
                        if (old == null) {
                            results.put(resultKey, Arrays.asList(converted));
                        } else {
                            old.add(converted);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
*/
        System.out.println(results);
    }

    private String getCutFilename(String path) {
        String[] parts = path.split(Pattern.quote(File.separator));
        String filename = getPieceName(parts[parts.length - 1]) + "-tmp.mp3";

        List<String> newParts = new ArrayList<>(Arrays.asList(parts)).subList(0, parts.length - 2);
        newParts.add("cut");
        newParts.add(filename);
        return String.join(File.separator, newParts);
    }

    private String getMidiPath(String pieceName) {
        return "src/test/resources/performance/mid/" + pieceName + ".mid";
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

    private BiFunction<String, String, Integer> getRateFunc(String funcName) {
        switch (funcName) {
            case "strictlyEqual":
                return LaLa::comparePatternsStrict;
            case "canSkip":
                return LaLa::comparePatternsSkip;
            case "canDiff":
                return LaLa::comparePatternsDiff;
            case "mixed":
                return LaLa::comparePatternsMixed;
            default:
                return LaLa::comparePatternsStrict;
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
        public float overlap;
        public boolean isRight;
        public long searchMs;

        PerformanceResult(float o, boolean is, long ms) {
            overlap = o;
            isRight = is;
            searchMs = ms;
        }
    }
}
