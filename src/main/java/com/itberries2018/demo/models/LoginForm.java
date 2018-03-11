package com.itberries2018.demo.models;

public class LoginForm {

	private final String login;
	private final String password;

	public LoginForm(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public String getLogin() {
		return login;
	}
}
