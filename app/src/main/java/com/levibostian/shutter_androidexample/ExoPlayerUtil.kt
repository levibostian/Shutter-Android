package com.levibostian.shutter_androidexample

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

object ExoPlayerUtil {

    fun getMp4StreamingExoPlayer(context: Context, videoUri: Uri): SimpleExoPlayer {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "Shutter-Android Example"), bandwidthMeter)
        val extractorsFactory = DefaultExtractorsFactory()

        // using the ExtractorMediaSource because it is best for mp4 videos.
        val videoSource = ExtractorMediaSource(videoUri, dataSourceFactory, extractorsFactory, null, null)

        val videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        videoPlayer.prepare(videoSource)
        videoPlayer.playWhenReady = true

        return videoPlayer
    }

}