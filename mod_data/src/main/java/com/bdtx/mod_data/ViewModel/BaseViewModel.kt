package com.bdtx.mod_data.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


open class BaseViewModel : ViewModel() {

    fun <T> launchUIWithResult(
        responseBlock: suspend () -> T?,  // 挂起函数拿到数据：无输入 → 输出T
        successBlock: (T?) -> Unit  // 处理函数
    ) {
        // 在主线程调用
        viewModelScope.launch(Dispatchers.Main) {
            val result = safeApiCallWithResult(responseBlock)  // 子线程执行获取数据
            successBlock(result)  // 成功拿到数据后在主线程处理它
        }
    }

    suspend fun <T> safeApiCallWithResult(
        responseBlock: suspend () -> T?
    ): T? {
        try {
            // 子线程执行请求函数获取数据 10秒超时
            val response = withContext(Dispatchers.IO) {
                withTimeout(10 * 1000) {
                    responseBlock()
                }
            } ?: return null  // 为空返回 null

            return response  // 返回数据
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


}