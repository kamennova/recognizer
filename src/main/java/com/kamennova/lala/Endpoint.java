package com.kamennova.lala;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * post to /learn
 * -> {input: MidiFile, name: String}
 * <- {level: number} // todo
 * <p>
 * post to /recognize
 * -> {input: MidiFile}
 * <- {result: {name: String, precision: 0.1}, isSuccess: bool}
 */
public class Endpoint {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        Persistence persistence = new RedisPersistence();
        server.createContext("/learn", new LearnHandler(persistence));
        server.createContext("/recognize", new RecognizeHandler(persistence));
//        server.setExecutor(threadPoolExecutor);
        server.start();
    }

    private static class MyHandler {
        protected Persistence persistence;

        MyHandler(Persistence p){
            persistence = p;
        }

        protected void downloadFile() {

        }

        protected String getErrorString(String message) {
            return "{\"error\": \"" + message + "\"}";
        }

        protected void convertToMidi() throws IOException, InterruptedException {
            String command = "ping -c 3 www.google.com";

            Process proc = Runtime.getRuntime().exec(command);

            // Read the output

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.print(line + "\n");
            }

            proc.waitFor();
        }

        protected void handleResponse(HttpExchange httpExchange, String response) throws IOException {
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static class LearnHandler extends MyHandler implements HttpHandler {
        LaLaLearn currLearn;

        LearnHandler(Persistence p) {
            super(p);
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String jsonRes = "";
//            httpExchange.getRequestBody().

            String error = validateRequest(httpExchange);
            String pieceName = "lala"; // todo

            if (error == null) {
                downloadFile();
                try {
                    convertToMidi();
                    // todo
                    String inputPath = "src/main/resources/Path1.mid";

                    LaLa.ChordSeqFull track = LaLa.getNormalizedTrack(inputPath);

                    int learnLevel = getLearnEntity(pieceName).process(track);

                    jsonRes = "{\"level\": " + learnLevel + "}";
                    getLearnEntity(pieceName).finishLearn();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            } else {
                jsonRes = getErrorString(error);
            }

            handleResponse(httpExchange, jsonRes);
        }

        private LaLaLearn getLearnEntity(String pieceName) {
            if (this.currLearn == null) {
                System.out.println("create");
                this.currLearn = new LaLaLearn(pieceName, persistence);
            } else if (!this.currLearn.getPieceName().equals(pieceName)) {
                System.out.println("new");
                this.currLearn.finishLearn();
                this.currLearn.clear();
                this.currLearn.setNewPiece(pieceName); // todo new entity ??
            } else {
                System.out.println("old");
            }

            return this.currLearn;
        }

        private String validateRequest(HttpExchange httpExchange) {
            String method = httpExchange.getRequestMethod();

//            if (!method.equals("POST")) {
//                return "method not accepted";
//            }

            return null;
        }
    }

    private static class RecognizeHandler extends MyHandler implements HttpHandler {
        LaLaRecognize recognizer;
        LaLaRecognize prevRecognizer;

        RecognizeHandler(Persistence p){
            super(p);
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String jsonRes = "";

            String error = validateRequest(httpExchange);
            if (error == null) {
                downloadFile();
                try {
                    convertToMidi();
                    // todo
                    String inputPath = "src/main/resources/Path1.mid";

                    LaLa.ChordSeqFull track = LaLa.getNormalizedTrack(inputPath);
                    updateRecognizeEntity(false);

                    LaLaRecognize.Result result = this.recognizer.process(track);

                    String resString = result.pieceName == null ? "null" :
                            "{\"name\": \"" + result.pieceName + "\", \"precision\": " + result.precision + "}";
                    String successString = result.precision > 0 ? "\"true\"" : "\"false\"";
                    jsonRes = "{\"result\": " + resString + ", \"isSuccess\": " + successString + "}";

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                jsonRes = getErrorString(error);
            }

            handleResponse(httpExchange, jsonRes);
        }

        private void updateRecognizeEntity(boolean mightNeedNew) {
            if (this.recognizer == null) {
                System.out.println("create recognizer");
                this.recognizer = new LaLaRecognize(persistence);
            } else if (mightNeedNew) {
                prevRecognizer = this.recognizer;
                this.recognizer = new LaLaRecognize(persistence);
            }
        }

        private String validateRequest(HttpExchange httpExchange) {
            String method = httpExchange.getRequestMethod();

//            if (!method.equals("POST")) {
//                return "method not accepted";
//            }

            return null;
        }
    }
}
