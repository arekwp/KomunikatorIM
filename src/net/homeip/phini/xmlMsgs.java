package net.homeip.phini;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class xmlMsgs {

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

		//String out = null;

		try {
			xmlOut.output(doc, sw);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sw.toString();
	}

}
