package com.example.jumping.coolweather.util;

/**
 * Created by Jumping on 2016/9/19.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
