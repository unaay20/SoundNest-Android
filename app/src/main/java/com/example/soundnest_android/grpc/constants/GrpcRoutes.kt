package com.example.soundnest_android.grpc.constants

object GrpcRoutes {
    private var host: String = "192.168.100.42"
    private var port: Int = 50051

    fun getHost(): String = host
    fun setHost(value: String) { host = value }

    fun getPort(): Int = port
    fun setPort(value: Int) { port = value }
}