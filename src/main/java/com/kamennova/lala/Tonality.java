package com.kamennova.lala;

import java.util.List;

public class Tonality {
    Tonality(Integer b, List<Integer> sharps) {
        this.base = b;
        this.sharps = sharps;
    }

    public List<Integer> sharps;
    public Integer base;
}
