package com.mncgroup.innovideoconverter

import android.app.Activity
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.io.File

class InnoVideoConverter(
    private val activity: Activity,
    private val callback: InnoVideoConverterCallback
) {
    companion object {
        const val TAG = "InnoVideoConverter"
    }

    /**
     * Compress file input uri file video with quality option
     * @param fileUriVideo file uri of video file
     * @param qualityOption option of video quality.
     * @param scale scale size of video
     *
     */
    fun compressVideoQuality(
        fileUriVideo: Uri,
        qualityOption: QualityOption,
        scale: InnoVideoScale
    ) {
        val inputFile = FFmpegKitConfig.getSafParameterForRead(activity, fileUriVideo)
        val file = getFileCacheDir()
        val crf = when (qualityOption) {
            QualityOption.VERY_HIGH -> "17"
            QualityOption.HIGH -> "20"
            QualityOption.MEDIUM -> "23"
            QualityOption.LOW -> "25"
            QualityOption.VERY_LOW -> "28"
        }
        val exe =
            "-y -i " + inputFile + " -vf scale=${scale.width}:${scale.height} -preset veryfast -crf $crf " + file.absolutePath
        executeCommandAsync(fileUriVideo, exe, file.absolutePath)
    }

    /**
     * cancel existing process
     */
    fun cancel() {
        FFmpegKit.cancel()
    }

    private fun getFileCacheDir(): File {
        val folder = activity.cacheDir
        return File(folder, System.currentTimeMillis().toString() + ".mp4")
    }

    private fun executeCommandAsync(fileUriVideo: Uri, command: String, filePath: String) {
        try {
            val duration = MediaPlayer.create(activity, fileUriVideo).duration.toDouble()

            callback.onProgress(true, 0.0)
            FFmpegKit.executeAsync(command,
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
enum class QualityOption {
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW,
    VERY_LOW
}