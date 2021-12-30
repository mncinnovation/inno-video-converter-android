package id.mncinnovation.compressingvideo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.mncgroup.innovideoconverter.*
import id.mncinnovation.compressingvideo.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private val TAG = "CompressingVideo"
    lateinit var binding: ActivityMainBinding
    var fileUriVideo: Uri? = null
    var fileUriVideoString: String? = null
    lateinit var innoVideoConverter: InnoVideoConverter

    private val resultLauncherSaveFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.data
                data?.let { uri ->
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    object : id.mncinnovation.compressingvideo.AsyncTask(this@MainActivity) {
                        override fun doInBackground() {
                            try {
                                val out = contentResolver.openOutputStream(uri)
                                val `in`: InputStream = FileInputStream(fileUriVideoString)
                                val buffer = ByteArray(1024)
                                var read: Int
                                while (`in`.read(buffer).also { read = it } != -1) {
                                    out!!.write(buffer, 0, read)
                                }
                                `in`.close()
                                // write the output file (You have now copied the file)
                                out?.flush()
                                out?.close()

                            } catch (e: IOException) {
                                Log.d("TAG", "Error Occured" + e.message)
                            }
                        }

                        override fun onPostExecute() {
                            binding.btnSaveFile.visibility = View.GONE
                            binding.tvStateProcess.text = "File saved"
                            binding.btnCompress.text = "Compress"
                            Toast.makeText(this@MainActivity, "File saved", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }.execute()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initInnoVideoConverter()

        val resultLauncherOpenFile =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (result.data != null) {
                        val uri: Uri? = result.data?.data
                        uri?.let {
                            try {
                                fileUriVideo = it
                                binding.btnSaveFile.visibility = View.GONE
                                binding.btnCompress.isEnabled = true
                                binding.tvStateProcess.text = "File ready"
                            } catch (e: Exception) {
                                Toast.makeText(this, "Gagal mengambil video", Toast.LENGTH_LONG)
                                    .show()
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

        binding.btnOpenFile.setOnClickListener {
            //create an intent to retrieve the video file from the device storage
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "video/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )

            resultLauncherOpenFile.launch(intent)
        }

        binding.btnCancel.setOnClickListener {
            innoVideoConverter.cancel()
        }

        binding.btnCompress.setOnClickListener {
            fileUriVideo?.let { fileUriVideo ->
                innoVideoConverter.compressVideoQuality(
                    fileUriVideo,
                    QualityOption.LOW,
                    InnoVideoScale(-2, 720),
                    EncodingSpeedOption.FASTER
                )
            }
        }

        binding.btnSaveFile.setOnClickListener {
            if (fileUriVideo != null) {
                createFile("VID-" + System.currentTimeMillis() / 1000)
            } else Toast.makeText(this@MainActivity, "Please upload video", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun initInnoVideoConverter() {
        innoVideoConverter = InnoVideoConverter(this, object : InnoVideoConverterCallback {
            override fun onProgress(progress: Boolean, percent: Double) {
                Log.e(InnoVideoConverter.TAG, "percentProgress $percent")
                if (progress) {
                    binding.btnCancel.visibility = View.VISIBLE
                    binding.tvStateProcess.text = "Compressing file"
                    binding.pbCompress.visibility = View.VISIBLE
                    binding.btnCompress.visibility = View.GONE
                } else {
                    binding.btnCancel.visibility = View.GONE
                    binding.btnCompress.visibility = View.VISIBLE
                    binding.pbCompress.visibility = View.GONE
                }
            }

            override fun onSuccessConverted(message: String, newUriFileConverted: String) {
                fileUriVideoString = newUriFileConverted
                binding.btnCompress.text = "Compressed"
                binding.tvStateProcess.text = "File Compressed"
                binding.btnSaveFile.visibility = View.VISIBLE
                Toast.makeText(
                    this@MainActivity,
                    "Compress success",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onErrorConvert(message: String) {
                binding.btnCompress.isEnabled = true
                binding.btnCompress.text = "Compress"
                binding.tvStateProcess.text = "Compress Failed"
                Log.e(TAG, "error: $message")
                Toast.makeText(
                    this@MainActivity,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onCanceledConvert(message: String) {
                binding.tvStateProcess.text = "Compress Canceled"
                Log.e(TAG, "cancel: $message")
            }
        })
    }

    private fun createFile(fileName: String?) {

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // file type
        intent.type = "video/mp4"
        // file name
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
        resultLauncherSaveFile.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            deleteTempFiles(cacheDir)
        }
    }

    private fun deleteTempFiles(file: File): Boolean {
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (f in files) {
                    if (f.isDirectory) {
                        deleteTempFiles(f)
                    } else {
                        f.delete()
                    }
                }
            }
        }
        return file.delete()
    }
}