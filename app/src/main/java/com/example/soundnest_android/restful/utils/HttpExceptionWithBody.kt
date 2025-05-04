package restful.utils

import retrofit2.Response

class HttpExceptionWithBody(
    val response: Response<*>,
    override val message: String
) : Exception(message)