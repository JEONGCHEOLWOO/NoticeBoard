package com.example.NoticeBoard.Exception;

import com.example.NoticeBoard.dto.ResponseDto;
import com.example.NoticeBoard.enumeration.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 유효성 검사 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(ResponseDto.fail(message));
    }

    // 비즈니스 예외 처리 (
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseDto<?>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.ok(ResponseDto.fail(ex.getErrorCode().getMessage()));
    }

    // 그 외 모든 예외 처리 (서버 오류)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handleException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
