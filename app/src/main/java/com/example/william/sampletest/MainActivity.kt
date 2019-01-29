package com.example.william.sampletest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.Manifest

import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v4.app.ActivityCompat
import android.widget.Toast
//import android.media.MediaRecorder
import android.os.Environment
//import android.media.MediaRecorder.OnInfoListener
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.TarsosDSPAudioFormat

import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.onsets.OnsetHandler
import be.tarsos.dsp.onsets.PercussionOnsetDetector
import be.tarsos.dsp.writer.WriterProcessor
import kotlinx.android.synthetic.main.activity_main.*


import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile


class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_RECORD_AUDIO = 1

    private val sampleRate = 44100
    private val sampleRateF = 44100f
    private val audioBufferSize = 2048
    private val bufferOverlap = 0

    private lateinit var name:TextView
    private lateinit var lugNumberText:TextView

    private lateinit var sampleFile:RandomAccessFile
    private val lugNumber = 0
    private final val listenDuration = 2000


    //Tarsos listening objects
    private lateinit var listener:AudioDispatcher
    private lateinit var drumDetector:PercussionOnsetDetector
    private lateinit var outputFormat:TarsosDSPAudioFormat
    private lateinit var writer:WriterProcessor

//    private val drumRecorder = MediaRecorder()
//    private var drumRecorderRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        name = findViewById<TextView>(R.id.temp_name)
        name.text = "Tap 1"
        lugNumberText = findViewById(R.id.lugNum)
        lugNumberText.text = lugNumber.toString()
        // sampling objects
        val session = Tuning()
        val sampler = DrumSampler()


        requestAudioPermissions()



        //setup tarsos for onset detection and recording
        setupRecording(lugNumber)











        val audioThread =  Thread(listener, "Drum Listener")
        audioThread.start()
        audioThread.join(2000)

        //await drum strike, capture and pass to drumSampler?
            //this gets done in

        //get result, strike again if necessary

        //if good, progress to next lug

        //when all lugs sampled, calculate angles and proceed to tune screen:

            //prompt user to install at certain lug

            //confirm installed, send turn command

            // iterate and repeat from lug installation

        //return to sampling

        //if tuned, go to completion screen

    }



    fun drumStrikeDetected(time: Double) {
        //TODO latch drum strike and start listening.

        name.setText(time.toString())
        name.setText(filesDir.list()[0].toString())
//        if(!drumRecorderRunning) {
//            drumRecorderRunning = true
//            drumRecorder.start()
//        }
    }


    fun setupRecording(lugNumber: Int){

        listener = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, audioBufferSize, bufferOverlap)

        // onset detection works
        val threshold = 8.0
        val sensitivity = 20.0
        drumDetector = PercussionOnsetDetector(
            sampleRateF,
            audioBufferSize,
            OnsetHandler { time, salience -> runOnUiThread { drumStrikeDetected(time) } },
            sensitivity,
            threshold
        )

        try {
            sampleFile = RandomAccessFile(cacheDir.path+String.format("/lug%d.3gp",lugNumber), "rw")//"/lug%d.3gp")
//            if(!sampleFile.exists())
//                sampleFile.mkdir()

            outputFormat = TarsosDSPAudioFormat(sampleRateF,16,1,true,false)
            writer = WriterProcessor(outputFormat,sampleFile)
        } catch(e:FileNotFoundException){
            e.printStackTrace()
        }

        //start listening
        listener.addAudioProcessor(drumDetector)
        listener.addAudioProcessor(writer)
        listener.run()
    }


//    fun setupMediaRecorder(lugNumber: Int){
////        val outputDir = filesDir.absolutePath.toString()
////        val outputFile = String.format("/lug%d.3gp", lugNumber)
//
//        try {
//            sampleFile = File(cacheDir, "/lug%d.3gp")
//            if(!sampleFile.exists())
//                sampleFile.mkdir()
//        } catch(e:FileNotFoundException){
//            e.printStackTrace()
//        }
//
//
//        drumRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
//        drumRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//        drumRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
//        drumRecorder.setOutputFile(sampleFile.absolutePath)
//        drumRecorder.setMaxDuration(listenDuration)
//
//
//
//        try {
//            drumRecorder.prepare()
//        } catch (e: IllegalStateException) {
//            e.printStackTrace()
//        }
//    }
    //setup mediarecorder to record drum strike when onset detection triggers
//        drumRecorder.setOnInfoListener(OnInfoListener { mr, what, extra ->
//            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
//                name.setText("Recording Done")
//                drumRecorder.reset()
//                drumRecorderRunning = false
//                setupMediaRecorder(lugNumber)
//                name.setText("recording done")
//
//            }
//        })


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

