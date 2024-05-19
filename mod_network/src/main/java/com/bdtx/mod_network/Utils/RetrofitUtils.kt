package com.bdtx.mod_network.Utils
import android.util.Log
import com.bdtx.mod_network.Constant.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

// Retrofit 网络请求工具类，kotlin 的 object 自带单例
object RetrofitUtils {

    private val TAG : String = "RetrofitUtils"
    private var builder : OkHttpClient.Builder
    private var retrofit : Retrofit.Builder

    init {
        builder = OkHttpClient.Builder()
        builder.connectTimeout(15, TimeUnit.SECONDS)  // 设置连接超时
            .readTimeout(20, TimeUnit.SECONDS)  // 设置读取超时
            .writeTimeout(20, TimeUnit.SECONDS)  // 设置写入超时
        val loggingInterceptor = HttpLoggingInterceptor { msg -> Log.e(TAG +" 返回数据", msg) }  // 不重写,部分手机平板需要打开日志
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY  // 定义打印的数据是请求体
        // 设置网络拦截器
        builder.addInterceptor(loggingInterceptor)
//        builder.addInterceptor(object : Interceptor {
//            override fun intercept(chain: Interceptor.Chain): Response {
//                if (NetworkUtil.isConnected(ApplicationUtils.getApplication())) {
//                    val request = chain.request()
//                    return chain.proceed(request)
//                } else {
//                    throw NoNetWorkException(ERROR.NETWORD_ERROR)
//                }
//            }
//        })

        retrofit = Retrofit.Builder()
            .client(builder.build())
            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))  // 让它把返回的数据转化为 json 对象
            .addConverterFactory(GsonConverterFactory.create())
    }

    fun <T> createAPI(apiService: Class<T>): T {
        return retrofit
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
            .create(apiService)
    }

    fun <T> createRxAPI(apiService: Class<T>): T {
        return retrofit
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())  // 在这里把 Retrofit 和 rxjava 关联起来，让请求接口返回的数据转化为 rxjava 的 Observable 对象
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
            .create(apiService)
    }

}