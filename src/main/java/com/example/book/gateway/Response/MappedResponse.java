package com.example.book.gateway.Response;

import java.util.HashMap;
import java.util.Map;

public class MappedResponse {
    public static Map<String,Object> responseMap(int status, String message, Object data){
        Map<String,Object> responseBody = new HashMap<>();
        responseBody.put("status",status);
        responseBody.put("message",message);
        if(message.equalsIgnoreCase("FAILED")){
            responseBody.put("error",data);
        }else{
            responseBody.put("data", data);
        }
        return responseBody;
    }
}
