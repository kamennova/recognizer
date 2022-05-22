import com.kamennova.lala.LaLa;
import com.kamennova.lala.Learner;
import com.kamennova.lala.MidiParser;
import com.kamennova.lala.common.ChordSeqFull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LearnerTests extends BaseTest {

    @Test
    void testLearnLevelNotEnough() throws Exception {
        String inputPath = "src/main/resources/samples/geese.mid";
        List<ChordSeqFull> tracks = MidiParser.getNotesFromMidi(inputPath);
        ChordSeqFull track = LaLa.getNormalizedMelodyTrack(tracks);

        Learner learnEntity = getLearnEntity("geese");

        int learnLevel = learnEntity.process(track);
        assertEquals(learnLevel, 0);
    }

    @Test
    void testLearnSuccessful() throws Exception {
        String inputPath = "src/main/resources/samples/geese.mid";
        List<ChordSeqFull> tracks = MidiParser.getNotesFromMidi(inputPath);
        ChordSeqFull track = LaLa.getNormalizedMelodyTrack(tracks);

        Learner learnEntity = getLearnEntity("geese");

        int learnLevel = learnEntity.process(track);
        assertEquals(learnLevel, 0);
    }
}
