package system;

import org.apache.http.client.methods.*;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class APITests {
    public static String API_PATH = "http://localhost:8001/api/";

    public CloseableHttpClient client = HttpClientBuilder.create().build();

    @Test
    public void testEndpointPathAndMethodValidation() throws IOException {
        CloseableHttpResponse responseWrongPath = client.execute( new HttpGet(API_PATH + "alala"));
        CloseableHttpResponse responseWrongMethod = client.execute(new HttpPatch(API_PATH + "learn"));

        assertThat(responseWrongPath.getStatusLine().getStatusCode()).isEqualTo(404);
        assertThat(responseWrongMethod.getStatusLine().getStatusCode()).isEqualTo(400);
    }

    @Test
    public void testLearnPieceNameValidation() throws IOException {
        CloseableHttpResponse res = client.execute(new HttpPost(API_PATH + "learn"));
        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(400);
        assertThat(EntityUtils.toString(res.getEntity())).contains("Piece name is required");

        res = client.execute(new HttpPost(API_PATH + "learn?pieceName=a"));
        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(400);
        assertThat(EntityUtils.toString(res.getEntity())).contains("Piece name should be at least 2 symbols long");

        res = client.execute(new HttpPost(API_PATH + "learn?pieceName=valid_pieceName"));
        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testTeachNewMusicPiece_success() throws IOException {
        HttpPost req = new HttpPost(API_PATH + "learn?pieceName=aaa");
        attachFile(req, "src/test/resources/performance/cut/bright_eyes-tmp0.mp3");

        CloseableHttpResponse res = client.execute(req);
        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);
        assertThat(EntityUtils.toString(res.getEntity())).contains("\"level\": 10");
    }

    @Test
    public void testTeachExistingMusicPiece_success() throws IOException {  // todo
        HttpPost req = new HttpPost(API_PATH + "learn?pieceName=bright_eyes");
        attachFile(req, "src/test/resources/performance/cut/bright_eyes-tmp1.mp3");

        CloseableHttpResponse res = client.execute(req);
        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);
        assertThat(EntityUtils.toString(res.getEntity())).contains("\"level\": 10");
    }

    @Test
    public void testTeachMusicPiece_noSounds() throws IOException {
        HttpPost req = new HttpPost(API_PATH + "learn?pieceName=no_sounds");
        attachFile(req, "src/test/resources/silence.mp3");

        CloseableHttpResponse res = client.execute(req);
        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);
        String message = EntityUtils.toString(res.getEntity());
        assertThat(message).contains("\"isSuccess\": false");
        assertThat(message).contains("\"message\": \"Not enough sounds recorded, please check your microphone\"");
    }

    @Test
    public void testRecognizeMusicPiece_success() throws IOException { // todo
        HttpPost req = new HttpPost(API_PATH + "recognize");
        attachFile(req, "src/test/resources/performance/random/bright_eyes-tmp.mp3");
        CloseableHttpResponse res = client.execute(req);

        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);
        String message = EntityUtils.toString(res.getEntity());
        assertThat(message).contains("\"isSuccess\": true");
        assertThat(message).contains("\"message\": \"Not enough sounds recorded, please check your microphone\"");
    }

    @Test
    public void testRecognizeMusicPiece_notEnough() throws IOException {
        HttpPost req = new HttpPost(API_PATH + "recognize");
        attachFile(req, "src/test/resources/silence.mp3");
        CloseableHttpResponse res = client.execute(req);

        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);
        String message = EntityUtils.toString(res.getEntity());
        System.out.println(message);
        assertThat(message).contains("\"isSuccess\": false");
        assertThat(message).contains("\"message\": \"Not enough sounds recorded, please check your microphone\"");
    }

    @Test
    public void testTeachMusicPiece_wrongCorrectionValidation() throws IOException {
        HttpPost req = new HttpPost(API_PATH + "recognize");
        attachFile(req, "src/test/resources/performance/cut/bright_eyes-tmp3.mp3");
        CloseableHttpResponse res = client.execute(req);

        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);

        res = client.execute(new HttpPost(API_PATH + "recognize/correct"));
        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(400);
        assertThat(EntityUtils.toString(res.getEntity())).contains("Piece name is required");

        res = client.execute(new HttpPost(API_PATH + "recognize/correct?pieceName=a"));
        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(400);
        assertThat(EntityUtils.toString(res.getEntity())).contains("Piece name should be at least 2 symbols long");

        res = client.execute(new HttpPost(API_PATH + "recognize/correct?pieceName=bright_eyes"));
        assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);

        // todo check coorection
    }

    private void attachFile(HttpPost req, String path) {
        req.addHeader("content-type", "application/x-www-form-urlencoded;charset=utf-8");

        FileEntity entity = new FileEntity(new File(path));
        req.setEntity(entity);
    }

    @AfterAll
    public void afterAll() throws IOException {
        client.close();
    }
}
