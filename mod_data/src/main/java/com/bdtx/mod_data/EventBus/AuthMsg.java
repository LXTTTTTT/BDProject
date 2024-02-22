package com.bdtx.mod_data.EventBus;

public class AuthMsg{
    public final static int AUTH_SUCCESS = 0;
    public final static int AUTH_FAIL = 1;
    public AuthMsg(int result,String key){
        this.authResult = result;
        this.key = key;
    }
    public int authResult;
    public String key;

}
