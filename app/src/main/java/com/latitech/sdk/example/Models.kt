package com.latitech.sdk.example

/**
 * 数据模型
 *
 * @author 超悟空
 * @version 1.0 2019/10/29
 * @since 1.0 2019/10/29
 **/

/**
 * 加入房间所需的参数
 *
 * @property roomId     房间id
 * @property roleId     角色id
 * @property userId     用户id，用户业务系统中的稳定用户id
 * @property nickname   用户名，在白板使用的用户名称
 * @property avatar     头像地址，在白板显示的用户头像
 * @property sessionId  用户会话id（可选），
 *                      如果用户业务系统中有与[userId]对应的临时id可以作为[sessionId]传递，
 *                      如果用户业务系统只有唯一的稳定用户id，则仅传递[userId]即可，[sessionId]会由白板系统自动生成
 * @property password   房间密码，空串表示无密码
 */
data class JoinParams(
    val roomId: String,
    val roleId: Int,
    val userId: String,
    val nickname: String = "",
    val avatar: String = "",
    val sessionId: String = "",
    val password: String = ""
) {

    override fun toString() = """{
                                "meetingId": "$roomId",
                                "memberVO": {
                                    "roleId": $roleId,
                                    "userId": "$userId",
                                    "nickName": "$nickname",
                                    "headPic": "$avatar",
                                    "sessionId": "$sessionId"
                                    },
                                "password":"$password"
                                }"""
}

/**
 * 白板bucket信息
 *
 * @property bucketId 所属bucket id
 * @property pageList bucket下所有页信息
 */
data class WhiteBoardBucket(
    val bucketId: String,
    val pageList: MutableList<WhiteBoardPage>
)


/**
 * 白板页数据
 *
 * @property pageId 页id
 * @property pageNumber 页号
 * @property thumbnails 白板缩略图url
 **/
data class WhiteBoardPage(
    val pageId: String,
    val pageNumber: Int,
    val thumbnails: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WhiteBoardPage

        if (pageId != other.pageId) return false

        return true
    }

    override fun hashCode(): Int {
        return pageId.hashCode()
    }
}