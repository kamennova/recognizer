package com.kamennova.lala.endpoint;

import com.kamennova.lala.persistence.Persistence;
import com.kamennova.lala.persistence.RedisPersistence;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * post to /learn
 * -> {recording: recording mp3 file, name: String}
 * <- {level: number} // todo
 * <p>
 * post to /recognize
 * -> {recording: mp3 File, mayBeNew: bool}
 * <- {result: {name: String, precision: 0.1}, isSuccess: bool}
 */
public class APIEndpoint {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        Persistence persistence = new RedisPersistence();
        server.createContext("/learn", new LearnHandler(persistence));
        server.createContext("/recognize", new RecognizeHandler(persistence));
        server.start();
    }
}
