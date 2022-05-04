package com.kamennova.lala;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * post to /learn
 *      -> {input: MidiFile, name: String}
 *      <- {level: bool} // todo
 *
 * post to /recognize
 *      -> {input: MidiFile}
 *      <- {result: {name: String, precision: 0.1}, isSuccess: bool}
 */
public class Endpoint {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        server.createContext("/learn", new LearnHandler());
        server.createContext("/recognize", new RecognizeHandler());
//        server.setExecutor(threadPoolExecutor);
        server.start();
    }

    private static class MyHandler {
        protected void downloadFile(){

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
            while((line = reader.readLine()) != null) {
                System.out.print(line + "\n");
            }

            proc.waitFor();
        }
    }

    private static class LearnHandler extends MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod();
            String jsonRes = "";

            if (method.equals("POST")) {

                jsonRes
//                requestParamValue = handlePostRequest(httpExchange);
            }

            handleResponse(httpExchange, requestParamValue);
        }

        private String handleGetRequest(HttpExchange httpExchange) {
            return httpExchange.
                    getRequestURI()
                    .toString()
                    .split("\\?")[1]
                    .split("=")[1];
        }

        private void convertToMidi() throws IOException, InterruptedException {
            String command = "ping -c 3 www.google.com";

            Process proc = Runtime.getRuntime().exec(command);

            // Read the output

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = "";
            while((line = reader.readLine()) != null) {
                System.out.print(line + "\n");
            }

            proc.waitFor();
        }

        private void handleResponse(HttpExchange httpExchange, String response) throws IOException {
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static class RecognizeHandler extends MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException, InterruptedException {
            String method = httpExchange.getRequestMethod();
            String jsonRes = "";

            String error = validateRequest(httpExchange);
            if (error == null) {
                downloadFile();
                convertToMidi();

                LaLaRecognize.Result  result = new LaLaRecognize.Result();

                String resString = result.pieceName == null ? "null" :
                        "{\"name\": \"" + result.pieceName + "\", \"precision\": " + result.precision + "}";
                String successString = result.precision > 0 ? "\"true\"" : "\"false\"";
                jsonRes = "{\"result\": " + resString + ", \"isSuccess\": " + successString + "}";
            } else {
                jsonRes = getErrorString(error);
            }

            handleResponse(httpExchange, jsonRes);
        }

        private String validateRequest(HttpExchange httpExchange){
            String method = httpExchange.getRequestMethod();

            if(!method.equals("POST")){
             return "method not accepted";
            }

            return null;
        }

        private String handleGetRequest(HttpExchange httpExchange) {
            return httpExchange.
                    getRequestURI()
                    .toString()
                    .split("\\?")[1]
                    .split("=")[1];
        }

        private void handleResponse(HttpExchange httpExchange, String response) throws IOException {
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
