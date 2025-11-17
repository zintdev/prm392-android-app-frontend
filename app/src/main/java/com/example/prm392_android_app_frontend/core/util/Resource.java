package com.example.prm392_android_app_frontend.core.util;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import java.util.Objects;
import java.util.function.Function;

public final class Resource<T> {

    public enum Status { LOADING, SUCCESS, ERROR }

    @NonNull private final Status status;
    @Nullable private final T data;
    @Nullable private final String message;
    @Nullable private final Throwable cause;   // lỗi gốc (nếu có)
    @Nullable private final Integer httpCode;  // mã HTTP (nếu có)

    private Resource(@NonNull Status status,
                     @Nullable T data,
                     @Nullable String message,
                     @Nullable Throwable cause,
                     @Nullable Integer httpCode) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.cause = cause;
        this.httpCode = httpCode;
    }

    // ---------- Factory ----------
    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null, null, null);
    }
    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null, null, null);
    }
    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null, null, null);
    }
    public static <T> Resource<T> error(@NonNull String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg, null, null);
    }
    public static <T> Resource<T> error(@NonNull String msg) {
        return new Resource<>(Status.ERROR, null, msg, null, null);
    }
    public static <T> Resource<T> error(@NonNull String msg, @Nullable T data, @Nullable Integer httpCode) {
        return new Resource<>(Status.ERROR, data, msg, null, httpCode);
    }
    public static <T> Resource<T> error(@NonNull String msg, @Nullable Throwable cause) {
        return new Resource<>(Status.ERROR, null, msg, cause, null);
    }

    // ---------- Getters ----------
    @NonNull public Status getStatus() { return status; }
    @Nullable public T getData() { return data; }
    @Nullable public String getMessage() { return message; }
    @Nullable public Throwable getCause() { return cause; }
    @Nullable public Integer getHttpCode() { return httpCode; }

    // ---------- Convenience ----------
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError()   { return status == Status.ERROR; }

    /** Map data -> data' (giữ nguyên status/message/httpCode/cause) */
    public <R> Resource<R> map(Function<? super T, ? extends R> mapper) {
        R next = (data == null) ? null : mapper.apply(data);
        return new Resource<>(status, next, message, cause, httpCode);
    }

    @Override public String toString() {
        return "Resource{" +
                "status=" + status +
                ", data=" + (data != null ? data.getClass().getSimpleName() : "null") +
                ", message=" + message +
                ", httpCode=" + httpCode +
                '}';
    }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;
        Resource<?> that = (Resource<?>) o;
        return status == that.status &&
                Objects.equals(data, that.data) &&
                Objects.equals(message, that.message) &&
                Objects.equals(httpCode, that.httpCode);
    }
    @Override public int hashCode() {
        return Objects.hash(status, data, message, httpCode);
    }
}
