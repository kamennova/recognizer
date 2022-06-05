package com.kamennova.lala.persistence;

import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class RedisPersistence implements Persistence {

    public static final String NAME_COLUMN = "name";
    public static final String PIECE_SET = "pieces";
    Jedis jedis;

    public Jedis getJedis() {
        return this.jedis;
    }

    public RedisPersistence() {
        this.jedis = new Jedis();
    }

    private String newPieceKey() {
        String uniqueId = UUID.randomUUID().toString();
        return "piece" + uniqueId;
    }

    @Override
    public void addPiece(String name) {
        if (pieceExists(name)) {
            throw new RuntimeException("Piece with name " + name + " already exists");
        }

        jedis.sadd(PIECE_SET, name);
    }

    public boolean pieceExists(String name) {
        return jedis.sismember(PIECE_SET, name);
    }

    private String getPiecePatternKey(String pieceName, int size) {
        return "pieces#" + pieceName + "#" + size;
    }

    @Override
    public void addPattern(String pieceName, String pattern) {
        if (!pieceExists(pieceName)) {
            throw new RuntimeException("Piece with name " + pieceName + " does not exist");
        }

        String patternPieceKey = getPiecePatternKey(pieceName, pattern.length());
        jedis.sadd(patternPieceKey, pattern);
    }

    @Override
    public Map<String, Double> findPiecesByNotePatterns(List<String> patterns,
                                                        BiFunction<String, String, Double> compFunc) {
        if (patterns.size() == 0) {
            return new HashMap<>();
        }

        int patternSize = patterns.get(0).length();
        List<String> piecesNames = new ArrayList<>(jedis.smembers(PIECE_SET));

        HashMap<String, Double> piecesResult = new HashMap<>();

        for (String name : piecesNames) {
            Set<String> existing = jedis.smembers(getPiecePatternKey(name, patternSize));
            Double score = existing.stream()
                    .map(str -> patterns.stream().map(base -> compFunc.apply(base, str)).reduce(0D, Double::sum))
                    .reduce(0D, Double::sum);

            if (score > 0) {
                piecesResult.put(name, score);
            }
        }

        return piecesResult;
    }

    @Override
    public void clearAll() {
        jedis.flushAll();
    }
}
