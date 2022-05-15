package com.kamennova.lala.endpoint;

import com.kamennova.lala.*;
import com.kamennova.lala.common.ChordSeqFull;
import com.kamennova.lala.persistence.Persistence;
import com.sun.net.httpserver.HttpExchange;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class RequestHandler {
    protected Persistence persistence;

    RequestHandler(Persistence p) {
        persistence = p;
    }

    protected void handleCors(HttpExchange httpExchange) {

    }

    private String getDownloadFilePath() {
        return "src/main/resources/upload.mp3";
    }

    protected String downloadRecording(InputStream body) throws IOException {
        byte[] data = body.readAllBytes();
        String filePath = getDownloadFilePath();
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(data);
        return filePath;
    }

    protected void log(String desc, Object obj) {
        System.out.println(desc);
        System.out.println(obj);
    }

    // downloads file also
    protected Map<String, String> getBodyParameters(InputStream body) throws Exception {
        MyFileUpload upload = new MyFileUpload(body,
                "src/main/resources/upload.mp3");
        return upload.getParameters();
    }

    protected String getErrorString(String message) {
        return "{\"error\": \"" + message + "\"}";
    }

    protected ChordSeqFull getTrackFromAudioInput(String pathToRecording) {
        try {
//            Mp3ToMidiTranscriber.transcribeToMidi(pathToRecording);
            String inputPath = "src/main/resources/Path1.mid";
            List<ChordSeqFull> tracks = MidiParser.getNotesFromMidi(inputPath);
            return LaLa.getNormalizedMelodyTrack(tracks);
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    protected void handleResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
