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
  implementation 'com.github.mncinnovation:inno-video-converter-android:0.1.6'
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
  <version>0.1.6</version>
</dependency>
```

### Implement the code

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
        innoVideoConverter = InnoVideoConverter(this)
    }
}
```

2. Call the function of convert filter as you need from object innoVideoConverter.

```kotlin
    innoVideoConverter.compressVideoQuality(
        1,
        fileUriVideo,
        QualityOption.LOW,
        InnoVideoScale(-2, 720),
        EncodingSpeedOption.FASTER,
        object : InnoVideoConverterCallback {
            override fun onProgress(progress: Boolean, percent: Double) {
    
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
        }
    )
```
3. To cancel converting/ compression process, call ``cancel`` to cancel all of existing process or ``cancel(tag)`` to cancel specific process by given tag.
```kotlin
    btnCancel.setOnClickListener {
      innoVideoConverter.cancel()
    }
```
```kotlin
    btnCancel.setOnClickListener {
      innoVideoConverter.cancel(1)
    }
```

## License

```
Copyright 2021 MNC Innovation Center

Proprietary license
```
