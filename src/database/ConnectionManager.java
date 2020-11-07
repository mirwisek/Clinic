package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

	private static ConnectionManager instance = null;

	private static Connection conn = null;

	private final static String USERNAME = "mir";
	private final static String PASSWORD = "pass";
	private final static String URL = "jdbc:hsqldb:file:database/clinic";

	private ConnectionManager() { }

	public static ConnectionManager getInstance(){
		if(instance == null){
			instance = new ConnectionManager();
		}
		return instance;
	}

	private boolean openConnection() {
		try {
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			return true;
		} catch (SQLException e) {
			System.err.println("OpenConnection ERROR: " + e.getMessage());
			return false;
		}
	}

	public Connection getConnection()
	{
		if (conn == null) {
			if (openConnection()) {
				System.out.println("Connection opened");
				return conn;
			} else {
				return null;
			}
		}
		return conn;
	}

	public void close() {
		System.out.println("Closing connection");
		try {
			conn.close();
			conn = null;
		} catch (Exception e) {
			System.err.println("Closing Connection Exception");
		}
	}

}
