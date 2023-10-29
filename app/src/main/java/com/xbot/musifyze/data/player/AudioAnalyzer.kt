package com.xbot.musifyze.data.player

import kotlinx.coroutines.flow.Flow

interface AudioAnalyzer {
    val dataFlow: Flow<AudioAnalyzerData>
}