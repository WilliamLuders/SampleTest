package com.skeletonkey.capstone.skeletonkey.module.interfaces

import java.io.File

interface SamplingConnection{
    fun init(): Boolean
    fun recordSample(lugNumber: Int): Boolean
}