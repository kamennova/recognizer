package com.kamennova.lala;

import org.apache.commons.io.FilenameUtils;

import java.io.*;

// wrapper class for omnizart
public class Mp3ToMidiTranscriber {
    private String getMidiPath(String input) {
        String[] parts = input.split("\\.");
        return parts[0] + ".mid";
    }

    public static String getCommand(String pathToFile, String output) {
        return "omnizart music transcribe " + pathToFile + " -o " + output;
    }

    public static boolean transcribeToMidi(String pathToFile, String output) throws IOException, InterruptedException {
        File file = new File(pathToFile);
        if (!file.exists()) {
            throw new FileNotFoundException(pathToFile);
        } else if (!FilenameUtils.getExtension(pathToFile).equals("mp3")) {
            throw new RuntimeException("Transcribed file format should be mp3");
        }

        String command = getCommand(pathToFile, output);
        System.out.println(command);
        Process proc = null;
        proc = Runtime.getRuntime().exec(command);

        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.print(line + "\n");
        }

        System.out.println(new String(proc.getErrorStream().readAllBytes()));
        return proc.waitFor() == 0;
    }

    public static void main(String[] args) {
        try {
            transcribeToMidi("Downloads/record2.mp3", "Downloads/record2.mid");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
