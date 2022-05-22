package com.kamennova.lala.persistence;

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
        int patternSize = patterns.get(0).length();
        List<String> piecesNames = new ArrayList<>(jedis.smembers("pieces"));

        String tempSearch = "tempSearch";
        jedis.del(tempSearch);
        patterns.forEach(p -> jedis.sadd(tempSearch, p));

        List<Integer> piecesResults = piecesNames.stream().map(name -> {
            Set<String> over = jedis.sinter(getPiecePatternKey(name, patternSize), tempSearch);
            System.out.println(over);
            return over.size();
        })
                .collect(Collectors.toList());

        // todo first search common

        /*
        piecesResult.entrySet().stream()
                .min(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .get(); todo
         */
        return IntStream.range(0, piecesNames.size())
                .boxed()
                .filter(i -> piecesResults.get(i) > 0)
                .collect(Collectors.toMap(
                        piecesNames::get,
                        piecesResults::get,
                        (prev, next) -> prev,
                        HashMap::new));
    }
}
