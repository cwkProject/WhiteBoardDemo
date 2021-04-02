package com.latitech.sdk.example

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.latitech.sdk.whiteboard.WhiteBoardAPI
import com.latitech.sdk.whiteboard.listener.OnNetworkStateListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * 房间功能
 *
 * @author 超悟空
 * @version 1.0 2018/10/18
 * @since 1.0 2018/10/18
 **/
class RoomViewModel : ViewModel() {

    /**
     * 是否第一次进入
     */
    private var first = true

    /**
     * 白板宽高比
     */
    val whiteBoardRatio = MutableLiveData("2048:1440")

    /**
     * 白板bucket信息
     */
    val bucket = MutableLiveData<WhiteBoardBucket>()

    /**
     * 当前白板页
     */
    val currentPage = MutableLiveData<WhiteBoardPage>()

    init {
        // 长连接回调，非UI线程
        WhiteBoardAPI.setOnNetworkStateListener(object : OnNetworkStateListener {
            override fun onConnected() {
                Log.v(TAG, "onConnected")
            }

            override fun onDisconnected(code: Int) {
                Log.v(TAG, "onDisconnected $code")
            }
        })

        // 白板事件监听器，非UI线程，有些需要原生端配合的功能必须对接，如'fileNetwork'
        WhiteBoardAPI.setOnWhiteBoardListener { command, data, callback ->
            when (command) {
                "fileNetwork" -> {
                    // 白板中文件服务需要配合的网络请求(c++层没有集成http库)
                    val json = JSONObject(data)

                    viewModelScope.launch(Dispatchers.Default) {
                        AnyWork().execute(
                            "$SDK_FILE_HOST${json.getString("url")}",
                            json.getInt("method"),
                            json.getJSONObject("params"),
                        )?.let {
                            callback?.call(it.isSuccess, it.result ?: "")
                        }
                    }
                }
                "documentChange" -> {
                    // 翻页事件，收到新页的id，首次进入房间也会收到
                    val pageId = JSONObject(data).getString("widgetId")

                    viewModelScope.launch {
                        currentPage.value = bucket.value?.pageList?.find { it.pageId == pageId }
                    }
                }
                "recoveryState" -> {
                    // widget回收站状态
                    val notEmpty = JSONObject(data).getBoolean("notEmpty")

                    Log.v(TAG, "recoveryState not_empty:$notEmpty")
                }
            }
        }

        // 网络数据监听器，非UI线程
        WhiteBoardAPI.setOnWebSocketListener { type, subType, data ->
            when (type) {
                0 -> {
                    // 房间基本信息
                    when (subType) {
                        0 -> {
                            // 登录成功
                            val message = JSONObject(data)

                            // 读取房间信息等等
                            val roomId = message.getString("meetingId")
                            val sessionId = message.getJSONObject("session").getString("sessionId")

                            // 读取白板宽高
                            val width = message.getInt("width")
                            val height = message.getInt("height")

                            whiteBoardRatio.postValue("$width:$height")
                        }
                    }
                }
                10 -> {
                    // 白板页数据
                    when (subType) {
                        0 -> {
                            // 全部页面列表数据信息，在首次进入房间和增删页面时下发
                            val message = JSONArray(data)

                            if (message.length() > 0) {
                                // 目前仅支持单一bucket
                                val pageInfo = message.getJSONObject(0)

                                val pageArray = pageInfo.getJSONArray("documents")

                                val pageList = mutableListOf<WhiteBoardPage>()

                                // 提取页列表，服务器下发的页列表是降序的
                                for (i in pageArray.length() - 1 downTo 0) {
                                    val page = pageArray.getJSONObject(i)
                                    pageList += WhiteBoardPage(
                                        page.getString("documentId"),
                                        page.getInt("documentNo"),
                                        page.optString("url") ?: ""
                                    )
                                }

                                bucket.postValue(
                                    WhiteBoardBucket(
                                        pageInfo.getString("bucketId"),
                                        pageList
                                    )
                                )
                            }
                        }
                        1 -> {
                            // 单个页信息改变，目前仅页面缩略图改变会触发该回调
                            val message = JSONObject(data)

                            val page = WhiteBoardPage(
                                message.getString("documentId"),
                                message.getInt("documentNo"),
                                message.optString("url") ?: ""
                            )

                            val index = bucket.value?.pageList?.indexOf(page) ?: -1

                            if (index > -1) {
                                bucket.value?.pageList?.set(index, page)
                                bucket.postValue(bucket.value)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 加入房间
     */
    fun join(data: String?) {
        if (first) {
            first = false
            WhiteBoardAPI.sendCommand("openRoom", "")

            WhiteBoardAPI.login()
            WhiteBoardAPI.joinRoom(data!!)
        }
    }

    /**
     * 向白板发送命令
     *
     * @param command 命令
     * @param params 携带的参数
     */
    fun sendWhiteBoardCommand(command: String, params: Map<String, *>? = null) {
        val p = if (params == null) "" else JSONObject(params).toString()
        WhiteBoardAPI.sendCommand(command, p)
    }

    override fun onCleared() {
        WhiteBoardAPI.leaveRoom()
        WhiteBoardAPI.logout()
        WhiteBoardAPI.sendCommand("closeRoom", "")
    }

    companion object {
        private const val TAG = "RoomViewModel"
    }
}