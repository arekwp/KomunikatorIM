package net.homeip.phini;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Klasa realizująca odbieranie danych od klienta
 * 
 * @author Arkadiusz Wiesner
 * 
 */
public class ClientThreadIn extends Thread {

	private Socket inSocket;
	private ClientList cList;
	private sqlConnection sc;

	private Client client;

	/**
	 * Wątek odbierający dane, startujący kiedy klient nawiąże połączenie
	 * 
	 * @param soc
	 *            Gniazdo na którym klient nawiązał połączenie
	 * @param clientList
	 *            Lista zalogowanych(aktywnych) klientów
	 * @param sc
	 *            Połączenie z bazą danych
	 */
	public ClientThreadIn(Socket soc, ClientList clientList, sqlConnection sc) {

		super("ClientThread");
		this.inSocket = soc;
		cList = clientList;

		try {
			this.inSocket.setKeepAlive(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.sc = sc;
	}

	/**
	 * 
	 * Metoda 'run' to główna metoda wykonywana w wątku Służy ona do odbierania
	 * danych od klientów i odpowiedniego zarządzania odebranymi danymi tzn,
	 * odbiera dane i przekazuje je do wątku, który dalej je przetwarza.
	 * 
	 */
	public void run() {
		try {
			while (true) {

				while (true) {
					BufferedReader input;

					input = new BufferedReader(new InputStreamReader(
							inSocket.getInputStream()));
					boolean done = false;

					String wholeLine = "";

					while (!done) {
						String str = input.readLine();
						// if (str.equals("") || str == null)
						if (str.equals("</message>"))
							break;
						if (!str.equals("") && !str.equals("\n"))
							wholeLine += str + "\n";
					}
					wholeLine += "</message>\n";
					// wholeLine = wholeLine.substring(0, wholeLine.length() -
					// 1);
					System.out.println("wholeline: " + wholeLine);

					SAXBuilder builder = new SAXBuilder();
					Reader r = new StringReader(wholeLine);

					Document doc = builder.build(r);

					Element root = doc.getRootElement();

					String type = root.getAttributeValue("type");

					System.out.println("root elem: " + type);

					if (type.equals("LOG")) {

						Element content = root.getChild("content");
						String login = content.getChildText("login");
						String pass = content.getChildText("pass");

						System.out.println("login = " + login + ", pass = "
								+ pass);

						new manageIncomincTraffic(type, pass, new Client(login,
								inSocket), sc, cList).start();

					} else if (type.equals("LOG2")) {

						Element content = root.getChild("content");
						String login = content.getChildText("login");
						String pass = content.getChildText("pass");

						System.out.println("login = " + login + ", pass = "
								+ pass);

						// new manageIncomincTraffic(type, pass, new
						// Client(login,
						// inSocket), sc, cList).start();

						// PrintWriter pw = new PrintWriter(
						// inSocket.getOutputStream());

						// String xmlMsg = null;
						if (sc.auth(login, pass)) {
							System.out.println("LOGGED2");
							// xmlMsg = xmlMsgs.createAuthMsg("LOGGED2");
							// cl.add(new Client(login, inSocket)); //
							// sychronized

							int index = cList.getClientIndex(new Client(login));

							System.out.println("2> Adding 'to' socket...");
							System.out.println("2 > Socket info: \n port: "
									+ inSocket.getPort() + "\n address: "
									+ inSocket.getInetAddress());
							cList.getClient(index).setToClientSocket(inSocket);
						} else {
							// xmlMsg = xmlMsgs.createAuthMsg("ERROR2");
						}

						// pw.write(xmlMsg + "\n");

						// pw.flush();

					} else if (type.equals("MSG")) {
						// String receiver = root.getAttributeValue("odbiorca");

						new manageIncomincTraffic(type, wholeLine, cList)
								.start();
					} else if (type.equals("CONTACTS")) {
						new manageIncomincTraffic(type, doc, cList).start();
					} else if (type.equals("END")) {
						new manageIncomincTraffic(type, doc, cList, 1).start();
					}
				}

			} // while -> incoming msg
		} catch (Exception e) { // jesli wystąpni wyjątek, np utracone
								// polaczenie z klientem, zamkniete gniazdo to
			if (client != null) { // usuwam klienta z listy zalogowanych i
									// zamykam gniazdo po stronie serwera(o ile
									// jest otwarte)
				int index = cList.getClientIndex(client);

				if (index > 0) { // jeśli nadal widnieje jako zalogowany..
					System.out.println("Błąd: klient " + client.getLogin()
							+ " zostaje usuniety z listy");
					cList.remove(cList.getClient(index));
					// klient zostaje usuniety z listy dostepnych
				}
			}
			try {
				inSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
	}
}
