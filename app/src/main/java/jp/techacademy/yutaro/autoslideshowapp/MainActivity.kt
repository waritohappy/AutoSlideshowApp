package jp.techacademy.yutaro.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    private val TIME_FOR_SHOW:Long = 2000
    private val mUriArray = arrayListOf<Uri>()
    private var mUriNum: Int = 0
    private var mHandler = Handler()
    private var mTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        back_button.setOnClickListener {
            if(mTimer ==null) {
                backImage()
            }
        }
        forward_button.setOnClickListener {
            if(mTimer ==null) {
                forwardImage()
            }
        }

        show_button.setOnClickListener {
            if (mTimer == null) {   //再生ボタン押下可能であれば
                mTimer = Timer()
                mHandler.post{
                    show_button.text = "停止"
                }
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            forwardImage()
                        }
                    }
                }, TIME_FOR_SHOW, TIME_FOR_SHOW)
            }else{
                mHandler.post{
                    show_button.text = "再生"
                }
                show_button.text = "再生"
                mTimer!!.cancel()
                mTimer=null
            }
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
                }else{
                    this.finish()
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
        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                mUriArray.add(
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        setImage()
    }

    private fun backImage() {
        if (mUriNum == 0) {
            mUriNum = mUriArray.size - 1
        } else {
            mUriNum -= 1
        }
        setImage()
    }

    private fun forwardImage() {
        if (mUriNum == mUriArray.size - 1) {
            mUriNum = 0
        } else {
            mUriNum += 1
        }
        setImage()
    }

    private fun setImage() {
        imageView.setImageURI(mUriArray[mUriNum])
    }
}