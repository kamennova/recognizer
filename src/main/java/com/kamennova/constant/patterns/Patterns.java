package com.kamennova.constant.patterns;

import com.kamennova.constant.RelativeBasis;

import java.util.List;

public class Patterns {
    public static Pattern MAJOR_TRIAD = new Pattern(
            List.of(
                    new RelativeBasis(Interval.Root, 1),
                    new RelativeBasis(Interval.MajorThird, 1),
                    new RelativeBasis(Interval.PerfectFifth, 1),
                    new RelativeBasis(Interval.MajorThird, 1)
            )
    );


}
