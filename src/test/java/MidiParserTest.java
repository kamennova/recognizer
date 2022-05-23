import com.kamennova.lala.MidiParser;
import com.kamennova.lala.common.ChordSeqFull;
import com.kamennova.lala.common.RNote;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MidiParserTest {
    @Test
    void testMidiParserStaccato() throws Exception {
        String inputPath = "src/main/resources/samples/when_the_love.mid";
        ChordSeqFull track = MidiParser.getNotesFromMidiStaccato(inputPath).get(0); // contains only 1 track

        List<List<RNote>> expectedChords = Arrays.asList(
                Arrays.asList(new RNote(71, 0)),
//                Arrays.asList(), // todo with pause?
                Arrays.asList(new RNote(72, 0), new RNote(84, 0), new RNote(91, 0)),
                Arrays.asList(new RNote(67, 0), new RNote(86, 0)),
                Arrays.asList(new RNote(91, 0), new RNote(79, 0)),
                Arrays.asList(new RNote(72, 0)),
                Arrays.asList(new RNote(77, 0), new RNote(89, 0)),
                Arrays.asList(new RNote(91, 0), new RNote(79, 0), new RNote(84, 0)),
                Arrays.asList(new RNote(65, 0), new RNote(89, 0), new RNote(84, 0)),
                Arrays.asList(new RNote(90, 0)),
                Arrays.asList(new RNote(67, 0), new RNote(86, 0), new RNote(74, 0))
        );

        List<List<Short>> actualKeys = track.chords.stream()
                .map(chord -> chord.stream().map(note -> note.interval).collect(Collectors.toList()))
                .collect(Collectors.toList());

        List<List<Short>> expectedKeys = expectedChords.stream()
                .map(chord -> chord.stream().map(note -> note.interval).collect(Collectors.toList()))
                .collect(Collectors.toList());

        IntStream.range(0, expectedChords.size()).forEach(i ->
                assertThat(expectedKeys.get(i))
                        .containsExactlyInAnyOrderElementsOf(actualKeys.get(i))
        );
    }

    @Test
    void testMidiParserStaccato2() throws Exception {
        String inputPath = "src/main/resources/samples/one_summer_day1.mid";
        ChordSeqFull track = MidiParser.getNotesFromMidiStaccato(inputPath).get(0); // contains only 1 track

        List<List<RNote>> expectedChords = Arrays.asList();

        List<List<String>> actualChords = track.chords.stream()
                .map(chord -> chord.stream().map(note -> note.interval + " " + note.duration).collect(Collectors.toList()))
                .collect(Collectors.toList());

        System.out.println(actualChords);
        IntStream.range(0, expectedChords.size()).forEach(i ->
                assertThat(expectedChords.get(i)).containsExactlyInAnyOrderElementsOf(track.chords.get(i))
        );

    }
}
