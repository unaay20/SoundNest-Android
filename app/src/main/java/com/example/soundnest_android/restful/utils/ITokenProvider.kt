package restful.utils

interface TokenProvider {
    fun getToken(): String?
    fun shouldAttachToken(): Boolean
}