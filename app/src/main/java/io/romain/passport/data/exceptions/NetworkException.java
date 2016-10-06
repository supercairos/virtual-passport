package io.romain.passport.data.exceptions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

public class NetworkException extends IOException {

	public int status;

	@Expose
	@SerializedName("message")
	public String message;

	@Expose
	@SerializedName("code")
	public int code;

	public NetworkException(int code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String getMessage() {
		return "Server returned code " + code + " and message " + message + " HTTP status was " + status;
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}
}


