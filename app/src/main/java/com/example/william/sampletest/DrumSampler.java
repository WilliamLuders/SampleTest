package com.example.william.sampletest;

import android.media.MediaRecorder;
import android.os.Environment;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import com.skeletonkey.capstone.skeletonkey.module.interfaces.SamplingConnection;
//import com.skeletonkey.capstone.skeletonkey.module.interfaces.SamplingConnection;

import java.io.File;
import java.io.IOException;

public class DrumSampler implements SamplingConnection {

    private int numLugs;
    private double f11[];
    private int sampleRate = 44100;
    private int audioBufferSize = 2048;
    private int bufferOverlap = 0;

    public boolean init() {
        f11 = new double[numLugs];
        return true;
    }

    public boolean recordSample(int lugNumber){



        String outputDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String outputFile = String. format("/lug %f.3gp",lugNumber);
        File sample = new File(outputDir, outputFile);

        MediaRecorder drumListener = new MediaRecorder();
        drumListener.setAudioSource(MediaRecorder.AudioSource.MIC);
        drumListener.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        drumListener.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        drumListener.setOutputFile(outputFile+outputDir);
        drumListener.setMaxDuration(5000);

        try {
            drumListener.prepare();
            drumListener.start();
        } catch (IOException e){

        }

        return analyzeSample(lugNumber, sample); // return whether or not a pitch can be extracted
    }


    private boolean analyzeSample(int lugNumber, File sample) {
        MyPitchDetector pdh = new MyPitchDetector();
        PitchDetectionResult pdr = new PitchDetectionResult();
        AudioDispatcher dispatcher;
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, sampleRate, audioBufferSize, pdh);
//        PercussionOnsetDetector drumHitDetector = new PercussionOnsetDetector();
        // parse file

        //pass to tarsos, get and save freq content
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, audioBufferSize, bufferOverlap);
            dispatcher.addAudioProcessor(pitchProcessor);
            dispatcher.run();
            f11[lugNumber]= pdr.getPitch();
            return pdr.isPitched(); // return whether or not a pitch can be extracted

        //return false;
    }

    public double[] getF11() {
        return f11;
    }
}

class  MyPitchDetector implements PitchDetectionHandler {

    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult,
                            AudioEvent audioEvent) {
        if (pitchDetectionResult.getPitch() != -1) {
            double timeStamp = audioEvent.getTimeStamp();
            float pitch = pitchDetectionResult.getPitch();
            float probability = pitchDetectionResult.getProbability();
            //double rms = audioEvent.getRMS() * 100;
            //String message = String.format("Pitch detected at %.2fs: %.2fHz ( %.2f probability, RMS: %.5f )\n", timeStamp, pitch, probability, rms);
            //System.out.println(message);
        }

    }
}
