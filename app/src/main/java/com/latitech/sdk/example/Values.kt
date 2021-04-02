package com.latitech.sdk.example

/**
 * 常量表
 *
 * @author 超悟空
 * @version 1.0 2019/10/29
 * @since 1.0 2019/10/29
 **/

///**
// * 白板长连接地址
// */
//const val SDK_WEB_SOCKET_HOST = "ws://192.168.0.24:8081"
//
///**
// * 白板文件服务器地址
// */
//const val SDK_FILE_HOST = "http://192.168.0.24:8082"
//
///**
// * 白板接口地址
// */
//const val SDK_API_HOST = "http://192.168.0.24:8888"

/**
 * 白板长连接地址
 */
const val SDK_WEB_SOCKET_HOST = "wss://sdktest.efaceboard.cn:8081"

/**
 * 白板文件服务器地址
 */
const val SDK_FILE_HOST = "https://sdkfile.efaceboard.cn:8082"

/**
 * 白板接口地址
 */
const val SDK_API_HOST = "https://sdktest.efaceboard.cn:8888"

/**
 * 阿里云OSS 节点地址
 */
const val OSS_END_POINT = "https://oss-cn-beijing.aliyuncs.com"

/**
 * 阿里云OSS 身份验证令牌获取地址
 */
const val OSS_STS_URL = "$SDK_FILE_HOST/FileServer/oss/getAKToken"

/**
 * 阿里云OSS bucket名称
 */
//const val OSS_BUCKET = "trainboard"

const val OSS_BUCKET = "deskboard"