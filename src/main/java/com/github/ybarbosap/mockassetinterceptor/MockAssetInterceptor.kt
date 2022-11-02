package com.github.ybarbosap.mockassetinterceptor

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http.HTTP_OK
import retrofit2.Invocation
import java.io.BufferedReader
import java.io.InputStream

class MockAssetInterceptor(private val context: Context) : Interceptor {

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.request().let { request ->
            verifyAnnotationOf(request)?.takeIf { it.apply }?.let { mockAsset ->
                val asset = context.assets.open(mockAsset.asset)
                read(asset) { json ->
                    Thread.sleep(mockAsset.delay)
                    buildResponseOf(request, json)
                }
            } ?: chain.proceed(request)
        }
    }

    private fun verifyAnnotationOf(request: Request): MockAsset? {
        return request.tag(Invocation::class)?.method()?.getAnnotation(MockAsset::class.java)
    }

    private fun read(asset: InputStream, fn: (String) -> Response): Response {
        return fn(
            try {
                asset.bufferedReader().use(BufferedReader::readText)
            } catch (e: Exception) {
                Log.e(this::class.simpleName, e.stackTraceToString())
                ""
            }
        )
    }

    private fun buildResponseOf(request: Request, json: String) : Response {
        return Response.Builder()
            .protocol(Protocol.HTTP_2)
            .code(HTTP_OK)
            .request(request)
            .message(String())
            .body(json.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }
}