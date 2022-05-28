import com.kamennova.lala.*;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.RedisPersistence;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.junit.jupiter.api.Test;

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
        Map<String, List<List<PerformanceResult>>> results = new HashMap<>();
        Arrays.sort(performanceFiles);

        for (int i = 0; i < performanceFiles.length; i++) {
            File recording = performanceFiles[i];
            if (!recording.isFile() || i > 0) {
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
                    System.out.println("For data: " + data);

                    for (int r = 0; r < rateFuncOptions.size(); r++) {
                        String rate = rateFuncOptions.get(r);
                        System.out.println("=== with rate function " + rate);

                        recognizeEntity.setRateFunc(getRateFunc(rate));
                        System.out.println(pieceName);
                        List<Recognizer.Result> pieceResults = recognizer.recognizeBySequence(getStore(data, recognizeEntity));
                        System.out.println(pieceResults.stream().map(entry -> (entry.pieceName.equals(pieceName) ?
                              "+++" : "-") + " " + entry.precision).collect(Collectors.joining("\n")));

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

    private BiFunction<String, String, Integer> getRateFunc(String funcName) {
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
