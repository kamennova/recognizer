package com.kamennova.lala;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// wrapper class for omnizart
public class Mp3ToMidiTranscriber {
    public static void transcribeToMidi2(String pathToFile) throws  IOException, InterruptedException {
        String command = "ping -c 3 www.google.com";

        Process proc = null;
            proc = Runtime.getRuntime().exec(command);
        // Read the output

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.print(line + "\n");
        }

        proc.waitFor();
    }


    public static void transcribeToMidi(String pathToFile) throws IOException, InterruptedException {

    }

}
