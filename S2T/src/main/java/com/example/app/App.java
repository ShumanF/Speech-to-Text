package com.example.app;


// Imports the Google Cloud client library
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.protobuf.ByteString;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import org.checkerframework.checker.units.qual.A;



import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class App
{
    public static void main(String... args) throws Exception {


        try{
            AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, true); // 44.1kHz, 16-bit, mono, signed PCM, big-endian            DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            if (!AudioSystem.isLineSupported(dataInfo)){
                System.out.println("Nije podrzana");
            }
            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(dataInfo);
            targetLine.open();

            String[] optionsToChoose = {"en-US", "hr-HR", "bs-BA", "sr-RS", "None of the listed"};

            String getLanguage = (String) JOptionPane.showInputDialog(
                    null,
                    "koji jezik zelite?",
                    "Odaberite jezik",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    optionsToChoose,
                    optionsToChoose[3]);

            JOptionPane.showMessageDialog(null,"Hit ok to start recording");
            targetLine.start();

            Thread audioRecorderThread = new Thread(){
                @Override public void run(){
                    AudioInputStream recordingStream = new AudioInputStream(targetLine);
                    File outputFile = new File("record.wav");
                    try {
                        AudioSystem.write(recordingStream,AudioFileFormat.Type.WAVE, outputFile);
                    } catch (IOException ex){
                        System.out.println(ex);
                    }
                    System.out.println("Stopped");
                }
            };

            audioRecorderThread.start();
            JOptionPane.showMessageDialog(null,"Hit the ok button to stop recording");
            targetLine.stop();
            targetLine.close();

            Path path = Paths.get("C:\\Users\\Korisnik\\Desktop\\S2T\\record.wav");

            String recenica = speechToText(path,getLanguage);
            System.out.println(recenica);
        }catch (Exception e){
            System.out.println(e);
        }


    }


    private static String speechToText(Path path, String jezik) throws IOException {
        // Instantiates a client
        try (SpeechClient speechClient = SpeechClient.create()) {

            // The path to the audio file to transcribe
            // String gcsUri = "gs://cloud-samples-data/speech/brooklyn_bridge.raw";


            byte[] data = Files.readAllBytes(path);
            ByteString audioBytes = ByteString.copyFrom(data);

            // Builds the sync recognize request
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(AudioEncoding.LINEAR16)
                            .setSampleRateHertz(44100)
                            .setLanguageCode(jezik)
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                return "Rezultat snimljenog glasa:\n" + alternative.getTranscript();


            }
            if (results.isEmpty()) {
                return "No transcription results returned from the API.";
            }
        }
        return null;
    }
}
