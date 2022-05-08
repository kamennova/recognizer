package com.kamennova.lala;

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

public class RedisPersistence implements Persistence {

    public static final String NAME_COLUMN = "name";
    Jedis jedis;

    RedisPersistence() {
        this.jedis = new Jedis();
    }

    private String newPieceKey() {
        String uniqueId = UUID.randomUUID().toString();
        return "piece" + uniqueId;
    }

    @Override
    public void addPiece(String name) {
        String key = newPieceKey();
        jedis.hset(name, NAME_COLUMN, name);
        // todo key?
    }

    @Override
    public void addPattern(String pieceName, List<Integer> pattern) {
//        String pieceKey = jedis.hget()
        jedis.sadd(pieceName, pattern.toString());
        System.out.println(pattern.toString());
    }
}
