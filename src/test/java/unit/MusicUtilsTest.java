package unit;

import com.kamennova.lala.MusicUtils;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.common.Note;
import org.junit.jupiter.api.Test;

import java.util.Set;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class MusicUtilsTest {
    @Test
    void testGetAvgKey() {
        ChordSeq test1 = new ChordSeq(Arrays.asList(
                Set.of(new Note(1, 0), new Note(10, 0)),
                Set.of(new Note(0, 0)),
                Set.of(new Note(5, 0)),
                Set.of(new Note(10, 0)),
                Set.of(new Note(12, 0))));
        // 5.5 + 0 + 5 + 10 + 12 = 32.5 / 5 = 6.5
        assertThat(MusicUtils.getAvgKey(test1)).isEqualTo(6.5);
    }

    @Test
    void testGetMelodyTrack() {
        ChordSeq leftHand = new ChordSeq(Arrays.asList(
                Set.of(new Note(1, 0)),
                Set.of(new Note(0, 0)),
                Set.of(new Note(5, 0)),
                Set.of(new Note(10, 0)),
                Set.of(new Note(12, 0))));

        ChordSeq rightHand = new ChordSeq(Arrays.asList(
                Set.of(new Note(12, 0)),
                Set.of(new Note(8, 0)),
                Set.of(new Note(22, 0)),
                Set.of(new Note(20, 0)),
                Set.of(new Note(0, 0))));

        assertThat(MusicUtils.getMelodyTrack(Arrays.asList(leftHand, rightHand))).isEqualTo(rightHand);
    }

    @Test
    void testSeparateMelodyPart() {
        ChordSeq mixedTrack = new ChordSeq(Arrays.asList(
                Set.of(new Note(80, 0)),
                Set.of(new Note(12, 0)),
                Set.of(new Note(5, 0)),
                Set.of(new Note(10, 0)),
                Set.of(new Note(12, 0)))); // todo real melodies

    }

    @Test
    void testNormalizeTrack() {
        ChordSeq track = new ChordSeq(Arrays.asList(
                Set.of(new Note(17, 0), new Note(10, 0)),
                Set.of(new Note(80, 0)),
                Set.of(new Note(64, 0), new Note(68, 0)),
                Set.of(new Note(48, 0)),
                Set.of(new Note(49, 0))));

        assertThat(MusicUtils.normalizeTrack(track).chords.stream().flatMap(notes -> notes.stream().map(n -> n.interval)).collect(Collectors.toList()))
                .containsAll(Arrays.asList(
                Set.of(new Note(5, 0)),
                Set.of(new Note(8, 0)),
                Set.of(new Note(8, 0)),
                Set.of(new Note(0, 0)),
                Set.of(new Note(1, 0))
        ).stream().map(notes -> notes.iterator().next().interval).collect(Collectors.toList()));
    }
}
