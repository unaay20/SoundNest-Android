package com.example.soundnest_android.restful.constants

object RestfulRoutes {
    //http://192.168.100.42/
    //https://10.0.2.2:6969/
    //https://localhost:6969/
    //https://allowing-walrus-classic.ngrok-free.app/
    //https://100.65.158.22/
    //https://10.0.2.2:6969/
    private var baseUrl = "http://100.65.158.22/"

    // Auth endpoints
    const val AUTH_LOGIN = "api/auth/login"
    const val AUTH_SEND_CODE_EMAIL = "api/auth/sendCodeEmail"
    const val AUTH_VERIFY_CODE = "api/auth/verifiCode"
    const val AUTH_VALIDATE_JWT = "api/auth/validateJWT"
    const val AUTH_UPDATE_FCM_TOKEN = "/api/auth/fcm/update"

    // User endpoints
    const val USER_NEW_USER = "api/user/newUser"
    const val USER_EDIT_USER = "api/user/editUser"
    const val USER_EDIT_PASSWORD = "api/user/editUserPassword"
    const val USER_GET_ADDITIONAL_INFO = "api/user/get/aditionalInfo"

    // Comment endpoints
    const val COMMENT_RESPOND = "api/comment/{commentId}/respondComment"
    const val COMMENT_CREATE = "api/comment/createComment"
    const val COMMENT_GET_BY_SONG_ID = "api/comment/getComment/{song_id}/song"
    const val COMMENT_GET_BY_ID = "api/comment/{id}/all"
    const val COMMENT_DELETE = "api/comment/delete/{id}"

    // Notification endpoints
    const val NOTIFICATION_CREATE = "api/notifications/createNotification"
    const val NOTIFICATION_GET_BY_ID = "api/notifications/{id}/notification"
    const val NOTIFICATION_GET_BY_USER_ID = "api/notifications/getNotifications/{userId}"
    const val NOTIFICATION_DELETE = "api/notifications/delete/{id}"
    const val NOTIFICATION_MARK_AS_READ = "api/notifications/notification/{id}/read"

    //Song endpoints
    const val SONG_DELETE = "api/songs/{idsong}/delete"
    const val SONG_PATCH_SONG_IMAGE = "api/songs/{idsong}/image"
    const val SONG_GET_LATEST_SONG = "api/songs/user/{idAppUser}/lastest"
    const val SONG_GET_SONGS_BY_USERID = "api/songs/user/{idAppUser}"
    const val SONG_GET_SONG_SEARCH_FILTERS = "api/songs/search"
    const val SONG_GET_POPULAR_SONGS = "api/songs/{amount}/popular/{year}/{month}"
    const val SONG_GET_RECENT_SONGS = "api/songs/{amount}/recent"
    const val SONG_GET_RANDOM_SONGS = "api/songs/{amount}/random"
    const val SONG_GET_BY_ID = "api/songs/{idsong}/song"
    const val SONG_GET_GENRES = "api/songs/genres"
    const val SONG_GET_EXTENSIONS = "songs/extensions"

    //Playlist endpoints
    const val PLAYLIST_GET_BY_USERID = "api/playlist/{iduser}/user"
    const val PLAYLIST_PATCH_REMOVE_SONG = "api/playlist/{idsong}/{idPlaylist}/remove"
    const val PLAYLIST_PATCH_ADD_SONG = "api/playlist/{idsong}/{idPlaylist}/add"
    const val PLAYLIST_DELETE = "api/playlist/{idPlaylist}/delete/"
    const val PLAYLIST_PUT_NEW_PLAYLIST = "api/playlist/upload"

    //Visualization endpoints
    const val VISUALIZATION_INCREMENT = "api/visit/{idsong}/increment"
    const val VISUALIZATION_GET_BY_MONTH = "api/visit/top/{year}/{month}"
    const val VISUALIZATION_GET_BY_SONG_ID_IN_MONTH = "api/visit/{idsong}/{year}/{month}"
    const val VISUALIZATION_GET_BY_SONG_ID = "api/visit/{idsong}"

    fun getBaseUrl(): String = baseUrl
    fun setBaseUrl(value: String) { baseUrl = value }
}
