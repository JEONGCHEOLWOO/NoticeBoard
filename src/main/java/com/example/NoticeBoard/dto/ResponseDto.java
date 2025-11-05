package com.example.NoticeBoard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto<T> {

    private boolean success;
    private T data;
    private String message;

    // 성공 응답
    public static <T> ResponseDto<T> success(T data, String massage){
        return  new ResponseDto<>(true, data, massage);
    }

    // 실패 응답
    public static <T> ResponseDto<T> fail(String massage){
        return  new ResponseDto<>(false, null, massage);
    }

}
