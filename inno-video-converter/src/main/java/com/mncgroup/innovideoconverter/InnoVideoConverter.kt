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
     * Compress file input uri file video with option quality
     * @param fileUriVideo file uri of video file
     * @param qualityOption option of video quality.
     *
     */
    fun compressVideoQuality(fileUriVideo: Uri, qualityOption: QualityOption) {
        val inputFile = FFmpegKitConfig.getSafParameterForRead(activity, fileUriVideo)
        val file = getFileCacheDir()
        val crf = when (qualityOption) {
            QualityOption.HIGH -> "18"
            QualityOption.MEDIUM -> "23"
            QualityOption.LOW -> "28"
        }
        val exe =
            "-y -i " + inputFile + " -vf scale=-1:720 -preset veryfast -crf $crf " + file.absolutePath
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
 * [LOW] for low quality of video
 * [MEDIUM] for medium quality of video
 * [HIGH] for high quality of video.
 */
enum class QualityOption {
    HIGH,
    MEDIUM,
    LOW
}