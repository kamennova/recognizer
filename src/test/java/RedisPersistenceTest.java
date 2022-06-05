import com.kamennova.lala.MusicProcessor;
import com.kamennova.lala.persistence.RedisPersistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisPersistenceTest {
    RedisPersistence persistence = new RedisPersistence();

    @BeforeAll
    void beforeClass() {
        persistence.clearAll();
    }

    @Test
    void testAddPiece_success() {
        persistence.addPiece("new_piece_123");
        assertThat(persistence.getJedis().smembers(RedisPersistence.PIECE_SET)).contains("new_piece_123");
        assertThat(persistence.pieceExists("new_piece_123")).isTrue();
    }

    @Test
    void testAddPiece_existing() {
        persistence.addPiece("existing_piece");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            persistence.addPiece("existing_piece");
        });
        assertThat(exception.getMessage()).contains("Piece with name existing_piece already exists");
    }

    @Test
    void testAddPattern_success() {
        String pattern = "ABCD";
        String pieceName = "new_piece_add_pattern";
        persistence.addPiece(pieceName);
        persistence.addPattern(pieceName, pattern);
        assertThat(persistence.findPiecesByNotePatterns(Collections.singletonList(pattern), (p1, p2) -> p2.equals(p1) ? 1D : 0D))
                .containsKey(pieceName);
    }

    @Test
    void testAddPattern_nonexistentPiece() {
        String pattern = "ABCD";
        String pieceName = "nonexistentpiece";
        Exception e = assertThrows(RuntimeException.class, () -> persistence.addPattern(pieceName, pattern));

        assertThat(e.getMessage()).contains("Piece with name nonexistentpiece does not exist");
        assertThat(persistence.findPiecesByNotePatterns(Collections.singletonList(pattern), (p1, p2) -> p2.equals(p1) ? 1D : 0D))
                .doesNotContainKey(pieceName);
    }

    @Test
    void testFindPieceByPattern_none() {
        String pattern = "1234";
        assertThat(persistence.findPiecesByNotePatterns(Collections.singletonList(pattern), MusicProcessor::comparePatternsStrict))
                .isEmpty();
    }

    @Test
    void testFindPiecesByPatterns_success() {
        persistence.clearAll();
        persistence.addPiece("piece_test_111");
        persistence.addPiece("piece_test_222");

        persistence.addPattern("piece_test_111", "ABCD");
        persistence.addPattern("piece_test_111", "ACCC");
        persistence.addPattern("piece_test_222", "ABCD");
        persistence.addPattern("piece_test_222", "ABBB");

        Map<String, Double> pieces = persistence.findPiecesByNotePatterns(Arrays.asList("ABCD", "ACCC", "ABBB"), MusicProcessor::comparePatternsStrict);
        assertThat(pieces).containsKey("piece_test_111");
        assertThat(pieces).containsKey("piece_test_222");
        assertThat(pieces.get("piece_test_111")).isEqualTo(2);
        assertThat(pieces.get("piece_test_222")).isEqualTo(2);
    }

    @Test
    void testClearAll() {
        persistence.addPiece("alalala");
        persistence.addPiece("alalala2");
        assertThat(persistence.getJedis().smembers(RedisPersistence.PIECE_SET).size()).isGreaterThan(0);
        persistence.clearAll();
        assertThat(persistence.getJedis().smembers(RedisPersistence.PIECE_SET).size()).isEqualTo(0);
    }

    @AfterAll
    void afterAll() {
        persistence.clearAll();
    }
}
