package com.kamennova.lala;

public class LaLa {
    public static void main(String[] args) throws Exception {
        // learn
        learnMode("Sonat111");
    }

    private static void learnMode(String pieceName) throws Exception {
        LaLaLearn l = new LaLaLearn(pieceName);
        l.processInput("src/main/resources/Path1.mid");
//        l.processInput("src/main/resources/Path1.mid");
//        l.processInput("src/main/resources/Path1.mid");
        l.finishLearn();
    }
}
