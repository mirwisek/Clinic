package database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DatabaseManager {

	private static DatabaseManager instance = null;

	private static Connection conn = null;

	private DatabaseManager(){

		conn = ConnectionManager.getInstance().getConnection();

		try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_UPDATABLE)) {

//			System.out.println(getClass().getResource("/resources/sql/createClient.sql").getFile());
//			stmt.executeQuery(readFileToString(getClass().getResource("/resources/sql/createClient.sql").getFile()));
//			stmt.executeQuery(readFileToString(getClass().getResource("/resources/sql/createInvoice.sql").getFile()));
//			stmt.executeQuery(readFileToString(getClass().getResource("/resources/sql/createProduct.sql").getFile()));
//			stmt.executeQuery(readFileToString(getClass().getResource("/resources/sql/createLine.sql").getFile()));
			stmt.executeQuery(readFileToString("sql/createClient.sql"));
			stmt.executeQuery(readFileToString("sql/createInvoice.sql"));
			stmt.executeQuery(readFileToString("sql/createProduct.sql"));
			stmt.executeQuery(readFileToString("sql/createLine.sql"));

		} catch (Exception e) {
			System.err.println("DatabaseManger SQLException: " + e.getMessage());
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Database initialization failure!");
			alert.setHeaderText("Error: " + e.getMessage());
			alert.setContentText("Cause: " + e.getCause());
			alert.showAndWait();
		}
	}

	public static DatabaseManager getInstance(){
		if(instance == null){
			instance = new DatabaseManager();
		}
		return instance;
	}

	public Connection getConnection(){
		if (conn == null) {
			conn = ConnectionManager.getInstance().getConnection();
		}
		return conn;
	}

	private static String readFileToString(String url){
		String s = "";
		try {
			s = FileUtils.readFileToString(new File(url), "UTF-8");
		} catch (IOException e) {
			System.err.println("readFileToString Error: " + e.getMessage());
		}
		return s;
	}

	public void close() {
		try {
			conn.close();
			conn = null;
			ConnectionManager.getInstance().close();
			System.out.println("Connections Closed!");
		} catch (Exception e) {
			System.err.println("Closing Connection Exception");
		}
	}
}
