package restful.utils

class SimpleTokenProvider : TokenProvider {
    private var token: String? = null
    private var attach: Boolean = false

    fun updateToken(newToken: String) {
        token = newToken
    }

    fun enableToken(enable: Boolean) {
        attach = enable
    }

    override fun getToken() = token
    override fun shouldAttachToken() = attach
}