package net.homeip.phini;

import java.net.Socket;

public class Client {

	private Socket toClientSocket;
	private Socket fromClientSocket;
	private String login;

	public Client(String l, Socket in) {
		this.setFromClientSocket(in);
		this.login = l;
	}

	public Client(String recipient) {
		this.login = recipient;
	}

	/**
	 * @return the login
	 */

	public String getLogin() {
		return login;
	}

	/**
	 * @param login
	 *            the login to set
	 */

	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the toClientSocket
	 */
	public Socket getToClientSocket() {
		return toClientSocket;
	}

	/**
	 * @param toClientSocket
	 *            the toClientSocket to set
	 */
	public void setToClientSocket(Socket toClientSocket) {
		this.toClientSocket = toClientSocket;
	}

	/**
	 * @return the fromClientSocket
	 */
	public Socket getFromClientSocket() {
		return fromClientSocket;
	}

	/**
	 * @param fromClientSocket
	 *            the fromClientSocket to set
	 */
	public void setFromClientSocket(Socket fromClientSocket) {
		this.fromClientSocket = fromClientSocket;
	}
}
