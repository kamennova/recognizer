package com.kamennova.lala.persistence;

import redis.clients.jedis.Jedis;

import java.util.*;
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

    private String getPiecePatternKey(String pieceName) {
        return "pieces#" + pieceName;
    }

    @Override
    public void addPattern(String pieceName, List<Integer> pattern) {
//        String pieceKey = jedis.hget()
        jedis.sadd(getPiecePatternKey(pieceName), pattern.toString());
        System.out.println(pattern.toString());
    }

    @Override
    public List<String> findPiecesWithPattern(List<Integer> pattern) {
        Set<String> piecesNames = jedis.smembers("pieces");
        System.out.println("members");
        System.out.println(jedis.smembers(getPiecePatternKey(piecesNames.iterator().next())));

        return piecesNames.stream()
                .filter(name -> jedis.sismember(getPiecePatternKey(name), pattern.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public HashMap<String, Integer> findPiecesByNotePatterns(List<List<Integer>> patterns) {
        List<String> piecesNames = new ArrayList<>(jedis.smembers("pieces"));
//        System.out.
        // temp
        String tempSearch = "tempSearch";
        jedis.del(tempSearch);
        patterns.forEach(p -> jedis.sadd(tempSearch, p.toString()));

        List<Integer> piecesResults = piecesNames.stream().map(name -> {
            Set<String> over = jedis.sinter(getPiecePatternKey(name), tempSearch);
            System.out.println(over);
            return over.size();
        })
                .collect(Collectors.toList());

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
