package com.bdtx.mod_network.Api;

import com.bdtx.mod_network.Constant.RequestConstantKt;
import com.bdtx.mod_network.Response.Account;
import com.bdtx.mod_network.Response.BaseResponse;
import com.bdtx.mod_network.Response.Room;

import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface RxRequestInterface {
    
    @POST(RequestConstantKt.ACCOUNT_PWD_LOGIN)
    Observable<BaseResponse<Account>> pwdLogin(@Body HashMap<String, String> hashMap);

    @PUT(RequestConstantKt.PWD_UPDATE)
    Observable<BaseResponse<String>> updatePwd(@Header("Authorization") String token, @Body HashMap<String, String> hashMap);

    @GET(RequestConstantKt.GET_USER_INFO)
    Observable<BaseResponse<Account>> getUserInfo(@Header("Authorization") String token);

    @GET
    Observable<BaseResponse<Room>> getChatList(@Url String url, @Header("Authorization") String token, @Query("id") String id, @Query("page") String page, @Query("pageSize") String pageSize);

    // 注解说明：参考 Postman
    // 传入 Headers 参数：@Header("Authorization") String token：键-"Authorization"，值-token
    // 传入 Params 参数：@Query("mode") String mode, @Query("to") String phone：键-"mode"，值-mode、键-"to"，值-phone
    // 传入 Params 参数：也可以 @QueryMap HashMap<String,String> params：创建多个键值对的 params HashMap
    // 传入 Body 的 Raw 参数：@Body HashMap<String, String> hashMap：创建 一个 HashMap 作为 JSON 格式数据

    // 传入 Body 的 x-www-form-urlencoded 参数：定义请求方式前先 @Multipart 例如：
    // @FormUrlEncoded
    // @POST(Constant.CREATE_ROOMS)
    // Observable<BaseResponse<Rooms>> createRooms(@Field("name") String name, @Field("age") int age);
    // 传入 键-"name"，值-name 的 x-www-form-urlencoded 参数，键-"age"，值-age 的 x-www-form-urlencoded 参数

    // 传入 Body 的 form-data 参数（文本/文件）：定义请求方式前先 @Multipart 例如：
    // @Multipart
    // @POST(Constant.CREATE_ROOMS)
    // Observable<BaseResponse<Rooms>> createRooms(@Header("Authorization") String token, @Part("name") RequestBody nickname , @Part MultipartBody.Part image);
    // 传入 键-"Authorization"，值-token 的 Headers 参数，键-"name"，值-nickname 的 form-data 文本参数，image 的 form-data 文件参数
}
