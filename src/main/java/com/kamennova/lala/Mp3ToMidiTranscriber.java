package com.kamennova.lala;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// wrapper class for omnizart
public class Mp3ToMidiTranscriber {
    public static boolean transcribeToMidi(String pathToFile) throws IOException, InterruptedException {
        String command = "omnizart music transcribe " + pathToFile + " -o src/main/resources/upload.mid"; // todo out

        Process proc = null;
        proc = Runtime.getRuntime().exec(command);

        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
//        reader.
        while ((line = reader.readLine()) != null) {
            System.out.print(line + "\n");
        }


        System.out.println(new String(proc.getErrorStream().readAllBytes()));
        return proc.waitFor() == 0;
    }


    public static void main(String[] args) {
        try {
            transcribeToMidi("Downloads/record2.mp3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
