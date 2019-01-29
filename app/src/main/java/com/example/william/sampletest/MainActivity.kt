package com.example.william.sampletest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.Manifest

import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import android.media.MediaRecorder
import android.os.Environment

import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.onsets.OnsetHandler
import be.tarsos.dsp.onsets.PercussionOnsetDetector
import java.io.File


class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_RECORD_AUDIO = 1
    private val sampleRate = 44100
    private val sampleRateF = 44100f
    private val audioBufferSize = 2048
    private val bufferOverlap = 0

//    private val sampleFile = File()
    private val lugNumber = 0
    private final val listenDuration = 2000

    private val drumRecorder = MediaRecorder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var name = findViewById<TextView>(R.id.temp_name)
        name.text = "Tap 1"

        // sampling objects
        val session = Tuning()
        val sampler = DrumSampler()


        requestAudioPermissions()

        //Tarsos listening objects
        val listener = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, audioBufferSize, bufferOverlap)

        // onset detection works
        val threshold = 8.0
        val sensitivity = 20.0
        val drumDetector = PercussionOnsetDetector(
            sampleRateF,
            audioBufferSize,
            OnsetHandler { time, salience -> runOnUiThread { drumStrikeDetected(name, time) } },
            sensitivity,
            threshold
        )

        //setup mediarecorder to record drum strike when onset detection triggers
        setupMediaRecorder(lugNumber)

//        val samples = Array<File>(8) { i->File(outputDir+String.format("/lug %d.3gp",i))}



        //start listening
        listener.addAudioProcessor(drumDetector)

        val audioThread =  Thread(listener, "Drum Listener")
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



    fun drumStrikeDetected(name:TextView, time: Double) {
        //TODO latch drum strike and start listening.
        name.setText(time.toString())
    }

    fun startRecording(lugNumber:Int){

    }

    fun setupMediaRecorder(lugNumber: Int){
        val outputDir = Environment.getExternalStorageDirectory().absolutePath
        val outputFile = String.format("/lug %d.3gp", lugNumber)

        drumRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        drumRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        drumRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        drumRecorder.setOutputFile(outputDir+outputFile)
        drumRecorder.setMaxDuration(listenDuration)
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
}

