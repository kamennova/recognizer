package unit;

import com.kamennova.lala.MidiParser;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.common.Note;
import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MidiParserTest {
    @Test
    void testMidiParserStaccato() throws Exception {
        String inputPath = "src/main/resources/samples/when_the_love.mid";
        ChordSeq track = MidiParser.getNotesFromMidiStaccato(inputPath).get(0); // contains only 1 track

        List<List<Note>> expectedChords = Arrays.asList(
                Arrays.asList(new Note(71, 0)),
//                Arrays.asList(), // todo with pause?
                Arrays.asList(new Note(72, 0), new Note(84, 0), new Note(91, 0)),
                Arrays.asList(new Note(67, 0), new Note(86, 0)),
                Arrays.asList(new Note(91, 0), new Note(79, 0)),
                Arrays.asList(new Note(72, 0)),
                Arrays.asList(new Note(77, 0), new Note(89, 0)),
                Arrays.asList(new Note(91, 0), new Note(79, 0), new Note(84, 0)),
                Arrays.asList(new Note(65, 0), new Note(89, 0), new Note(84, 0)),
                Arrays.asList(new Note(90, 0)),
                Arrays.asList(new Note(67, 0), new Note(86, 0), new Note(74, 0))
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
        ChordSeq track = MidiParser.getNotesFromMidiStaccato(inputPath).get(0); // contains only 1 track

        List<List<Note>> expectedChords = Arrays.asList();

        List<List<String>> actualChords = track.chords.stream()
                .map(chord -> chord.stream().map(note -> note.interval + " " + note.duration).collect(Collectors.toList()))
                .collect(Collectors.toList());

        System.out.println(actualChords);
        IntStream.range(0, expectedChords.size()).forEach(i ->
                assertThat(expectedChords.get(i)).containsExactlyInAnyOrderElementsOf(track.chords.get(i))
        );
    }

    @Test
    void testIsPlayedSimultaneously(){
        assertThat(MidiParser.arePlayedSimultaneously(1, 1)).isTrue();
        assertThat(MidiParser.arePlayedSimultaneously(1, 14)).isTrue();
        assertThat(MidiParser.arePlayedSimultaneously(1, 22)).isFalse();
    }

    @Test
    void testGetNotesFromMidi_nonexistent(){
        Exception e = assertThrows(FileNotFoundException.class, () -> MidiParser.getNotesFromMidi("nonexistent"));
        assertThat(e.getMessage()).contains("nonexistent");
    }

    @Test
    void testGetNotesFromMidi_nonMidi(){
        Exception e = assertThrows(InvalidMidiDataException.class, () -> MidiParser.getNotesFromMidi("src/test/resources/performance/full/greensleeves.mp3"));
        assertThat(e.getMessage()).contains("could not get sequence from file");
    }

    @Test
    void getGetNotesFromMidi_emptyFile() throws Exception {
        List<ChordSeq> seq = MidiParser.getNotesFromMidi("src/test/resources/empty.mid");
        assertThat(seq.isEmpty());
    }
}
