package com.yulongcloud.cloudflareddns.exception;

public class CloudflareException extends Exception {
    public CloudflareException() {
    }

    public CloudflareException(String message) {
        super(message);
    }

    public CloudflareException(String message, Throwable cause) {
        super(message, cause);
    }

    public CloudflareException(Throwable cause) {
        super(cause);
    }
}
