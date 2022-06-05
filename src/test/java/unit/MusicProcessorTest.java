package unit;

import com.kamennova.lala.*;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.common.Note;
import com.kamennova.lala.common.NoteSeq;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MusicProcessorTest extends BaseTest {
    @Test
    void testSequencesCleanMidi() throws Exception {
        String inputPath = "src/main/resources/lmlyd.mid";
        List<ChordSeq> tracks = MidiParser.getNotesFromMidi(inputPath);
        ChordSeq track = MusicUtils.getNormalizedMelodyTrack(tracks);

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
        ChordSeq track = MusicUtils.getNormalizedMelodyTrack(tracks);

        Learner learnEntity = getLearnEntity("when_the_love");
        learnEntity.process(track);

        List<List<Integer>> expectedSequences = Arrays.asList(Arrays.asList(7, 5, 7));

        List<List<Integer>> seqToPersist = learnEntity.getSequencesToPersist(learnEntity.getStore3());
        assertThat(seqToPersist).containsAll(expectedSequences);
    }

    @Test
    void testGetPatternString() {
        assertThat(MusicProcessor.getPatternString(Arrays.asList())).isEqualTo("");
        assertThat(MusicProcessor.getPatternString(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)))
                .isEqualTo("cCdDefFgGaAb");
    }

    @Test
    void testComparePatternStrict() {
        assertThat(MusicProcessor.comparePatternsStrict("abcd", "abcd")).isEqualTo(1);
        assertThat(MusicProcessor.comparePatternsStrict("aAbBcd", "aAbBcd")).isEqualTo(1);
        assertThat(MusicProcessor.comparePatternsStrict("", "")).isEqualTo(1);
        assertThat(MusicProcessor.comparePatternsStrict("ABC", "abc")).isEqualTo(0);
        assertThat(MusicProcessor.comparePatternsStrict("abc", "cde")).isEqualTo(0);
    }

    @Test
    void testComparePatternDiff() {

    }

    @Test
    public void testComparePatternSkip() {
        Assertions.assertEquals(MusicProcessor.comparePatternsSkip("abcd", "abcd"), 4);
        Assertions.assertEquals(MusicProcessor.comparePatternsSkip("abcd", "aesd"), 0);
        Assertions.assertEquals(MusicProcessor.comparePatternsSkip("abcd", "acde"), 2);
        Assertions.assertEquals(MusicProcessor.comparePatternsSkip("acdf", "abcd"), 2);
        Assertions.assertEquals(MusicProcessor.comparePatternsSkip("abcd", "abcd"), 4);
    }

    @Test
    void testComparePatternMixed() {

    }

    @Test
    void testAllNotesSame() {
        assertThat(MusicProcessor.areAllNotesSame(new NoteSeq(Arrays.asList(new Note(0, 0), new Note(2, 4),
                new Note(2, 5), new Note(7, 0))))).isFalse();

        assertThat(MusicProcessor.areAllNotesSame(new NoteSeq(Arrays.asList()))).isFalse();

        assertThat(MusicProcessor.areAllNotesSame(new NoteSeq(Arrays.asList(new Note(2, 0), new Note(2, 4),
                new Note(2, 5), new Note(2, 3))))).isTrue();
    }

    @Test
    void testIsNoteSemi() {
        assertThat(MusicProcessor.isNoteSemi(0)).isFalse();
        assertThat(MusicProcessor.isNoteSemi(1)).isTrue();
        assertThat(MusicProcessor.isNoteSemi(2)).isFalse();
        assertThat(MusicProcessor.isNoteSemi(3)).isTrue();
        assertThat(MusicProcessor.isNoteSemi(4)).isFalse();
        assertThat(MusicProcessor.isNoteSemi(5)).isFalse();
        assertThat(MusicProcessor.isNoteSemi(6)).isTrue();
        assertThat(MusicProcessor.isNoteSemi(7)).isFalse();
        assertThat(MusicProcessor.isNoteSemi(8)).isTrue();
        assertThat(MusicProcessor.isNoteSemi(9)).isFalse();
        assertThat(MusicProcessor.isNoteSemi(10)).isTrue();
        assertThat(MusicProcessor.isNoteSemi(11)).isFalse();
        assertThat(MusicProcessor.isNoteSemi(12)).isFalse();
    }

    @Test
    void testGetTonalityScore() {
        assertThat(MusicProcessor.getTonalityScore(Constants.TONALITIES.get(1), Arrays.asList())); // todo
    }

    @Test
    void testGetTonality() {

    }
}
