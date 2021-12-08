package com.fntj.lib.zb.model;

import java.io.Serializable;

public class APIResult<T> implements Serializable {

    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> APIResult<T> Success(T data) {

        APIResult<T> result = new APIResult<T>();
        result.setSuccess(true);
        result.setCode(null);
        result.setMessage(null);
        result.setData(data);

        return result;
    }

    public static APIResult Fail(String message, String code) {

        APIResult result = new APIResult();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);

        return result;
    }

    public static APIResult DataExist() {
        return Fail("数据已存在", "data-exist");
    }

    public static APIResult ParamError() {
        return Fail("参数错误", "param-error");
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
