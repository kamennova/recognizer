package com.kamennova.constant.patterns;

//public enum IntervalName {
//    IntervalN
//}

public enum Interval {
    Root(0, "Root", 0),  // todo root from wrong
    DiminishedSecond(0, "Diminished", 2),
    DiminishedThird(2, "Diminished", 3),
    DiminishedFourth(4, "Diminished", 4),
    DiminishedFifth(6, "Diminished", 5),
    DiminishedSeventh(9, "Diminished", 7),

    MajorThird(4, "Major", 3),
    MajorSeventh(11, "Major", 7),

    MinorThird(3, "Minor", 3),
    MinorSeventh(10, "Minor", 7),

    PerfectFifth(7, "Perfect", 5),
    AugmentedFifth(8, "Augmented", 5);


    private short halfStepsNum;
    private String mainName;
    private short level;

    Interval(int halfStepsNum, String mainName, int level){
        this.halfStepsNum = (short) halfStepsNum;
        this.mainName = mainName;
        this.level = (short) level;
    }
}
