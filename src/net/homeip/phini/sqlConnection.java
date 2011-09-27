package net.homeip.phini;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class sqlConnection {

	private Connection conn;
	private Statement st;

	public sqlConnection() {

	}

	public void connect() {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			File f = new File("/tmp/komunikator.db.properties");
			if (!f.exists()) {
				createDb();
			} else {
				conn = DriverManager.getConnection(
						"jdbc:hsqldb:file:/tmp/komunikator.db", "SA", "");
				st = conn.createStatement();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void createDb() {
		try {
			conn = DriverManager.getConnection(
					"jdbc:hsqldb:file:/tmp/komunikator.db", "SA", "");
			st = conn.createStatement();

			String sql = "CREATE TABLE logpas (login varchar(30), pass varchar(30));";
			st.execute(sql);

			// wstawiam "fabryczne" dane do bazy:

			sql = "INSERT INTO logpas VALUES('root', 'rooter');";
			st.execute(sql);

			sql = "INSERT INTO logpas VALUES('arek', 'arek');";
			st.execute(sql);

			sql = "INSERT INTO logpas VALUES('marta', 'marta');";
			st.execute(sql);

			sql = "INSERT INTO logpas VALUES('test', 'test');";
			st.execute(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("[II] Table were created successfuly.");
		System.out.println("[II] Data was inserted successfuly.");
	}

	public boolean auth(String l, String p) {

		String recLogin = "";

		try {
			String sql = "SELECT login FROM logpas WHERE login='" + l
					+ "' AND pass='" + p + "';";

			// Statement st = conn.createStatement();
			if (st == null)
				System.out.println("st is null!");
			ResultSet rs = st.executeQuery(sql);

			rs.next();
			int qty = rs.getRow();
			if (qty == 0)
				return false;

			recLogin = rs.getString("LOGIN");

			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("{AUTH}");

		if (l.equals(recLogin))
			return true;
		else
			return false;
	}

	public boolean userExists(String login) {
		String sql = "SELECT login FROM logpas WHERE login='" + login + "';";

		ResultSet rs;
		try {
			rs = st.executeQuery(sql);

			rs.next();
			int qty = rs.getRow();
			if (qty == 0)
				return false;

			String recLogin = rs.getString("LOGIN");

			if (recLogin.equals(login))
				return true;
			else
				return false;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void disconnect() {
		try {
			st.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
