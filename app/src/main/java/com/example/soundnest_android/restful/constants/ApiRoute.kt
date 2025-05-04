package restful.constants

object ApiRoutes {
    const val BASE_URL = "https://localhost:6969"

    // Auth endpoints
    const val AUTH_LOGIN = "/api/auth/login"
    const val AUTH_SEND_CODE_EMAIL = "/api/auth/sendCodeEmail"
    const val AUTH_VERIFY_CODE = "/api/auth/verifiCode"

    // User endpoints
    const val USER_NEW_USER = "/api/user/newUser"
    const val USER_EDIT_USER = "/api/user/editUser"

    // Comment endpoints
    const val COMMENT_CREATE = "/api/comment/comment"
    const val COMMENT_GET_BY_SONG_ID = "/api/comment/getComment/{song_id}/comments"
    const val COMMENT_GET_BY_ID = "/api/comment/getComment/comment/{id}"
    const val COMMENT_DELETE = "/api/comment/delete/{id}"

    // Notification endpoints
    const val NOTIFICATION_CREATE = "/api/notifications/createNotification"
    const val NOTIFICATION_GET_BY_ID = "/api/notifications/{id}/notification"
    const val NOTIFICATION_GET_BY_USER_ID = "/api/notifications/getNotifications/{userId}"
    const val NOTIFICATION_DELETE = "/api/notifications/delete/{id}"
    const val NOTIFICATION_MARK_AS_READ = "/api/notifications/notification/{id}/read"
}
