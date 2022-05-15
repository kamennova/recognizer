package com.kamennova.lala.common;

import java.util.List;

public class Tonality {
    public Tonality(Integer b, List<Integer> sharps) {
        this.base = b;
        this.sharps = sharps;
    }

    public List<Integer> sharps;
    public Integer base;
}
