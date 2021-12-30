package com.mncgroup.innovideoconverter

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.arthenica.ffmpegkit.BuildConfig
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
     * Convert file input uri file video with yuv444p
     * @param fileUriVideo file uri of video file
     * @param pixelFormat pixel format of filter. For ex : yuv420p, yuv422, gray etc. List name of pixel format can be founded <a href="https://ffmpeg.org/pipermail/ffmpeg-devel/2007-May/035617.html">here</a>
     */
    fun convertFilterPixelFormat(fileUriVideo: Uri, pixelFormat : String) {
        val inputFile = FFmpegKitConfig.getSafParameterForRead(activity, fileUriVideo)
        val file = getFileCacheDir()
        val exe = "-y -i " + inputFile + " -pix_fmt $pixelFormat " + file.absolutePath
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
                                "Success converted with yuv444p",
                                filePath
                            )
                        }
                        returnCode.isError -> {
                            callback.onErrorConvert(
                                "Error convert with yuv444p. ${session.failStackTrace}"
                            )
                        }
                        else -> {
                            callback.onCanceledConvert(
                                "Canceled convert with yuv444p by user"
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