package com.latitech.sdk.example

import org.cwk.android.library.network.communication.ICommunication
import org.cwk.android.library.network.factory.CommunicationBuilder
import org.cwk.android.library.work.SimpleWorkModel
import org.json.JSONObject

/**
 * 任意网络请求
 *
 * @author 超悟空
 * @version 1.0 2019/8/26
 * @since 1.0 2019/8/26
 **/
class AnyWork : SimpleWorkModel<Any, String>() {

    override fun onSuccessExtract(jsonResult: JSONObject): String =
            jsonResult.getString(RESULT)

    override fun onTaskUri(): String = mParameters[0] as String

    override fun onFill(dataMap: MutableMap<String, String>, vararg parameters: Any) {
        val json = parameters[2] as JSONObject

        for (key in json.keys()) {
            dataMap[key] = json.getString(key)
        }
    }

    override fun onInterceptCreateCommunication(): ICommunication<*, *> {
        return CommunicationBuilder(TAG, mParameters[1] as Int).build()
    }
}