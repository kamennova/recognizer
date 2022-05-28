package com.kamennova.lala.persistence;

import com.sun.source.tree.Tree;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RedisPersistence implements Persistence {

    public static final String NAME_COLUMN = "name";
    Jedis jedis;

    public RedisPersistence() {
        this.jedis = new Jedis();
    }

    private String newPieceKey() {
        String uniqueId = UUID.randomUUID().toString();
        return "piece" + uniqueId;
    }

    @Override
    public void addPiece(String name) {
        String key = newPieceKey();
        jedis.sadd("pieces", name);
//        jedis.hset(name, NAME_COLUMN, name);
        // todo key?
    }

    private String getPiecePatternKey(String pieceName, int size) {
        return "pieces#" + pieceName + "#" + size;
    }

    @Override
    public void addPattern(String pieceName, String pattern) {
        String patternPieceKey = getPiecePatternKey(pieceName, pattern.length());
        jedis.sadd(patternPieceKey, pattern);
    }

    @Override
    public List<String> findPiecesWithPattern(String pattern, BiFunction<String, String, Integer> compFunc) {
        Set<String> piecesNames = jedis.smembers("pieces");
        System.out.println("members");
        String patternKey = getPiecePatternKey(piecesNames.iterator().next(), pattern.length());
        System.out.println(jedis.smembers(patternKey));

        return piecesNames.stream()
                .filter(name -> jedis.sismember(patternKey, pattern))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> findPiecesByNotePatterns(List<String> patterns,
                                                         BiFunction<String, String, Integer> compFunc) {
        if (patterns.size() == 0) {
            return new HashMap<>();
        }

        int patternSize = patterns.get(0).length();
        List<String> piecesNames = new ArrayList<>(jedis.smembers("pieces"));

        HashMap<String, Integer> piecesResult = new HashMap<>();

        for (int i = 0; i < piecesNames.size(); i++) {
            String name = piecesNames.get(i);
            Set<String> existing = jedis.smembers(getPiecePatternKey(name, patternSize));
            Integer score = existing.stream()
                    .map(str -> patterns.stream().map(base -> compFunc.apply(base, str)).reduce(0, Integer::sum))
                    .reduce(0, Integer::sum);

            if (score > 0) {
                piecesResult.put(name, score);
            }
        }

        return piecesResult;
    }
}
