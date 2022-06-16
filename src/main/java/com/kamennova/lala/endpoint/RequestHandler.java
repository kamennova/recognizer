package com.kamennova.lala.endpoint;

import com.kamennova.lala.MidiParser;
import com.kamennova.lala.Mp3ToMidiTranscriber;
import com.kamennova.lala.MusicUtils;
import com.kamennova.lala.common.ChordSeq;
import com.kamennova.lala.persistence.Persistence;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandler {
    protected Persistence persistence;
    protected static final String PIECE_NAME_PROPERTY = "pieceName";

    RequestHandler(Persistence p) {
        persistence = p;
    }

    protected void handleCors(HttpExchange httpExchange) {

    }

    protected String validatePieceName(String pieceName) {
        if (pieceName == null) {
            return "Piece name is required";
        } else if (pieceName.length() < 2) {
            return "Piece name should be at least 2 symbols long";
        }

        return null;
    }

    private static int getFileIndex(String path) {
        File[] uploadedFiles = new File(path).listFiles();
        Arrays.sort(uploadedFiles);
        int lastIndex = -1;
        if (uploadedFiles.length > 0) {
            String[] nameParts = uploadedFiles[uploadedFiles.length - 1].getName().split("\\.");
            String name = nameParts[nameParts.length - 2];
            lastIndex = Integer.parseInt(name.charAt(name.length() - 1) + "");
        }

        return lastIndex + 1;
    }

    public static String getDownloadFilePath() {
        int index = getFileIndex("src/main/resources/upload");
        return String.format("src/main/resources/upload/recording%s.mp3", index);
    }

    private String getMidiFilePath() {
        int index = getFileIndex("src/main/resources/transcribed");
        return String.format("src/main/resources/transcribed/recording%s.mid", index);
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
        return new BodyParser(body).getParameters();
    }


    public static Map<String, String> getURIParameters(HttpExchange http) {
        String query = http.getRequestURI().getQuery();
        Map<String, String> map = new HashMap<>();

        if (query == null) {
            return map;
        }
        String[] params = query.split("&");

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    protected String getErrorString(String message) {
        return "{\"error\": \"" + message + "\"}";
    }

    protected ChordSeq getTrackFromAudioInput(HttpExchange httpExchange) throws Exception {
        String pathToFile = downloadRecording(httpExchange.getRequestBody());
        String midiPath = getMidiFilePath();
        Mp3ToMidiTranscriber.transcribeToMidi(pathToFile, midiPath);
        List<ChordSeq> tracks = MidiParser.getNotesFromMidi(midiPath);
        return MusicUtils.getNormalizedMelodyTrack(tracks);
    }

    protected void handleResponse(HttpExchange httpExchange, int code, String response) throws IOException {
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(code, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
