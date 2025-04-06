package com.jwt.implementation.response;

public class LoginResponse {

	private String token;
	private long tokenExpireTime;
	
	public LoginResponse(String token, long tokenExpireTime) {
		super();
		this.token = token;
		this.tokenExpireTime = tokenExpireTime;
	}
	
	public LoginResponse() {
		
	}  
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public long getTokenExpireTime() {
		return tokenExpireTime;
	}
	public void setTokenExpireTime(long tokenExpireTime) {
		this.tokenExpireTime = tokenExpireTime;
	}
	
}
