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

	public manageIncomincTraffic(String type, String doc, ClientList cList) {
		super("MSG thread");
		System.out.println("Managing MSG");
		this.type = type;
		this.cl = cList;
		this.doc = doc;
	}

	public manageIncomincTraffic(String type, Document doc, ClientList cList) {
		super("CONTACTS thread");
		System.out.println("Managing CONTACTS");
		this.type = type;
		this.cl = cList;
		this.xmlDoc = doc;
	}

	public void run() {
		if (type.equals("LOG")) {
			auth();
		} else if (type.equals("MSG")) {
			fwMsg();
		} else if (type.equals("CONTACTS")) {
			activeContacts();
		}
	}

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

		@SuppressWarnings("unchecked")
		List<Element> kontakty = inContent.getChildren("kontakt");
		System.out.println("ilosc odebranych kontaktów: " + kontakty.size());
		for (int i = 0; i < kontakty.size(); i++) {

			Element kontakt = (Element) kontakty.get(i);
			System.out.print(">> kontakt: " + kontakt.getValue() + " is ");

			if (cl.isActive(new Client(kontakt.getValue()))) {
				outMsg.addContent(new Element("kontakt").addContent(kontakt
						.getValue()));
				System.out.println("active.");
			} else {
				System.out.println("not active");
			}
		}

		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		StringWriter sw = new StringWriter();

		try {
			out.output(outDoc, sw);

			System.out.println("sw: " + sw.toString());

			System.out.println("cList: ");
			cl.print();
			int index = cl.getClientIndex(new Client(nadawca));
			if (index != -1) {

				Socket s = cl.getClient(index).getToClientSocket();
				pw = new PrintWriter(s.getOutputStream());
				System.out.println("OUTGOING CONTACTS");
				System.out.println(sw.toString());
				pw.write(sw.toString());
				pw.flush();
				// }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

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
			prepareNotDelivered(recipient, sender);

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

	private String prepareNotDelivered(String recipient, String sender) {
		Element message = new Element("message");
		Document xdoc = new Document(message);

		message.setAttribute(new Attribute("type", "MSG"));
		message.setAttribute(new Attribute("date", new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss").format(new Date())));
		message.setAttribute(new Attribute("nadawca", recipient));
		message.setAttribute(new Attribute("odbiorca", sender));
		message.addContent(new Element("content")
				.setText("Serwer: Odbiorca niezalogowany."));

		StringWriter sw = new StringWriter();
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

		try {
			out.output(xdoc, sw);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sw.toString();
	}

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
