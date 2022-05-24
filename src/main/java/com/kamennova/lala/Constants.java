package com.kamennova.lala;

import com.kamennova.lala.common.Note;
import com.kamennova.lala.common.Tonality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Constants {
    public static class Keys {
//        public static List<Integer> C_MAJOR
    }

    List<Integer> geese = new ArrayList<>(Arrays.asList(65, 64, 62, 60, 67, 67, 65, 64, 62, 60, 67, 67,
            65, 69, 69, 65, 64, 67, 67, 64, 62, 64, 65, 60, 60,
            65, 69, 69, 65, 64, 67, 67, 64, 62, 64, 65, 60, 60));

    List<Note> timedGeese = new ArrayList<>(Arrays.asList(
            n(65, 4), n(64, 4), n(62, 4), n(60, 4), n(67, 8), n(67, 8),
            n(65, 4), n(64, 4), n(62, 4), n(60, 4), n(67, 8), n(67, 8),
            n(65, 4), n(69, 4), n(69, 4), n(65, 4), n(64, 4), n(67, 4), n(67, 4), n(64, 4),
            n(62, 4), n(64, 4), n(65, 4), n(60, 8), n(60, 8)
    ));

    public static List<Tonality> TONALITIES = Arrays.asList(
            //
            new Tonality(NOTE.C.code, Collections.emptyList()),
            new Tonality(NOTE.G.code, Arrays.asList(NOTE.F_SHARP.code)),
            new Tonality(NOTE.D.code, Arrays.asList(NOTE.F_SHARP.code, NOTE.C_SHARP.code)),
            new Tonality(NOTE.A.code, Arrays.asList(NOTE.F_SHARP.code, NOTE.C_SHARP.code, NOTE.G_SHARP.code)),
            new Tonality(NOTE.E.code, Arrays.asList(NOTE.F_SHARP.code, NOTE.C_SHARP.code, NOTE.G_SHARP.code, NOTE.D_SHARP.code)),
            new Tonality(NOTE.B.code, Arrays.asList(NOTE.F_SHARP.code, NOTE.C_SHARP.code, NOTE.G_SHARP.code, NOTE.D_SHARP.code, NOTE.A_SHARP.code)),
            new Tonality(NOTE.F_SHARP.code, Arrays.asList(NOTE.F_SHARP.code, NOTE.C_SHARP.code, NOTE.G_SHARP.code, NOTE.D_SHARP.code, NOTE.A_SHARP.code, NOTE.E_SHARP.code)),
            new Tonality(NOTE.C_SHARP.code, Arrays.asList(NOTE.F_SHARP.code, NOTE.C_SHARP.code, NOTE.G_SHARP.code, NOTE.D_SHARP.code, NOTE.A_SHARP.code, NOTE.E_SHARP.code, NOTE.B_SHARP.code))
    );

    public static String getNoteName(int note) {
        switch (note % 12) {
            case 0:
                return "C";
            case 1:
                return "Csh";
            case 2:
                return "D";
            case 4:
                return "E";
            case 5:
                return "F";
            case 7:
                return "G";
            case 9:
                return "A";
            case 11:
                return "B";
            default:
                return "?";
        }
    }

    public enum NOTE {
        C(0),
        C_SHARP(1),
        D_FLAT(1),
        D(2),
        D_SHARP(3),
        E_FLAT(3),
        E(4),
        F_FLAT(4),
        E_SHARP(5),
        F(5),
        F_SHARP(6),
        G_FLAT(6),
        G(7),
        G_SHARP(8),
        A_FLAT(8),
        A(9),
        A_SHARP(10),
        B_FLAT(10),
        B(11),
        C_FLAT(11),
        B_SHARP(0);

        public Integer code;

        public static String getNoteName(int note) {
            switch (note % 12) {
                case 0:
                    return "C";
                case 1:
                    return "C♯";
                case 2:
                    return "D";
                case 3:
                    return "D♯";
                case 4:
                    return "E";
                case 5:
                    return "F";
                case 6:
                    return "F♯";
                case 7:
                    return "G";
                case 8:
                    return "G♯";
                case 9:
                    return "A";
                case 10:
                    return "A♯";
                case 11:
                    return "B";
                default:
                    return "?";
            }
        }

        NOTE(Integer c) {
            this.code = c;
        }
    }

    private Note n(int interval, int duration) {
        return new Note(interval, duration);
    }
}
