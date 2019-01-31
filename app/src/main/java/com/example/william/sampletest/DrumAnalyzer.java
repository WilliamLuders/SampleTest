package com.example.william.sampletest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.badlogic.audio.io.*;
import com.badlogic.audio.analysis.FFT;
import com.badlogic.audio.analysis.FourierTransform;

//import be.tarsos.dsp.*;
//import be.tarsos.dsp.io.android.AudioDispatcherFactory;

public class DrumAnalyzer {


    //	private String filePath = "C:\\Users\\William\\Documents\\4thYear\\Capstone\\TuningAlgo\\190Hz\\--------";
    private int numLugs;
    private File sampleDir;
    private int sampleRate = 44100;

    //send file path containing one sample per lug/drum strike
    public DrumAnalyzer(String filePath) {
        sampleDir = new File(filePath);
        numLugs = sampleDir.listFiles().length;

    }

    public double[] analyzeSamples() throws FileNotFoundException {
        //TODO incorporate input path when this is copied into android project

        double overtones[] = new double[numLugs];

        for (File f:sampleDir.listFiles()) {
            System.out.println(f.getName());
            getF11(f);
        }

        return overtones;
    }
    private double getF11(File drumSample) throws FileNotFoundException {
        //AudioDispatcher listener = new AudioDispatcherFactory().fromPipe(source, targetSampleRate, audioBufferSize, bufferOverlap)fromPipe()
        float samples[] = new float[8192];
        float spectrum[] = new float[8192/2+1];
        WaveDecoder decoder;
        try {
            decoder = new WaveDecoder(new FileInputStream(drumSample));
            decoder.readSamples(samples);

            FFT fft = new FFT(8192, sampleRate);
            fft.window(FFT.HAMMING);

            fft.forward(samples);
            System.arraycopy(fft.getSpectrum(), 0, spectrum, 0, spectrum.length);

            for (int i = 0; i<100; i++) {
                System.out.print(spectrum[i]+", ");
            }

            //TODO scale index max to get true max freq
            return max(spectrum);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1.0;
    }
    private double max(float spectrum[]) {
        float valueMax = 0;
        int indexMax = 0;
        for (int i = 0; i<spectrum.length; i++) {
            if(spectrum[i]>valueMax) {
                valueMax = spectrum[i];
                indexMax = i;
            }
        }

        return indexMax;

    }
}
