package net.homeip.phini;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Klasa zajmująca się przetwarzaniem odebranych wiadomości w zależności od ich
 * typu. Obsługiwane typu wiadomości: 1) MSG 2) CONTACTS 3) LOG 4) END
 * 
 * @author Arek Wiesner
 * 
 */
public class manageIncomincTraffic extends Thread {

	private Socket inSocket;
	private String login;
	private String pass;
	private String type;
	private sqlConnection sc;
	private PrintWriter pw;
	private ClientList cl;
	private String doc;
	private Document xmlDoc;

	/**
	 * Konstruktor klasy zajmujący się ustalaniem wartości pól dla wiadomości
	 * typu LOG
	 * 
	 * @param type
	 *            Typ przychodzącej wiadomości
	 * @param pass
	 *            Hasło logowania
	 * @param c
	 *            Gniazdo połączenia
	 * @param sqlConn
	 *            Połaczenie z bazą danych
	 * @param cList
	 *            Lista zalogowanych(aktywnych) użytkowników
	 */
	public manageIncomincTraffic(String type, String pass, Client c,
			sqlConnection sqlConn, ClientList cList) {
		super("LOG thread");

		System.out.println("Managing LOG");
		this.login = c.getLogin();
		this.pass = pass;
		this.inSocket = c.getFromClientSocket();
		this.type = type;
		this.sc = sqlConn;
		this.cl = cList;

		try {
			pw = new PrintWriter(inSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Konstruktor klasy zajmujący się ustalaniem wartości pól dla wiadomości
	 * MSG
	 * 
	 * @param type
	 *            Typ wiadomości
	 * @param doc
	 *            Ciąg znakowy(string) zawierający treść przesyłanej wiadomości
	 *            w formie XML
	 * @param cList
	 *            Lista zalogowanych(aktywnych) użytkowników
	 */
	public manageIncomincTraffic(String type, String doc, ClientList cList) {
		super("MSG thread");
		System.out.println("Managing MSG");
		this.type = type;
		this.cl = cList;
		this.doc = doc;
	}

	/**
	 * Konstruktor klasy zajmujący się ustalaniem wartości pól dla wiadomości
	 * CONTACTS
	 * 
	 * @param type
	 *            Typ wiadomości
	 * @param doc
	 *            Obiekt typu Document zawierający przesyłaną wiadomość jako XML
	 * @param cList
	 *            Lista zalogowanych(aktywnych) użytkowników
	 */
	public manageIncomincTraffic(String type, Document doc, ClientList cList) {
		super("CONTACTS thread");
		System.out.println("Managing CONTACTS");
		this.type = type;
		this.cl = cList;
		this.xmlDoc = doc;
	}

	/**
	 * Konstruktor klasy zajmujący się ustalaniem wartości pól dla wiadomości
	 * END
	 * 
	 * @param type
	 *            Typ wiadomości
	 * @param doc
	 *            Obiekt typu Document zawierający przesyłaną wiadomość jako XML
	 * @param cList
	 *            Lista zalogowanych(aktywnych) użytkowników
	 * @param l
	 *            Dodatkowy, nie używany obecnie parametr
	 */
	public manageIncomincTraffic(String type, Document doc, ClientList cList,
			int l) {
		super("END thread");
		System.out.println("Managing END message");
		this.type = type;
		this.cl = cList;
		this.xmlDoc = doc;
	}

	public manageIncomincTraffic(String type, Document xDoc, sqlConnection sc,
			Socket s) {
		super("REG thread");
		System.out.println("Managing REG message");
		this.xmlDoc = xDoc;
		this.type = type;
		this.sc = sc;
		this.inSocket = s;
	}

	/**
	 * Metoda 'run' to główna metoda wykonywana w wątku. Realizuje ona
	 * odpowiednie działania w zależności od typu wiadomości.
	 */
	public void run() {
		if (type.equals("LOG")) {
			auth();
		} else if (type.equals("MSG")) {
			fwMsg();
		} else if (type.equals("CONTACTS")) {
			activeContacts();
		} else if (type.equals("END")) {
			endConversation();
		} else if (type.equals("REG")) {
			register();
		}
	}

	private void register() {
		System.out.println("rejestrowanie uzytkownika");
		Element root = xmlDoc.getRootElement();

		login = root.getChild("content").getChild("login").getValue();
		pass = root.getChild("content").getChild("pass").getValue();
		try {
			PrintWriter pw = new PrintWriter(inSocket.getOutputStream());

			if (sc.userExists(login)) {
				String tmp = xmlMsgs.createRegMsg("ERRUE");
				pw.write(tmp);
				pw.flush();
			} else {
				sc.add(login, pass);
				String tmp = xmlMsgs.createRegMsg("OK");
				pw.write(tmp);
				pw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metoda zajmująca się zakańczaniem połączenia z wylogowywującym się
	 * klientem.
	 */
	private void endConversation() {
		Element root = xmlDoc.getRootElement();
		String nadawca = root.getAttributeValue("nadawca");
		System.out.println("usuwam " + nadawca + " z listy zalogowanych.");

		cl.remove(new Client(nadawca));

	}

	/**
	 * Metoda na podstawie przyjętej listy kontaktów odsyła liste tylko tych,
	 * którzy są obecnie zalogowani
	 */
	private void activeContacts() {
		/*
		 * SAXBuilder sax = new SAXBuilder(); Reader r = new StringReader(doc);
		 * 
		 * try { xmlDoc = sax.build(r); } catch (Exception e1) {
		 * e1.printStackTrace(); }
		 */

		Element inRoot = xmlDoc.getRootElement();
		Element inContent = inRoot.getChild("content");

		Element outMsg = new Element("message");
		Document outDoc = new Document(outMsg);
		String nadawca = inRoot.getAttributeValue("nadawca");

		outMsg.setAttribute(new Attribute("date", new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss").format(new Date())));
		outMsg.setAttribute(new Attribute("type", "CONTACTS"));

		Element content = new Element("content");

		@SuppressWarnings("unchecked")
		List<Element> kontakty = inContent.getChildren("kontakt");
		System.out.println("ilosc odebranych kontaktów: " + kontakty.size());
		int qtyOfActive = 0;
		for (int i = 0; i < kontakty.size(); i++) {

			Element kontakt = (Element) kontakty.get(i);
			System.out.print(">> kontakt: " + kontakt.getValue() + " is ");

			if (cl.isActive(new Client(kontakt.getValue()))) {
				qtyOfActive++;
				content.addContent(new Element("kontakt").addContent(kontakt
						.getValue()));
				System.out.println("active.");
			} else {
				System.out.println("not active");
			}
		}

		outMsg.setContent(content);

		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		StringWriter sw = new StringWriter();

		try {
			out.output(outDoc, sw);

			System.out.println("sw: " + sw.toString());

			System.out.println("cList: ");
			cl.print();

			System.out.println("odsyłam kontakty dla: " + nadawca);

			int index = cl.getClientIndex(new Client(nadawca));
			System.out.println("index nadawcy kontaktów: " + index);
			if (index != -1 && qtyOfActive != -1) {

				Socket s = cl.getClient(index).getToClientSocket();
				pw = new PrintWriter(s.getOutputStream());
				System.out.println("OUTGOING CONTACTS");
				System.out.println(sw.toString());
				pw.write(sw.toString());
				pw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Metoda przekazująca wiadomość do odpowiedniego użytkownika.
	 */
	private void fwMsg() {

		SAXBuilder sax = new SAXBuilder();
		Reader r = new StringReader(doc);

		try {
			xmlDoc = sax.build(r);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Element root = xmlDoc.getRootElement();

		String recipient = root.getAttributeValue("odbiorca");
		String sender = root.getAttributeValue("nadawca");

		int index = cl.getClientIndex(new Client(recipient));
		System.out.println(">MSG -> index = " + index);
		if (index != -1) {
			try {
				pw = new PrintWriter(cl.getClient(index).getToClientSocket()
						.getOutputStream());
				pw.write(doc);
				pw.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Użytkownik niezalogowany");
			doc = xmlMsgs.prepareNotDelivered(recipient, sender);

			index = cl.getClientIndex(new Client(sender));
			try {
				pw = new PrintWriter(cl.getClient(index).getToClientSocket()
						.getOutputStream());
				pw.write(doc);
				pw.flush();
			} catch (IOException e) {
			}

		}
	}

	/**
	 * Metoda realizująca logowanie(autoryzowanie) klienta.
	 */
	private void auth() {
		// sc.connect();
		String xmlMsg = null;
		if (sc.auth(login, pass) == true) {
			System.out.println("LOGGED");
			xmlMsg = xmlMsgs.createAuthMsg("LOGGED");

			System.out.println("1 > Adding 'to' socket...");
			System.out.println("1 > Socket info: \n port: "
					+ inSocket.getPort() + "\n address: "
					+ inSocket.getInetAddress());

			cl.add(new Client(login, inSocket)); // sychronized
		} else {
			xmlMsg = xmlMsgs.createAuthMsg("ERROR");
		}
		System.out.println("LOG ANSWER XML: \n" + xmlMsg);

		pw.write(xmlMsg + "\n");

		pw.flush();
		// os.close();

		// sc.disconnect();
	}
}
