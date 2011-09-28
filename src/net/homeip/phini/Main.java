package net.homeip.phini;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Główna klasa programu. Patrz opis metody "main" znajdującej się w poniższej
 * klasie.
 * 
 * @author Arkadiusz Wiesner
 * 
 */
public class Main {

	/**
	 * Głowna metoda programu serwera. Jej podstawowe działania: 1) Otwiera
	 * połączenie z bazą danych(a jeśli ona nie istnieje to ją tworzy). 2)
	 * Oczekuje na połączenie z klientem, a gdy to nastąpi uruchamia odpowiedni
	 * wątek, któremu przekazuje gniazdo połączenia i który kontynuuje
	 * przetwarzanie danych.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if (new File("/tmp/komunikator.db.lck").exists()) {
			System.out
					.println("Plik /tmp/komunikator.db.lck bazy danych już istnieje.");
			System.out
					.println("Serwer jest już uruchomiony albo inny proces korzysta z bazy danych");
			System.out
					.println("Proszę zamknąć wszystkie połączenia z bazą danych i ponownie uruchomić serwer");
			return;
		}

		ServerSocket socket = null;
		int port = 9977;
		boolean listening = true;
		ClientList cList = new ClientList();

		sqlConnection sc = new sqlConnection();
		sc.connect();

		System.out.println("Trying to open port...");

		try {
			socket = new ServerSocket(port);
		} catch (Exception e) {
			System.out.println("[SRV][EE] Could not bind to port " + port
					+ ". Exiting..");
			System.exit(-1);
		}

		System.out.println("[SRV][II] Successfuly bind to port " + port + ".");

		while (listening) {
			System.out.println("[SRV][II] Listening...");

			try {
				new ClientThreadIn(socket.accept(), cList, sc).start();
			} catch (IOException e) {
				e.printStackTrace();
			}

			synchronized (cList) {
				cList.print();
			}

		}
		System.out
				.println("[SRV][II] Stopped listeting. Closing ServerSocket...");
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		sc.disconnect();
		System.out.println("[SRV][II] Exiting...");
	}

}
