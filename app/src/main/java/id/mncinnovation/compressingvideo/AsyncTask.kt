package id.mncinnovation.compressingvideo

import android.app.Activity

abstract class AsyncTask(private val activity: Activity) {
    private fun startBackground() {
        Thread {
            doInBackground()
            activity.runOnUiThread { onPostExecute() }
        }.start()
    }

    fun execute() {
        startBackground()
    }

    abstract fun doInBackground()
    abstract fun onPostExecute()
}