package com.gccbenben.qqbotservice.bean;

import java.io.Serializable;

/**
 * 通用http返回对象
 *
 * @author gccbenben
 * @date 2021/07/17
 */
public class HttpCommonResponse<T>  implements Serializable {
    /**
     * 状态码
     */
    private long code;
    /**
     * 提示信息
     */
    private String message;
    /**
     * 数据封装
     */
    private T data;

    protected HttpCommonResponse() {
    }

    protected HttpCommonResponse(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> HttpCommonResponse<T> success(T data) {
        return new HttpCommonResponse<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     * @param  message 提示信息
     */
    public static <T> HttpCommonResponse<T> success(T data, String message) {
        return new HttpCommonResponse<T>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     * @param errorCode 错误码
     */
    public static <T> HttpCommonResponse<T> failed(IErrorCode errorCode) {
        return new HttpCommonResponse<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回结果
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static <T> HttpCommonResponse<T> failed(IErrorCode errorCode,String message) {
        return new HttpCommonResponse<T>(errorCode.getCode(), message, null);
    }

    /**
     * 失败返回结果
     * @param message 提示信息
     */
    public static <T> HttpCommonResponse<T> failed(String message) {
        return new HttpCommonResponse<T>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> HttpCommonResponse<T> failed() {
        return failed(ResultCode.FAILED);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> HttpCommonResponse<T> validateFailed() {
        return failed(ResultCode.VALIDATE_FAILED);
    }

    /**
     * 参数验证失败返回结果
     * @param message 提示信息
     */
    public static <T> HttpCommonResponse<T> validateFailed(String message) {
        return new HttpCommonResponse<T>(ResultCode.VALIDATE_FAILED.getCode(), message, null);
    }

    /**
     * 未登录返回结果
     */
    public static <T> HttpCommonResponse<T> unauthorized(T data) {
        return new HttpCommonResponse<T>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 未授权返回结果
     */
    public static <T> HttpCommonResponse<T> forbidden(T data) {
        return new HttpCommonResponse<T>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), data);
    }

    /**
     * 登录超时,请重新登录
     */
    public static <T> HttpCommonResponse<T> sessionInvalid(T data) {
        return new HttpCommonResponse<T>(ResultCode.SESSION_INVALID.getCode(), ResultCode.SESSION_INVALID.getMessage(), data);
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
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

    @Override
    public String toString(){
        return "CommonResult{code='" + this.code + '\'' + ", message='" + this.message + '\'' + ", data=" + this.data + '}';
    }
}
