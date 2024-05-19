package com.bdtx.mod_network.Utils

import com.bdtx.mod_network.Api.RequestInterface
import com.bdtx.mod_network.Api.RxRequestInterface


object RequestUtils {
    val API by lazy { RetrofitUtils.createAPI(RequestInterface::class.java) }  // Flow
    val rxAPI by lazy { RetrofitUtils.createRxAPI(RxRequestInterface::class.java) }  // RxJava
}