package com.example.soundnest_android.grpc.interceptors

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor

class AuthInterceptor(private val tokenProvider: () -> String?) : ClientInterceptor {
    companion object {
        private val AUTHORIZATION_METADATA_KEY: Metadata.Key<String> =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                val token = tokenProvider()
                if (!token.isNullOrEmpty()) {
                    headers.put(AUTHORIZATION_METADATA_KEY, "Bearer $token")
                }
                super.start(responseListener, headers)
            }
        }
    }
}
