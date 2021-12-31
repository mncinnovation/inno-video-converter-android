package com.mncgroup.innovideoconverter

import android.app.Activity
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.io.File

class InnoVideoConverter(
    private val activity: Activity
) {
    private var sessionTag = mutableListOf<Map<Int, Long>>()

    companion object {
        const val TAG = "InnoVideoConverter"
    }

    /**
     * Compress file input uri file video with quality option
     * @param tag is an unique number identifier for compression process
     * @param fileUriVideo file uri of video file
     * @param qualityOption option of video quality.
     * @param scale scale size of video
     * @param encodingSpeedOption encoding speed to compression ratio
     */
    fun compressVideoQuality(
        tag: Int,
        fileUriVideo: Uri,
        qualityOption: QualityOption,
        scale: InnoVideoScale,
        encodingSpeedOption: EncodingSpeedOption? = EncodingSpeedOption.MEDIUM,
        callback: InnoVideoConverterCallback
    ) {
        val inputFile = FFmpegKitConfig.getSafParameterForRead(activity, fileUriVideo)
        val file = getFileCacheDir()

        val exe =
            "-y -i " + inputFile + " -vf scale=${scale.width}:${scale.height} -preset ${encodingSpeedOption?.value} -crf ${qualityOption.value} " + file.absolutePath
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "command : $exe")
        }
        executeCommandAsync(tag, fileUriVideo, exe, file.absolutePath, callback)
    }

    /**
     * cancel existing process
     */
    fun cancel() {
        FFmpegKit.cancel()
        sessionTag.clear()
    }

    /**
     * cancel compression process by spesific tag
     * @param tag is and unique number of compression process
     */
    fun cancel(tag: Int) {
        var indexFounded: Int? = null
        for (i in 0 until sessionTag.size) {
            if (sessionTag[i].containsKey(tag)) {
                sessionTag[i][tag]?.let { sessionId ->
                    indexFounded = i
                    FFmpegKit.cancel(sessionId)
                }
            }
        }
        indexFounded?.let { sessionTag.removeAt(it) }

    }

    private fun getFileCacheDir(): File {
        val folder = activity.cacheDir
        return File(folder, System.currentTimeMillis().toString() + ".mp4")
    }

    private fun executeCommandAsync(
        tag: Int,
        fileUriVideo: Uri,
        command: String,
        filePath: String,
        callback: InnoVideoConverterCallback
    ) {
        try {
            val duration = MediaPlayer.create(activity, fileUriVideo).duration.toDouble()

            callback.onProgress(true, 0.0)

            val sessionExe = FFmpegKit.executeAsync(command,
                { session ->
                    val state = session.state
                    val returnCode = session.returnCode
                    // CALLED WHEN SESSION IS EXECUTED
                    Log.d(
                        TAG,
                        java.lang.String.format(
                            "FFmpeg process exited with state %s and rc %s.%s",
                            state,
                            returnCode,
                            session.failStackTrace
                        )
                    )
                    activity.runOnUiThread {
                        callback.onProgress(false, 100.0)

                        when {
                            returnCode.isSuccess -> {
                                callback.onSuccessConverted(
                                    "Success compressed",
                                    filePath
                                )
                            }
                            returnCode.isError -> {
                                callback.onErrorConvert(
                                    "Error compress. ${session.logsAsString}"
                                )
                            }
                            else -> {
                                callback.onCanceledConvert(
                                    "Canceled compress by user"
                                )
                            }
                        }
                    }
                }, {
                    // CALLED WHEN SESSION PRINTS LOGS
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "LOG : ${it.message}")
                    }
                }) {
                // CALLED WHEN SESSION GENERATES STATISTICS
                activity.runOnUiThread {
                    val percent: Double = (it.time.toDouble() / duration) * 100
                    callback.onProgress(true, percent)
                }
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "STATS : $it")
                }
            }
            Log.i(TAG, "sessionId: ${sessionExe.sessionId}")

            sessionTag.add(
                mapOf(
                    tag to sessionExe.sessionId
                )
            )
        } catch (e: Exception) {
            callback.onErrorConvert(e.message ?: e.localizedMessage ?: e.toString())
            e.printStackTrace()
        }
    }
}

interface InnoVideoConverterCallback {
    fun onProgress(progress: Boolean, percent: Double)
    fun onSuccessConverted(message: String, newUriFileConverted: String)
    fun onErrorConvert(message: String)
    fun onCanceledConvert(message: String)
}

/**
 * Video Quality Option for compressing the video
 * [VERY_LOW] for very low quality of video
 * [LOW] for low quality of video
 * [MEDIUM] for medium quality of video
 * [HIGH] for high quality of video.
 * [VERY_HIGH] for very high quality of video.
 */
enum class QualityOption(val value: String) {
    VERY_HIGH("17"),
    HIGH("20"),
    MEDIUM("23"),
    LOW("25"),
    VERY_LOW("28")
}


/**
 * This is a collection of options that will provide a certain encoding speed to compression ratio.
 * Default value is [MEDIUM].
 * [VERY_SLOW] for very slow encoding speed.
 * [SLOWER] for slower encoding speed.
 * [SLOW] for slow encoding speed.
 * [MEDIUM] for medium encoding speed.
 * [FAST] for fast encoding speed.
 * [FASTER] for faster encoding speed.
 * [VERY_FAST] for very fast encoding speed.
 * [SUPER_FAST] for super fast encoding speed.
 * [ULTRA_FAST] for ultra fast encoding speed.
 */
enum class EncodingSpeedOption(val value: String) {
    ULTRA_FAST("ultrafast"),
    SUPER_FAST("superfast"),
    VERY_FAST("veryfast"),
    FASTER("faster"),
    FAST("fast"),
    MEDIUM("medium"),
    SLOW("slow"),
    SLOWER("slower"),
    VERY_SLOW("veryslow")
}