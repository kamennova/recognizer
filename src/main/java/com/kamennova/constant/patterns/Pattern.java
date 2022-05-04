package com.kamennova.constant.patterns;

import com.kamennova.constant.RelativeBasis;

import java.util.List;

public class Pattern {
    private final List<RelativeBasis> content;

    public List<RelativeBasis> getContent() {
        return content;
    }

    public Pattern(List<RelativeBasis> intervals) {
        this.content = intervals;
    }
}
