package com.latitech.sdk.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.latitech.sdk.whiteboard.WhiteBoardAPI
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * 房间id
         */
        private const val ROOM_ID = "替换为房间id串"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化白板的各种服务器地址，应用启动仅需在主线程执行一次，通常应该在application中初始化
        WhiteBoardAPI.init(this, BuildConfig.DEBUG)
        WhiteBoardAPI.setWebSocketUrl(SDK_WEB_SOCKET_HOST)
        WhiteBoardAPI.initOss(OSS_END_POINT, OSS_STS_URL, OSS_BUCKET)

        join.setOnClickListener {
            // 加入房间
            val params = JoinParams(
                ROOM_ID,
                9,
                UUID.randomUUID().toString(),
                "aa"
            )

            startActivity<RoomActivity>(RoomActivity.ROOM_DATA_TAG to params.toString())
        }
    }
}
