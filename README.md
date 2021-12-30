# Inno Video Converter Android

An simple way to manipulating your video on Android.

## How to use

### Add to Project

Gradle

```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

dependencies {
  implementation 'com.github.mncinnovation:inno-video-converter-android:0.1.0'
}
```

or Maven

```
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.mncinnovation</groupId>
  <artifactId>inno-video-converter-android</artifactId>
  <version>0.1.0</version>
</dependency>
```

### How to Use

1. Add and declare object of class InnoVideoConverter.

```kotlin
class MainActivity : AppCompatActivity() {
    lateinit var innoVideoConverter: InnoVideoConverter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initInnoVideoConverter()

    }

    private fun initInnoVideoConverter() {
        innoVideoConverter = InnoVideoConverter(this, object : InnoVideoConverterCallback {
            override fun onProgress(progress: Boolean) {
                
            }
            override fun onSuccessConverted(message: String, newUriFileConverted: String) {
                Log.i("InnoVideoConverter", "success : $message")
            }

            override fun onErrorConvert(message: String) {
                Log.i("InnoVideoConverter", "error : $message")
            }

            override fun onCanceledConvert(message: String) {
                Log.i("InnoVideoConverter", "cancelled : $message")
            }
        })
    }
}
```

2. Call the function of convert filter as you need from object innoVideoConverter.

```kotlin
    innoVideoConverter.convertFilterPixelFormat(fileUriVideo, "yuv444p")
```

## License

```
Copyright 2021 MNC Innovation Center

Proprietary license
```
