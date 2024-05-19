package com.bdtx.mod_network.Api

import com.bdtx.mod_network.Constant.ACCOUNT_PWD_LOGIN
import com.bdtx.mod_network.Constant.BIND_DEVICE
import com.bdtx.mod_network.Constant.CODE_LOGIN
import com.bdtx.mod_network.Constant.CREATE_ROOMS
import com.bdtx.mod_network.Constant.DEVICE_LIST
import com.bdtx.mod_network.Constant.GET_DEVICE_TYPE
import com.bdtx.mod_network.Constant.GET_USAGE_TYPE
import com.bdtx.mod_network.Constant.GET_USER_INFO
import com.bdtx.mod_network.Constant.LIVE_ROOMS
import com.bdtx.mod_network.Constant.PWD_UPDATE
import com.bdtx.mod_network.Constant.SEND_PHONE_CODE
import com.bdtx.mod_network.Constant.TOKEN_LOGIN
import com.bdtx.mod_network.Constant.UNBIND_DEVICE
import com.bdtx.mod_network.Constant.WAREHOUSING
import com.bdtx.mod_network.Constant.WX_LOGIN
import com.bdtx.mod_network.Response.Account
import com.bdtx.mod_network.Response.BaseResponse
import com.bdtx.mod_network.Response.Room
import com.bdtx.mod_network.Response.Rooms
import com.bdtx.mod_network.Response.TerminalType
import com.bdtx.mod_network.Response.UseScope
import com.bdtx.mod_network.Response.WarehousingDevice
import com.bdtx.mod_network.Constant.*
import com.bdtx.mod_network.Response.Chat
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface RequestInterface {

    @POST(ACCOUNT_PWD_LOGIN)
    suspend fun pwdLogin(@Body hashMap: HashMap<String, String>): BaseResponse<Account>

    @POST(SEND_PHONE_CODE)
    suspend fun sendPhoneCode(@Query("mode") mode: String, @Query("to") phone: String): BaseResponse<String>

    @POST(CODE_LOGIN)
    suspend fun codeLogin(@Body hashMap: HashMap<String, String>): BaseResponse<Account>

    @POST(TOKEN_LOGIN)
    suspend fun tokenLogin(@Body hashMap: HashMap<String, String>): BaseResponse<Account>

    @POST(WX_LOGIN)
    suspend fun wxLogin(@Query("code") code: String, @Query("encryptedData") encryptedData: String, @Query("iv") iv: String, @Query("phone") phone: String): BaseResponse<Account>

    @PUT(PWD_UPDATE)
    suspend fun updatePwd(@Header("Authorization") token: String, @Body hashMap: HashMap<String, String>): BaseResponse<String>

    @POST(BIND_DEVICE)
    suspend fun bindDevice(@Header("Authorization") token: String, @Body hashMap: HashMap<String, String>): BaseResponse<String>

    @GET(DEVICE_LIST)
    suspend fun getDeviceList(@Header("Authorization") token: String): BaseResponse<List<String>>

    @POST(UNBIND_DEVICE)
    suspend fun unbindDevice(@Header("Authorization") token: String, @Body hashMap: HashMap<String, String>): BaseResponse<String>

    @GET(GET_USER_INFO)
    suspend fun getUserInfo(@Header("Authorization") token: String): BaseResponse<Account>

    @GET(LIVE_ROOMS)
    suspend fun getRooms(@Header("Authorization") token: String, @Query("page") page: String, @Query("pageSize") pageSize: String): BaseResponse<Rooms>

    @Multipart
    @POST(CREATE_ROOMS)
    suspend fun createRooms(@Header("Authorization") token: String, @QueryMap params: HashMap<String,String>, @Part image: MultipartBody.Part): BaseResponse<Room>

    @GET
    suspend fun getRoomInfo(@Url url: String, @Header("Authorization") token: String, @Query("id") id: String): BaseResponse<Room>

    @POST
    suspend fun sendLiveMsg(@Url url: String, @Header("Authorization") token: String, @Query("id") id: String, @Body content: HashMap<String, String>): BaseResponse<Chat>

    @GET
    suspend fun getChatList(@Url url: String, @Header("Authorization") token: String, @Query("id") id: String, @Query("page") page: String, @Query("pageSize") pageSize: String): BaseResponse<Room>

    @PUT
    suspend fun closeLive(@Url url: String, @Header("Authorization") token: String, @Query("id") id: String): BaseResponse<String>

    @GET
    suspend fun getLiveTrack(@Url url: String, @Header("Authorization") token: String, @Query("id") id: String): BaseResponse<Room>

    // 管理后台
    @GET(GET_DEVICE_TYPE)
    suspend fun getDeviceType(): BaseResponse<List<TerminalType>>

    @GET(GET_USAGE_TYPE)
    suspend fun getUsageType(): BaseResponse<List<UseScope>>

    @POST(WAREHOUSING)
    suspend fun warehousing(@Header("Authorization") token: String, @Body hashMap: HashMap<String, String>): BaseResponse<WarehousingDevice>
}