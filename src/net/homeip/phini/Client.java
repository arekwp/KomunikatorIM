package net.homeip.phini;

import java.net.Socket;

/**
 * Klasa reprezentująca obiekt klienta i przechowywująca o nim podstawowe dane
 * takie jak dwa gniazda dla połączeń przychodzących/wychodzących oraz login
 * 
 * @author phini
 * 
 */
public class Client {

	private Socket toClientSocket;
	private Socket fromClientSocket;
	private String login;

	/**
	 * Konstruktor klasy Client
	 * 
	 * @param l
	 *            String - Login
	 * @param in
	 *            Socket - gniazdo obsługujące połączenia przychodzące do
	 *            serwera(od klienta)
	 */
	public Client(String l, Socket in) {
		this.setFromClientSocket(in);
		this.login = l;
	}

	/**
	 * Konstruktor klasy Client
	 * 
	 * @param l
	 *            String - nazwa klienta
	 */
	public Client(String l) {
		this.login = l;
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
