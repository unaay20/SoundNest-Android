package com.example.soundnest_android.grpc.services

import android.content.Context
import com.example.soundnest_android.R
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.interceptors.AuthInterceptor
import io.grpc.ManagedChannel
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import io.grpc.okhttp.OkHttpChannelBuilder

open class BaseGrpcService(
    host: String,
    port: Int,
    private val tokenProvider: () -> String?
) : Closeable {
    protected val channel: ManagedChannel

    init {
        val interceptor = AuthInterceptor(tokenProvider)


        val builder = OkHttpChannelBuilder.forAddress(host, port)
            .usePlaintext()

        channel = builder
            .intercept(interceptor)
            .build()
    }

    protected suspend fun <T> safeCall(apiCall: suspend () -> T): GrpcResult<T?> {
        return try {
            val response = apiCall()
            GrpcResult.Success(response)
        } catch (e: StatusException) {
            GrpcResult.GrpcError(e.status.code, e.message ?: "gRPC error")
        } catch (e: StatusRuntimeException) {
            GrpcResult.GrpcError(e.status.code, e.message ?: "gRPC runtime error")
        } catch (e: IOException) {
            GrpcResult.NetworkError(e)
        } catch (e: Exception) {
            GrpcResult.UnknownError(e)
        }
    }

    override fun close() {
        channel.shutdownNow()
    }
//    fun buildSslSocketFactory(context: Context): SSLSocketFactory {
//        val certificateFactory = CertificateFactory.getInstance("X.509")
//        val inputStream = context.resources.openRawResource(R.raw.server)
//        val certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate
//        inputStream.close()
//
//        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
//        keyStore.load(null, null)
//        keyStore.setCertificateEntry("server", certificate)
//
//        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
//        tmf.init(keyStore)
//
//        val sslContext = SSLContext.getInstance("TLS")
//        sslContext.init(null, tmf.trustManagers, SecureRandom())
//
//        return sslContext.socketFactory
//    }
//    fun buildSecureChannel(context: Context, host: String, port: Int): ManagedChannel {
//        val sslSocketFactory = buildSslSocketFactory(context)
//
//        val builder = io.grpc.okhttp.OkHttpChannelBuilder.forAddress(host, port)
//            .useTransportSecurity()
//
//        // acceso al método privado via reflection
//        val method = builder.javaClass.getDeclaredMethod(
//            "withSslSocketFactory",
//            SSLSocketFactory::class.java
//        )
//        method.isAccessible = true
//        method.invoke(builder, sslSocketFactory)
//
//        return builder.build()
//    }
}
