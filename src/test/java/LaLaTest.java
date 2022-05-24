import com.kamennova.lala.LaLa;
import com.kamennova.lala.Learner;
import com.kamennova.lala.MidiParser;
import com.kamennova.lala.common.ChordSeq;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
    • normalization - meshed track — separate left from right hand
    • normalization - choose melody track
    • getTonality???
    • getSequences — contains …
    • rhythm — getRhythm
    • rhythm — getRSequences
 */
public class LaLaTest extends BaseTest {
    @Test
    void getSequencesTest() {

    }


    @Test
    void testSequencesCleanMidi() throws Exception {
        String inputPath = "src/main/resources/lmlyd.mid";
        List<ChordSeq> tracks = MidiParser.getNotesFromMidi(inputPath);
        ChordSeq track = LaLa.getNormalizedMelodyTrack(tracks);

        Learner learnEntity = getLearnEntity("lmlyd");
        learnEntity.process(track);

        List<List<Integer>> expectedSequences = Arrays.asList(
                Arrays.asList(8, 3, 8),
                Arrays.asList(0, 3, 3),
                Arrays.asList(10, 10, 8)
        );

        List<List<Integer>> seqToPersist = learnEntity.getSequencesToPersist(learnEntity.getStore3());
        assertThat(seqToPersist).containsAll(expectedSequences);
    }

    @Test
    void testSequencesTranscribedMidi() throws Exception {
        String inputPath = "src/main/resources/samples/when_the_love.mid";
        List<ChordSeq> tracks = MidiParser.getNotesFromMidiStaccato(inputPath);
        ChordSeq track = LaLa.getNormalizedMelodyTrack(tracks);

        Learner learnEntity = getLearnEntity("when_the_love");
        learnEntity.process(track);

        List<List<Integer>> expectedSequences = Arrays.asList(Arrays.asList(7, 5, 7));

        List<List<Integer>> seqToPersist = learnEntity.getSequencesToPersist(learnEntity.getStore3());
        assertThat(seqToPersist).containsAll(expectedSequences);
    }


    private void logHigh() {
//        System.out.println(track.chords.stream().map(ch ->
//                "[" + ch.stream().map(n -> String.valueOf(n.interval - 24))
//                        .collect(Collectors.joining(",")) + "]").collect(Collectors.toList()));
    }
}
