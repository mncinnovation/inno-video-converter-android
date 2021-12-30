package com.mncgroup.innovideoconverter

import android.app.Activity
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
            QualityOption.ULTRA_HIGH -> "0"
            QualityOption.VERY_HIGH -> "10"
            QualityOption.HIGH -> "18"
            QualityOption.MEDIUM -> "23"
            QualityOption.LOW -> "28"
            QualityOption.VERY_LOW -> "33"
        }
        val exe =
            "-y -i " + inputFile + " -vf scale=${scale.width}:${scale.height} -preset veryfast -crf $crf " + file.absolutePath
        executeCommandAsync(exe, file.absolutePath)
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

    private fun executeCommandAsync(command: String, filePath: String) {
        callback.onProgress(true)
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
                    callback.onProgress(false)

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
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "STATS : $it")
            }
        }
    }
}

interface InnoVideoConverterCallback {
    fun onProgress(progress: Boolean)
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
 * [ULTRA_HIGH] for ultra high quality of video.
 */
enum class QualityOption {
    ULTRA_HIGH,
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW,
    VERY_LOW
}