package com.latitech.sdk.example

/**
 * 常量表
 *
 * @author 超悟空
 * @version 1.0 2019/10/29
 * @since 1.0 2019/10/29
 **/

/**
 * 白板长连接地址
 */
const val SDK_WEB_SOCKET_HOST = "wss://deskboard.efaceboard.cn:8081"

/**
 * 白板文件服务器地址
 */
const val SDK_FILE_HOST = "https://deskboard.efaceboard.cn:8082"

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
const val OSS_BUCKET = "trainboard"