package unit;

import java.io.*;
import javax.sound.sampled.*;

public class AudioFileCutter {

    public static void main(String[] args) throws IOException, InterruptedException {
        cutMp3File(10, 15, "src/main/resources/alala.mp3", "src/main/resources/alala-temp.mp3");
    }

    public static boolean cutMp3File(int startSecond, int duration, String input, String output) throws IOException, InterruptedException {
        String command = "ffmpeg -ss  " + startSecond + " -t  " + duration + " -i " + input + " " + output;
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

    public static void copyAudio(String sourceFileName, String destinationFileName, int startSecond, int secondsToCopy) {
        AudioInputStream inputStream = null;
        AudioInputStream shortenedStream = null;
        try {
            File file = new File(sourceFileName);
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
            AudioFormat format = fileFormat.getFormat();
            inputStream = AudioSystem.getAudioInputStream(file);
            int bytesPerSecond = format.getFrameSize() * (int) format.getFrameRate();
            inputStream.skip(startSecond * bytesPerSecond);
            long framesOfAudioToCopy = secondsToCopy * (int) format.getFrameRate();
            shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);
            File destinationFile = new File(destinationFileName);
            AudioSystem.write(shortenedStream, fileFormat.getType(), destinationFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (shortenedStream != null) try {
                shortenedStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}