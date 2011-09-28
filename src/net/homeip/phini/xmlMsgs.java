package net.homeip.phini;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Klasa realizująca tworzenie wiadomości typu LOG jako odpowiedzi serwera.
 * 
 * @author Arkadiusz Wiesner
 * 
 */
public class xmlMsgs {

	/**
	 * Statyczna metoda, której zadaniem jest tworzenie wiadomości typu LOG z
	 * zadanym tekstem
	 * 
	 * @param msg
	 *            treść wiadomości: 1) LOGGED gdy zalogowano 2) ERROR Gdy
	 *            wystąpił bład logowania
	 * @return Zwraca ciąg znakowy reprezentujący wiadomość w formacie XML
	 */
	public static String createAuthMsg(String msg) {

		StringWriter sw = new StringWriter();

		Element message = new Element("message");
		Document doc = new Document(message);
		message.setAttribute(new Attribute("type", "LOG"));

		// time format: 2011-09-22 19:18:02
		message.setAttribute(new Attribute("date", new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss").format(new Date())));
		message.addContent(new Element("content").setText(msg));
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());

		// String out = null;

		try {
			xmlOut.output(doc, sw);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sw.toString();
	}

	public static String createRegMsg(String tresc) {
		StringWriter sw = new StringWriter();
		Element message = new Element("message");
		Document xDoc = new Document(message);

		message.setAttribute(new Attribute("type", "REG"));
		message.addContent(new Element("content").setText(tresc));

		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

		try {
			out.output(xDoc, sw);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sw.toString();
	}

	/**
	 * Metoda Tworząca odpowiedź na wiadomość typu MSG, gdy adresat jest
	 * niedostępny(niezalogowany)
	 * 
	 * @param recipient
	 *            Odbiorca
	 * @param sender
	 *            Nadawca
	 * @return Zwraca utworzoną wiadomość reprezentowaną jako ciąg znakowy.
	 */
	public static String prepareNotDelivered(String recipient, String sender) {
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
}
