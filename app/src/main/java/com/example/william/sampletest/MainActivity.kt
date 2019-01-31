package com.example.william.sampletest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.Manifest

import android.content.pm.PackageManager
import android.os.Environment.getExternalStoragePublicDirectory
import android.support.v4.content.ContextCompat
import android.support.v4.app.ActivityCompat
import android.widget.Toast

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.io.TarsosDSPAudioFormat

import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.onsets.OnsetHandler
import be.tarsos.dsp.onsets.PercussionOnsetDetector
import be.tarsos.dsp.writer.WriterProcessor
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor

import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.io.File
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_RECORD_AUDIO = 1
    private val sampleRate = 44100
    private val sampleRateF = 44100f
    private val audioBufferSize = 2048
    private val bufferOverlap = 0


    private lateinit var nameText:TextView
    private lateinit var lugNumberText:TextView
    private lateinit var frequencyText:TextView

    private lateinit var sampleFile:RandomAccessFile
    private var lugNumber = 0
    private var numLugs = 8
    private final val listenDuration = 2000L
    private lateinit var sampleDirStr:String
    //Tarsos listening objects
    private lateinit var listener:AudioDispatcher
    private lateinit var drumDetector:PercussionOnsetDetector
    private lateinit var drumHandler:OnsetHandler
    private lateinit var outputFormat:TarsosDSPAudioFormat
    private lateinit var writer:WriterProcessor
    private lateinit var pdh:PitchDetectionHandler
    private lateinit var pitchProc:PitchProcessor

    private lateinit var audioThread:Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioPermissions()
        requestFilePermissions()


        sampleDirStr = "/storage/self/primary/MovieMakerLib/samples"//("samples").path//+"/samples"//"//Internal storage/MovieMakerLib/samples"//cacheDir.path+"/samples"

        refreshSampleDir()


        nameText = findViewById(R.id.temp_name)
        nameText.text = "Tap 1"
        lugNumberText = findViewById(R.id.lugNum)
        lugNumberText.text = String.format("Lug Number: %d",lugNumber)
        frequencyText = findViewById(R.id.frequency)
        frequencyText.text = "freq detection not working"
        frequencyText.text = sampleDirStr

        // sampling objects
        val session = Tuning()
        val sampler = DrumSampler()

        setupTarsos(lugNumber)

        audioThread =  Thread(listener, "Drum Listener")
        audioThread.start()

        //await drum strike, capture and pass to drumSampler?

        //get result, strike again if necessary

        //if good, progress to next lug

        //when all lugs sampled, calculate angles and proceed to tune screen:

            //prompt user to install at certain lug

            //confirm installed, send turn command

            // iterate and repeat from lug installation

        //return to sampling

        //if tuned, go to completion screen

    }

    private fun refreshSampleDir() {


        if(!File(sampleDirStr).exists()) {
            File(sampleDirStr).mkdir()
        }

        //delete contents of folder
        val contents = File(sampleDirStr).listFiles()
        if (contents != null) {
            for (f in contents!!) {
                f.delete()
            }
        }
    }


    fun drumStrikeDetected(time: Double) {
        //latch drum strike and start listening.
        listener.removeAudioProcessor(drumDetector)
        listener.addAudioProcessor(writer)

        nameText.text = String.format("Drum Onset time: %f.3",time)
        lugNumber++
        lugNumberText.text = String.format("Lug Number: %d",lugNumber)
        sleep(listenDuration)
        writer.processingFinished()
        listener.removeAudioProcessor(writer)
        if(lugNumber<numLugs) {
            listener.addAudioProcessor(drumDetector)
        }
        frequencyText.text = File(sampleDirStr).list()[lugNumber-1].toString()
        setupWriter()
    }

    fun setupTarsos(lugNumber:Int){
        listener = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, audioBufferSize, bufferOverlap)

        // setup onset detection
        val threshold = 10.0
        val sensitivity = 50.0

        drumDetector = PercussionOnsetDetector(sampleRateF, audioBufferSize,
            OnsetHandler { time, salience -> runOnUiThread { drumStrikeDetected(time) } },
            sensitivity,threshold
        )


        //setup file recording
        setupWriter()

        // start drum strike detector
        listener.addAudioProcessor(drumDetector)

    }

    private fun setupWriter(){
        var audioFileStr = sampleDirStr+String.format("/lug%d.wav",lugNumber)

        if(File(audioFileStr).exists()) {
            File(audioFileStr).delete()
        }

        sampleFile = RandomAccessFile(audioFileStr, "rw")
        outputFormat = TarsosDSPAudioFormat(sampleRateF,16,1,false,false)
        writer = WriterProcessor(outputFormat,sampleFile)
    }


    private fun requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show()

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    MY_PERMISSIONS_RECORD_AUDIO
                )

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    MY_PERMISSIONS_RECORD_AUDIO
                )
            }
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {

        }
    }

    private fun requestFilePermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(this, "Please grant permissions to access external storage", Toast.LENGTH_LONG).show()

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

        }
    }
    private fun processFiles(){


    }

}

