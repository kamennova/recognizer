package unit;

import com.kamennova.lala.Mp3ToMidiTranscriber;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Mp3ToMidiTranscriberTest {
    @Test
    void testGetCommand() {
        assertThat(Mp3ToMidiTranscriber.getCommand("src/test/test.mp3", "src/test/output.mid"))
                .isEqualTo("omnizart music transcribe src/test/test.mp3 -o src/test/output.mid");
    }

    @Test
    void testTranscriptionSuccess() throws IOException, InterruptedException {
        String output = "src/test/resources/temp/success.mid";
        Mp3ToMidiTranscriber.transcribeToMidi("src/test/resources/performance/cut/bright_eyes-tmp0.mp3", output);
        assertThat(new File(output).exists()).isTrue();
    }

    @Test
    void testTranscription_nonexsitentFile() {
        String output = "src/test/resources/temp/nonexistent.mid";
        Exception e = assertThrows(FileNotFoundException.class, () -> Mp3ToMidiTranscriber.transcribeToMidi("src/test/resources/no.mp3", output));
        assertThat(e.getMessage()).contains("no.mp3");
        assertThat(new File(output).exists()).isFalse();
    }

    @Test
    void testTranscription_wrongFormat() {
        String output = "src/test/resources/temp/wrongformat.mid";
        Exception e = assertThrows(RuntimeException.class, () -> Mp3ToMidiTranscriber.transcribeToMidi("src/test/resources/pic.jpeg", output));
        assertThat(e.getMessage()).contains("Transcribed file format should be mp3");
        assertThat(new File(output).exists()).isFalse();
    }

    @Test
    void testTranscription_silence() throws IOException, InterruptedException {
        String output = "src/test/resources/temp/silence.mid";
        Mp3ToMidiTranscriber.transcribeToMidi("src/test/resources/silence.mp3", output);
        assertThat(new File(output).exists()).isTrue();
    }

    @AfterAll
    static void afterAll() {
        new File("src/test/resources/temp/success.mid").delete();
        new File("src/test/resources/temp/silence.mid").delete();
    }
}
