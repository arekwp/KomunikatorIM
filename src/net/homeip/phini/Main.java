package net.homeip.phini;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class Main {

	/**
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
