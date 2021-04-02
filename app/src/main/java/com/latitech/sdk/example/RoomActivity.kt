package com.latitech.sdk.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.latitech.sdk.example.databinding.ActivityRoomBinding
import com.latitech.sdk.whiteboard.WhiteBoardAPI
import com.latitech.sdk.whiteboard.listener.ScreenshotsCallback
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
     * 临时图片路径
     */
    private var imageTempPath = ""

    /**
     * 视图模型
     */
    private val viewModel by viewModels<RoomViewModel>()

    /**
     * 视图绑定
     */
    private val binding by lazy {
        ActivityRoomBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.join(intent.getStringExtra(ROOM_DATA_TAG))
    }

    override fun onContentChanged() {

        binding.whiteBoard.setZOrderMediaOverlay(true)

        binding.insertImage.setOnClickListener {
            openPicture.launch("image/*")
        }

        binding.camera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                imageTempPath = FileUtil.createImagePath(this)
                openCamera.launch(FileUtil.createImageUri(this, imageTempPath))
            } else {
                requestPermissionCamera.launch(Manifest.permission.CAMERA)
            }
        }

        binding.insertFile.setOnClickListener {
            openFile.launch("*/*")
        }

        binding.select.setOnClickListener {
            viewModel.sendWhiteBoardCommand(
                "updateInputMode", mapOf(
                    "mode" to 2 // 选择模式
                )
            )
        }

        binding.pan.setOnClickListener {
            viewModel.sendWhiteBoardCommand(
                "updateInputMode", mapOf(
                    "mode" to 0 // 笔模式
                )
            )
        }

        var black = true

        binding.panColor.setOnClickListener {
            black = !black
            if (black) {
                viewModel.sendWhiteBoardCommand(
                    "updatePenStyle", mapOf(
                        "type" to 0, // 普通笔
                        "color" to "#FFFF0000",
                        "size" to 6
                    )
                )
            } else {
                viewModel.sendWhiteBoardCommand(
                    "updatePenStyle", mapOf(
                        "type" to 1, // 荧光笔
                        "color" to "#5F8F5CF3",
                        "size" to 64
                    )
                )
            }
        }

        binding.eraser.setOnClickListener {
            viewModel.sendWhiteBoardCommand(
                "updateInputMode", mapOf(
                    "mode" to 1 // 橡皮模式
                )
            )
        }

        binding.restore.setOnClickListener {
            viewModel.sendWhiteBoardCommand("restore")
        }

        binding.clearRecovery.setOnClickListener {
            viewModel.sendWhiteBoardCommand("clearRecovery")
        }

        binding.prePage.setOnClickListener {
            viewModel.currentPage.value?.let {
                // 找出前一页的id
                val index = viewModel.bucket.value?.pageList?.indexOf(it) ?: -1
                if (index > 0) {
                    viewModel.bucket.value?.pageList?.get(index - 1)?.pageId
                } else {
                    null
                }
            }?.let {
                viewModel.sendWhiteBoardCommand(
                    "cutDocument", mapOf(
                        "widgetId" to it // 传递目标页的id
                    )
                )
            }
        }

        binding.nextPage.setOnClickListener {
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
                viewModel.sendWhiteBoardCommand(
                    "cutDocument", mapOf(
                        "widgetId" to it // 传递目标页的id
                    )
                )
            }
        }

        binding.newPage.setOnClickListener {
            viewModel.sendWhiteBoardCommand("newDocument")
        }

        binding.screenshots.setOnClickListener {
            WhiteBoardAPI.screenshots(object : ScreenshotsCallback {
                override fun onSuccess(buffer: ByteBuffer, width: Int, height: Int) {
                    runOnUiThread {
                        alert {
                            customView = ImageView(this@RoomActivity).apply {
                                val bitmap =
                                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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

    private val requestPermissionCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                imageTempPath = FileUtil.createImagePath(this)
                openCamera.launch(FileUtil.createImageUri(this, imageTempPath))
            }
        }

    private val openCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        val file = File(imageTempPath)

        if (file.exists()) {
            viewModel.sendWhiteBoardCommand(
                "insertFile", mapOf(
                    "path" to imageTempPath,
                    "name" to file.name,
                    "left" to 0,
                    "top" to 0
                )
            )
        }
    }

    private val openPicture = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            return@registerForActivityResult
        }

        val path = FileUtil.getPathFromUri(this, it)

        if (path != null) {
            Log.v(TAG, "onPictureSuccess path:$path")
            val name = path.substringAfterLast('/')

            viewModel.sendWhiteBoardCommand(
                "insertFile", mapOf(
                    "path" to path,
                    "name" to name,
                    "left" to 0,
                    "top" to 0
                )
            )
        }
    }

    private val openFile = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            return@registerForActivityResult
        }

        val path = FileUtil.getPathFromUri(this, it)

        if (path != null) {
            Log.v(TAG, "onFileSuccess path:$path")
            val name = path.substringAfterLast('/')

            viewModel.sendWhiteBoardCommand(
                "insertFile", mapOf(
                    "path" to path,
                    "name" to name,
                    "left" to 0,
                    "top" to 0
                )
            )
        }
    }
}