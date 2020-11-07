package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Client;

public class ClientManager {

	private static Connection conn = DatabaseManager.getInstance().getConnection();

	public enum Fields {
		ID("ClientID"),
		NAME("ClientName"),
		PHONE_NUMER("PhoneNumber"),
		TYPE("Type");

		private String value;

		Fields(String value){ this.value = value; }

		@Override
		public String toString() {
			return value;
		}
	}


	public static void setConnection() {
		if(conn == null)
			conn = DatabaseManager.getInstance().getConnection();
	}

	public static ArrayList<Client> searchClient(String query, ClientManager.Fields searchBy) throws SQLException {

		ArrayList<Client> list = new ArrayList<>();
		String sql = "";
		if(searchBy == Fields.ID) {
			sql = "SELECT * FROM Client WHERE Locked = TRUE OR ClientID LIKE '%" + query + "%'";
		} else if(searchBy == Fields.TYPE) {
			query = query.replace(",", "\',\'");
			sql = "SELECT * FROM Client WHERE Locked = TRUE OR " + searchBy.toString() + " IN ('" + query + "')";
		} else {
			sql = "SELECT * FROM Client WHERE Locked = TRUE OR REGEXP_MATCHES(LOWER("
					+ searchBy.toString() + "),'.*" + query + ".*')";
		}
		PreparedStatement pStmt = conn.prepareStatement(sql);
		ResultSet result = pStmt.executeQuery();

		while(result.next()){
			list.add(new Client(result.getString(3), result.getString(1), result.getString(4),
					result.getBoolean(5)));
		}
		if(result != null) result.close();
		if(pStmt != null) pStmt.close();
		return list;
	}

	public static ObservableList<Client> getClientList() throws SQLException {
		ObservableList<Client> list = FXCollections.observableArrayList();
		if(getClientCount() != -1) {
			Statement stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery("SELECT * FROM Client");
			while(result.next()){
				list.add(new Client(result.getString(3), result.getString(1), result.getString(4),
						result.getBoolean(5)));
			}
			if(result != null) result.close();
			if(stmt != null) stmt.close();
		}
		return list;
	}

	public static int getClientCount() throws SQLException {

		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery("SELECT COUNT(*) FROM Client");
		int val = -1;

		while(result.next()){
			val = result.getInt(1);
		}
		if(result != null) result.close();
		if(stmt != null) stmt.close();

		return val;
	}

	public static void addClient(Client c) throws SQLException {

		PreparedStatement pStmt = null;

		pStmt = conn
				.prepareStatement("INSERT INTO Client VALUES (?, ?, ?, ?, ?);");
		pStmt.setString(1, c.getPhoneNumber());
		pStmt.setString(2, c.getId());
		pStmt.setString(3, c.getName());
		pStmt.setString(4, c.getType());
		pStmt.setBoolean(5, c.isLocked());

		pStmt.execute();
		if(pStmt != null)
			pStmt.close();


	}

	public static void updateClient(Client c) throws SQLException {
		PreparedStatement pStmt = conn
				.prepareStatement("UPDATE Client SET PhoneNumber = ?, ClientName = ?, Type = ?, "
				+ "Locked = ? WHERE ClientID = ?");

		pStmt.setString(1, c.getPhoneNumber());
		pStmt.setString(2, c.getName());
		pStmt.setString(3, c.getType());
		pStmt.setBoolean(4, c.isLocked());
		pStmt.setString(5,  c.getId());

		pStmt.execute();

		if(pStmt != null)
			pStmt.close();


	}

	public static void updateLock(String id, boolean locked) throws SQLException {
		PreparedStatement pStmt = null;
		pStmt = conn
				.prepareStatement("UPDATE Client SET Locked = ? WHERE ClientID = ?");

		pStmt.setBoolean(1, locked);
		pStmt.setString(2,  id);

		pStmt.execute();

		if(pStmt != null)
			pStmt.close();


	}

	public static void deleteClient(String id) throws SQLException{
		PreparedStatement pStmt = null;
		pStmt = conn
				.prepareStatement("DELETE FROM Client WHERE ClientID = ?");
		pStmt.setString(1, id);
		pStmt.execute();

		if(pStmt != null)
			pStmt.close();


	}

	public static void closeConnection() throws SQLException{
		if(conn != null) {
			conn.close();
			conn = null;
		}
	}
}
