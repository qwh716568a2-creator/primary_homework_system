package com.primaryhomework.backend.config;

import com.primaryhomework.backend.entity.vo.R;
import com.primaryhomework.backend.utils.CommonException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<R<Void>> handleCommonException(CommonException e) {
        return ResponseEntity.status(resolveStatus(e.getCode()))
                .body(R.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        if (fieldError == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(R.fail(HttpStatus.BAD_REQUEST.value(), "\u53c2\u6570\u6821\u9a8c\u5931\u8d25"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(R.fail(HttpStatus.BAD_REQUEST.value(), fieldError.getDefaultMessage()));
    }

    @ExceptionHandler({BindException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<R<Void>> handleBadRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(R.fail(HttpStatus.BAD_REQUEST.value(), resolveBadRequestMessage(e)));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<R<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(R.fail(HttpStatus.NOT_FOUND.value(), "\u63a5\u53e3\u4e0d\u5b58\u5728"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "\u670d\u52a1\u5668\u5f00\u5c0f\u5dee\u4e86\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5"));
    }

    private HttpStatus resolveStatus(Integer code) {
        if (code == null) {
            return HttpStatus.BAD_REQUEST;
        }
        if (code >= 50000) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (code >= 40400 && code < 40500) {
            return HttpStatus.NOT_FOUND;
        }
        if (code >= 40300 && code < 40400) {
            return HttpStatus.FORBIDDEN;
        }
        if (code >= 40100 && code < 40200) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private String resolveBadRequestMessage(Exception e) {
        if (e instanceof BindException bindException) {
            FieldError fieldError = bindException.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
            return fieldError == null ? "\u53c2\u6570\u6821\u9a8c\u5931\u8d25" : fieldError.getDefaultMessage();
        }
        if (e instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintViolations().stream()
                    .findFirst()
                    .map(item -> item.getMessage())
                    .orElse("\u53c2\u6570\u6821\u9a8c\u5931\u8d25");
        }
        return e.getMessage() == null || e.getMessage().isBlank() ? "\u53c2\u6570\u6821\u9a8c\u5931\u8d25" : e.getMessage();
    }
}