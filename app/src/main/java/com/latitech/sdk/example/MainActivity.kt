package com.latitech.sdk.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.latitech.sdk.example.databinding.ActivityMainBinding
import com.latitech.sdk.whiteboard.WhiteBoardAPI
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * 房间id
         */
        private const val ROOM_ID = "6341efdd43b548989c60e7546a7b8f81"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化白板的各种服务器地址，应用启动仅需在主线程执行一次，通常应该在application中初始化
        WhiteBoardAPI.init(this, BuildConfig.DEBUG)
        WhiteBoardAPI.setWebSocketUrl(SDK_WEB_SOCKET_HOST)
        WhiteBoardAPI.initOss(OSS_END_POINT, OSS_STS_URL, OSS_BUCKET)
        WhiteBoardAPI.useSkia(true)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.join.setOnClickListener {
            // 加入房间
            val params = JoinParams(
                    ROOM_ID,
                    5,
                    "c801e228-a612-4524-97e9-5829f3cf842e",
                    "aa",
                    ""
            )

            startActivity<RoomActivity>(RoomActivity.ROOM_DATA_TAG to params.toString())
        }
    }
}
