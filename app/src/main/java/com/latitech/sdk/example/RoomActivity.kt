package com.latitech.sdk.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.latitech.sdk.example.databinding.ActivityRoomBinding
import com.latitech.sdk.whiteboard.WhiteBoardAPI
import com.latitech.sdk.whiteboard.listener.ScreenshotsCallback
import kotlinx.android.synthetic.main.activity_room.*
import org.jetbrains.anko.alert
import java.io.File
import java.nio.ByteBuffer

/**
 * 白板房间
 *
 * @author 超悟空
 * @version 1.0 2018/3/15
 * @since 1.0 2018/3/15
 **/
class RoomActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "RoomActivity"

        /**
         * 传递房间data
         */
        const val ROOM_DATA_TAG = "room_data_tag"
    }

    /**
     * 相册请求码
     */
    private val REQUEST_CODE_PICTURE = 100

    /**
     * 文件请求码
     */
    private val REQUEST_CODE_FILE = 101

    /**
     * 拍照请求码
     */
    private val REQUEST_CODE_CAMERA = 102

    /**
     * 临时图片路径
     */
    private var imageTempPath = ""

    /**
     * 视图模型
     */
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(RoomViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityRoomBinding = DataBindingUtil.setContentView(this, R.layout.activity_room)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.join(intent.getStringExtra(ROOM_DATA_TAG))
    }

    override fun onContentChanged() {

        white_board.setZOrderMediaOverlay(true)

        insert_image.setOnClickListener {
            onUploadPicture()
        }

        camera.setOnClickListener {
            onOpenCamera()
        }

        insert_file.setOnClickListener {
            onUploadFile()
        }

        pan.setOnClickListener {
            viewModel.sendWhiteBoardCommand("updateInputMode", mapOf(
                    "mode" to 0 // 笔模式
            ))
        }

        var black = true

        pan_color.setOnClickListener {
            black = !black
            if (black) {
                viewModel.sendWhiteBoardCommand("updatePenStyle", mapOf(
                        "type" to 0, // 普通笔
                        "color" to "#FFFF0000",
                        "size" to 6
                ))
            } else {
                viewModel.sendWhiteBoardCommand("updatePenStyle", mapOf(
                        "type" to 1, // 荧光笔
                        "color" to "#5F8F5CF3",
                        "size" to 24
                ))
            }
        }

        eraser.setOnClickListener {
            viewModel.sendWhiteBoardCommand("updateInputMode", mapOf(
                    "mode" to 1 // 橡皮模式
            ))
        }

        pre_page.setOnClickListener {
            viewModel.currentPage.value?.let {
                // 找出前一页的id
                val index = viewModel.bucket.value?.pageList?.indexOf(it) ?: -1
                if (index > 0) {
                    viewModel.bucket.value?.pageList?.get(index - 1)?.pageId
                } else {
                    null
                }
            }?.let {
                viewModel.sendWhiteBoardCommand("cutDocument", mapOf(
                        "widgetId" to it // 传递目标页的id
                ))
            }
        }

        next_page.setOnClickListener {
            viewModel.currentPage.value?.let { page ->
                // 找出后一页的id
                viewModel.bucket.value?.pageList?.let {
                    val index = it.indexOf(page)
                    if (index in 0 until it.size - 1) {
                        it[index + 1].pageId
                    } else {
                        null
                    }
                }
            }?.let {
                viewModel.sendWhiteBoardCommand("cutDocument", mapOf(
                        "widgetId" to it // 传递目标页的id
                ))
            }
        }

        new_page.setOnClickListener {
            viewModel.sendWhiteBoardCommand("newDocument")
        }

        screenshots.setOnClickListener {
            WhiteBoardAPI.screenshots(object : ScreenshotsCallback {
                override fun onSuccess(buffer: ByteBuffer, width: Int, height: Int) {
                    runOnUiThread {
                        alert {
                            customView = ImageView(this@RoomActivity).apply {
                                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                bitmap.copyPixelsFromBuffer(buffer)
                                setImageBitmap(bitmap)
                                scaleType = ImageView.ScaleType.FIT_XY
                            }

                            show()
                        }
                    }
                }

                override fun onFailed() {
                }
            })
        }
    }

    private fun onOpenCamera() {

        imageTempPath = FileUtil.createImagePath(this)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtil.createImageUri(this, imageTempPath))
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }

    /**
     * 上传一张图片
     */
    private fun onUploadPicture() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICTURE)
    }

    /**
     * 上传文件
     */
    private fun onUploadFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE_FILE)
    }

    /**
     * 照相成功
     */
    private fun onCameraSuccess() {

        val file = File(imageTempPath)

        if (file.exists()) {
            viewModel.sendWhiteBoardCommand("insertFile", mapOf(
                    "path" to imageTempPath,
                    "name" to file.name,
                    "left" to 0,
                    "top" to 0
            ))
        }
    }

    /**
     * 相片选择成功
     *
     * @param uri 文件地址
     */
    private fun onPictureSuccess(uri: Uri) {
        val path = FileUtil.getPathFromUri(this, uri)

        if (path != null) {
            Log.v(TAG, "onPictureSuccess path:$path")
            val name = path.substringAfterLast('/')

            viewModel.sendWhiteBoardCommand("insertFile", mapOf(
                    "path" to path,
                    "name" to name,
                    "left" to 0,
                    "top" to 0
            ))
        }
    }

    /**
     * 文件选择成功
     *
     * @param uri 文件地址
     */
    private fun onFileSuccess(uri: Uri) {
        val path = FileUtil.getPathFromUri(this, uri)

        if (path != null) {
            Log.v(TAG, "onFileSuccess path:$path")
            val name = path.substringAfterLast('/')

            viewModel.sendWhiteBoardCommand("insertFile", mapOf(
                    "path" to path,
                    "name" to name,
                    "left" to 0,
                    "top" to 0
            ))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICTURE -> {
                    data?.data?.let { onPictureSuccess(it) }
                }
                REQUEST_CODE_FILE -> {
                    data?.data?.let { onFileSuccess(it) }
                }
                REQUEST_CODE_CAMERA -> {
                    onCameraSuccess()
                }
            }
        }
    }
}