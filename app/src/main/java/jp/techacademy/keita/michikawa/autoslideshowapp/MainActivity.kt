package jp.techacademy.keita.michikawa.autoslideshowapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.content.ContentUris
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.util.Log
import java.util.*
import jp.techacademy.keita.michikawa.autoslideshowapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val PERMISSIONS_REQUEST_CODE = 100

    private var idImage: Long = 0
    private var idFirst: Long = 0
    private var idLast: Long = 0
    private var imageUri: Uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 0)

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0

    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            if(binding.btnPlayPause.text == "再生") {
                if (idImage == idLast) {
                    idImage = idFirst
                } else {
                    idImage++
                }
                imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idImage)
                binding.imageView.setImageURI(imageUri)
                Log.d("ANDROID_SLIDE", "URI : " + idImage.toString())
            }
        }

        binding.btnBack.setOnClickListener {
            if(binding.btnPlayPause.text == "再生") {
                if (idImage == idFirst) {
                    idImage = idLast
                } else {
                    idImage--
                }
                imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idImage)
                binding.imageView.setImageURI(imageUri)
                Log.d("ANDROID_SLIDE", "URI : " + idImage.toString())
            } else {
                binding.btnPlayPause.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            }
        }

        binding.btnPlayPause.setOnClickListener {
            if (mTimer == null){
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 2.0

                        binding.btnPlayPause.text = "停止"

                        if(idImage == idLast) {
                            idImage = idFirst
                        } else {
                            idImage++
                        }

                        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idImage)

                        mHandler.post {
                            binding.imageView.setImageURI(imageUri)
                            binding.btnNext.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                            binding.btnNext.isClickable = false
                            binding.btnBack.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                            binding.btnBack.isClickable = false
                        }

                        Log.d("ANDROID_SLIDE", "minURI : " + idFirst.toString())
                        Log.d("ANDROID_SLIDE", "maxURI : " + idLast.toString())
                        Log.d("ANDROID_SLIDE", "nowURI : " + idImage.toString())

                    }
                }, 100, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を2000ミリ秒 に設定
            } else {
                mTimer!!.cancel()
                mTimer = null
                binding.btnPlayPause.text = "再生"
                mHandler.post {
                    binding.btnNext.backgroundTintList = ColorStateList.valueOf(Color.rgb(255,140,0))
                    binding.btnNext.isClickable = true
                    binding.btnBack.backgroundTintList = ColorStateList.valueOf(Color.rgb(255,140,0))
                    binding.btnBack.isClickable = true
                }
            }
        }


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        Log.d("ANDROID", "Getting Contents Info")

        if (cursor!!.moveToFirst()) {
            // 最初の画像のIDを取得
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            idFirst = cursor.getLong(fieldIndex)
            Log.d("ANDROID_SLIDE", "URI : " + idFirst.toString())
        }
        if (cursor!!.moveToLast()) {
            // 最後の画像のIDを取得
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            idLast = cursor.getLong(fieldIndex)
            Log.d("ANDROID_SLIDE", "URI : " + idLast.toString())
        }
        cursor.close()

        idImage = idFirst
        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idImage)
        binding.imageView.setImageURI(imageUri)
        Log.d("ANDROID_SLIDE", "minURI : " + idFirst.toString())
        Log.d("ANDROID_SLIDE", "maxURI : " + idLast.toString())
        Log.d("ANDROID_SLIDE", "nowURI : " + idImage.toString())

    }
}