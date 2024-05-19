package com.bdtx.mod_network.Response;


public class BaseResponse<T> {

    public T data = null;
    public int code = 0;
    public String errorMsg = "";
    public boolean isSuccessful(){
        return code==0;
    }
    @Override
    public String toString() {
        return "BaseRequest{" +
                "code=" + code +
                ", errorMsg='" + errorMsg + '\'' +
                ", data=" + data +
                '}';
    }
}
