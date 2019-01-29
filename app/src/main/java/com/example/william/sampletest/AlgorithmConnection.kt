package com.example.william.sampletest


interface AlgorithmConnection{
    fun init()
    fun getAngles(sampleFreqs: DoubleArray): DoubleArray
    fun setDesiredFreq(desiredFreq: Double): Double
    fun getDesiredFreq(): Double
}