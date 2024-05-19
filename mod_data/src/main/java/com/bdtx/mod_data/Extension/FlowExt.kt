package com.bdtx.mod_data.Extension

import com.bdtx.mod_network.Error.ApiException
import com.bdtx.mod_network.Error.ExceptionHandler
import com.bdtx.mod_network.Error.OtherException
import com.bdtx.mod_network.Response.BaseResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


/**
 * 通过flow执行请求，需要在协程作用域中执行
 * @param requestCall 执行的请求
 * @param errorHandler 错误回调
 * @return 请求结果
 */
suspend fun <T> requestFlow(
    before: (() -> Unit)? = null,  // 前置准备
    after: (() -> Unit)? = null,  // 后置工作
    requestCall: suspend () -> BaseResponse<T>?,
    errorHandler: ((Int?, String?) -> Unit)? = null
): T? {
    var data: T? = null
    val flow = requestFlowResponse(before, after, requestCall, errorHandler)
    // 调用collect获取emit()回调的结果，最后的结果
    flow.collect {
        data = it?.data
    }
    return data
}

/**
 * 通过flow执行请求，需要在协程作用域中执行
 * @param requestCall 执行的请求
 * @param errorHandler 错误回调
 * @return Flow<BaseResponse<T>?>
 */
suspend fun <T> requestFlowResponse(
    before: (() -> Unit)? = null,  // 前置准备
    after: (() -> Unit)? = null,  // 后置工作
    requestCall: suspend () -> BaseResponse<T>?,  // 请求
    errorHandler: ((Int?, String?) -> Unit)? = null,  // 错误处理
): Flow<BaseResponse<T>?> {
    val flow = flow {
        // 设置10秒超时
        val response = withTimeout(10 * 1000) {
            requestCall()
        }
        if(response!=null){
            if (!response.isSuccessful()) {
                throw ApiException(response.code, response.errorMsg)
            }
        }else{
            throw OtherException(6666,"无法获取数据")
        }
        emit(response)  // 发送网络请求结果回调
    }.flowOn(Dispatchers.IO) // 指定运行线程，flow{} 执行的线程
        .onStart {
            before?.invoke()  // 前置准备，一般是弹出加载框
        }
        // 请求完成：成功/失败
        .onCompletion {
            after?.invoke()
        }
        // 捕获异常
        .catch { e ->
            e.printStackTrace()
            val exception = ExceptionHandler.handleException(e)
            errorHandler?.invoke(exception.errCode, exception.errMsg)
        }

    return flow
}

