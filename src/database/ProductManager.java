package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Product;
import model.ProductState;

public class ProductManager {

	private static Connection conn = DatabaseManager.getInstance().getConnection();

	public enum Fields {
		ID("ProductID"),
		NAME("ProductName"),
		COMPOSITION("Composition"),
		TYPE("Type"),
		LEGAL("Legal"),
		PACKING("Packing"),
		SUPPLIER("Supplier"),
		DESCRIPTION("Description"),
		COMPANY("Company"),
		EXPIRY("ExpiryDate"),
		ENTRY("Entry"),
		QTY_CARTONS("QtyCarton"),
		QTY_BOX("QtyBox"),
		QTY_BP("QtyBP"),
		QTY_PILLS("QtyPill"),
		BOX_P_CARTON("BoxPCarton"),
		BP_P_BOX("BPPBox"),
		PILL_P_BP("PillPBP"),
		PRICE_CARTON("PriceCarton"),
		PRICE_BOX("PriceBox"),
		PRICE_BP("PriceBP"),
		PRICE_PILL("PricePill"),
		PRICE_CARTON_CR("PriceCartonCR"),
		PRICE_BOX_CR("PriceBoxCR"),
		PRICE_BP_CR("PriceBPCR"),
		PRICE_PILL_CR("PricePillCR"),
		STATE("State");

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

	public static void removeCriticalState(Product p, int range) throws SQLException {
		Statement stmt = conn.createStatement();
		ProductState state = p.getProductState();
		if(state == ProductState.LOCKED_CRITICAL)
			state = ProductState.LOCKED;
		else if(state == ProductState.CRITICAL)
			state = ProductState.UNLOCKED;
		stmt.executeQuery("Update Product SET State = \'" + state + "\' WHERE ProductID = \'" + p.getProductID() + "\' AND " + Fields.QTY_CARTONS
				+ " > " + range);

//		ResultSet result = stmt.executeQuery("SELECT * FROM Product WHERE ProductID = " + p.getProductID());
//
//		Product temp = new Product(result.getString(Fields.ID.toString()), result.getString(Fields.NAME.toString()),
//				result.getString(Fields.TYPE.toString()), result.getString(Fields.COMPOSITION.toString()),
//				result.getString(Fields.DESCRIPTION.toString()), result.getString(Fields.SUPPLIER.toString()),
//				result.getString(Fields.COMPANY.toString()), result.getBoolean(Fields.LEGAL.toString()),
//				result.getString(Fields.PACKING.toString()), result.getDate(Fields.EXPIRY.toString()),
//				 new Date(result.getTimestamp(Fields.ENTRY.toString()).getTime()),result.getInt(Fields.QTY_CARTONS.toString()),
//				 result.getInt(Fields.QTY_BOX.toString()), result.getInt(Fields.QTY_BP.toString()),
//				 result.getInt(Fields.QTY_PILLS.toString()),  result.getInt(Fields.BOX_P_CARTON.toString()),
//					result.getInt(Fields.BP_P_BOX.toString()), result.getInt(Fields.PILL_P_BP.toString()),
//				 result.getDouble(Fields.PRICE_CARTON.toString()), result.getDouble(Fields.PRICE_BOX.toString()),
//				 result.getDouble(Fields.PRICE_BP.toString()),
//				result.getDouble(Fields.PRICE_PILL.toString()), result.getDouble(Fields.PRICE_CARTON_CR.toString()),
//				result.getDouble(Fields.PRICE_BOX_CR.toString()), result.getDouble(Fields.PRICE_BP_CR.toString()),
//				result.getDouble(Fields.PRICE_PILL_CR.toString()), ProductState.valueOf(result.getString(Fields.STATE.toString()).toUpperCase()));


		if(stmt != null)
			stmt.close();

//		return temp;
	}

	public static void updateProductStates(int range) throws SQLException {

		Statement stmt = conn.createStatement();
		stmt.executeQuery("Update Product SET State = \'" + ProductState.CRITICAL + "\' WHERE " + Fields.QTY_CARTONS + " <= " + range
				+ " AND State = \'" + ProductState.UNLOCKED + "\'");
		stmt.executeQuery("Update Product SET State = \'" + ProductState.LOCKED_CRITICAL + "\' WHERE " + Fields.QTY_CARTONS + " <= " + range
				+ " AND State = \'" + ProductState.LOCKED + "\'");

		if(stmt != null) stmt.close();
	}

	public static ArrayList<Product> getUpdatedProducts(List<Product> productIDs) throws SQLException {

		ArrayList<Product> list = new ArrayList<>();
		StringBuilder sb = new StringBuilder("(");
		for(Product p : productIDs) {
			sb.append("\'").append(p.getProductID()).append("\',");
		}
		sb.setCharAt(sb.length()-1, ')');
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Product WHERE " + Fields.ID + " IN " + sb.toString());

		ResultSet result = stmt.executeQuery();

		while(result.next()) {
			Product temp = new Product(result.getString(Fields.ID.toString()), result.getString(Fields.NAME.toString()),
					result.getString(Fields.TYPE.toString()), result.getString(Fields.COMPOSITION.toString()),
					result.getString(Fields.DESCRIPTION.toString()), result.getString(Fields.SUPPLIER.toString()),
					result.getString(Fields.COMPANY.toString()), result.getBoolean(Fields.LEGAL.toString()),
					result.getString(Fields.PACKING.toString()), result.getDate(Fields.EXPIRY.toString()),
					 new Date(result.getTimestamp(Fields.ENTRY.toString()).getTime()),result.getInt(Fields.QTY_CARTONS.toString()),
					 result.getInt(Fields.QTY_BOX.toString()), result.getInt(Fields.QTY_BP.toString()),
					 result.getInt(Fields.QTY_PILLS.toString()),  result.getInt(Fields.BOX_P_CARTON.toString()),
						result.getInt(Fields.BP_P_BOX.toString()), result.getInt(Fields.PILL_P_BP.toString()),
					 result.getDouble(Fields.PRICE_CARTON.toString()), result.getDouble(Fields.PRICE_BOX.toString()),
					 result.getDouble(Fields.PRICE_BP.toString()),
					result.getDouble(Fields.PRICE_PILL.toString()), result.getDouble(Fields.PRICE_CARTON_CR.toString()),
					result.getDouble(Fields.PRICE_BOX_CR.toString()), result.getDouble(Fields.PRICE_BP_CR.toString()),
					result.getDouble(Fields.PRICE_PILL_CR.toString()), ProductState.valueOf(result.getString(Fields.STATE.toString()).toUpperCase()));
			list.add(temp);
		}

		if(result != null) result.close();
		if(stmt != null) stmt.close();

		return list;
	}

	public static Product getProductByID(String id) throws SQLException {

		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Product WHERE " + Fields.ID + " = ?");
		stmt.setString(1,  id);
		ResultSet result = stmt.executeQuery();
		Product temp = null;
		if(result.next()) {
			temp = new Product(result.getString(Fields.ID.toString()), result.getString(Fields.NAME.toString()),
					result.getString(Fields.TYPE.toString()), result.getString(Fields.COMPOSITION.toString()),
					result.getString(Fields.DESCRIPTION.toString()), result.getString(Fields.SUPPLIER.toString()),
					result.getString(Fields.COMPANY.toString()), result.getBoolean(Fields.LEGAL.toString()),
					result.getString(Fields.PACKING.toString()), result.getDate(Fields.EXPIRY.toString()),
					new Date(result.getTimestamp(Fields.ENTRY.toString()).getTime()),result.getInt(Fields.QTY_CARTONS.toString()),
					result.getInt(Fields.QTY_BOX.toString()), result.getInt(Fields.QTY_BP.toString()),
					result.getInt(Fields.QTY_PILLS.toString()),  result.getInt(Fields.BOX_P_CARTON.toString()),
					result.getInt(Fields.BP_P_BOX.toString()), result.getInt(Fields.PILL_P_BP.toString()),
					result.getDouble(Fields.PRICE_CARTON.toString()), result.getDouble(Fields.PRICE_BOX.toString()),
					result.getDouble(Fields.PRICE_BP.toString()),
					result.getDouble(Fields.PRICE_PILL.toString()), result.getDouble(Fields.PRICE_CARTON_CR.toString()),
					result.getDouble(Fields.PRICE_BOX_CR.toString()), result.getDouble(Fields.PRICE_BP_CR.toString()),
					result.getDouble(Fields.PRICE_PILL_CR.toString()), ProductState.valueOf(result.getString(Fields.STATE.toString()).toUpperCase()));
		}

		if(result != null) result.close();
		if(stmt != null) stmt.close();
		return temp;
	}

	public static ArrayList<Product> getCriticalProductsList(int range, boolean includeLocked) throws SQLException{
		ArrayList<Product> list = new ArrayList<>();

		Statement stmt = conn.createStatement();

		String sql = new String("SELECT * FROM Product WHERE State IN (\'" + ProductState.CRITICAL + "\',\'" +
				ProductState.LOCKED_CRITICAL + "\') ");

		if(includeLocked)
			sql += " OR State = \'" + ProductState.LOCKED + "\' ";

		sql += "ORDER BY State";

		ResultSet result = stmt.executeQuery(sql);
		while (result.next()) {
			Product temp = new Product(result.getString(Fields.ID.toString()), result.getString(Fields.NAME.toString()),
					result.getString(Fields.TYPE.toString()), result.getString(Fields.COMPOSITION.toString()),
					result.getString(Fields.DESCRIPTION.toString()), result.getString(Fields.SUPPLIER.toString()),
					result.getString(Fields.COMPANY.toString()), result.getBoolean(Fields.LEGAL.toString()),
					result.getString(Fields.PACKING.toString()), result.getDate(Fields.EXPIRY.toString()),
					 new Date(result.getTimestamp(Fields.ENTRY.toString()).getTime()),result.getInt(Fields.QTY_CARTONS.toString()),
					 result.getInt(Fields.QTY_BOX.toString()), result.getInt(Fields.QTY_BP.toString()),
					 result.getInt(Fields.QTY_PILLS.toString()),  result.getInt(Fields.BOX_P_CARTON.toString()),
						result.getInt(Fields.BP_P_BOX.toString()), result.getInt(Fields.PILL_P_BP.toString()),
					 result.getDouble(Fields.PRICE_CARTON.toString()), result.getDouble(Fields.PRICE_BOX.toString()),
					 result.getDouble(Fields.PRICE_BP.toString()),
					result.getDouble(Fields.PRICE_PILL.toString()), result.getDouble(Fields.PRICE_CARTON_CR.toString()),
					result.getDouble(Fields.PRICE_BOX_CR.toString()), result.getDouble(Fields.PRICE_BP_CR.toString()),
					result.getDouble(Fields.PRICE_PILL_CR.toString()),
					ProductState.valueOf(result.getString(Fields.STATE.toString()).toUpperCase()));

			list.add(temp);
		}

		if(result != null)
			result.close();
		if(stmt != null)
			stmt.close();

		return list;

	}

	public static void addProduct(Product p) throws SQLException {
		PreparedStatement pStmt = conn.
				prepareStatement("INSERT INTO Product "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		pStmt.setString(1, p.getProductID());
		pStmt.setString(2, p.getType());
		pStmt.setBoolean(3, p.getLegal());
		pStmt.setString(4, p.getName());
		pStmt.setString(5, p.getPacking());
		pStmt.setString(6, p.getSupplier());
		pStmt.setString(7, p.getCompany());
		pStmt.setDate(8, p.getdExpiry());
		pStmt.setString(9, p.getComposition());
		pStmt.setString(10, p.getDescription());
		pStmt.setDouble(11, p.getPriceCarton());
		pStmt.setDouble(12, p.getPriceBox());
		pStmt.setDouble(13, p.getPriceBP());
		pStmt.setDouble(14, p.getPricePill());
		pStmt.setDouble(15, p.getPriceCartonCR());
		pStmt.setDouble(16, p.getPriceBoxCR());
		pStmt.setDouble(17, p.getPriceBPCR());
		pStmt.setDouble(18, p.getPricePillCR());
		pStmt.setInt(19, p.getQtyCartons());
		pStmt.setInt(20, p.getQtyBoxes());
		pStmt.setInt(21, p.getQtyBP());
		pStmt.setInt(22, p.getQtyPills());
		pStmt.setInt(23, p.getBoxPCarton());
		pStmt.setInt(24, p.getBPPBox());
		pStmt.setInt(25, p.getPillPBP());
		pStmt.setDate(26, p.getdEntry());
		pStmt.setString(27, p.getProductState().toString());
		pStmt.execute();

		if(pStmt != null)
			pStmt.close();

	}

	public static Date getEntryDate(String id) throws SQLException {
		PreparedStatement stmt = conn.
				prepareStatement("SELECT EntryDate FROM Product WHERE ProductID = ?");
		stmt.setString(1, id);

		ResultSet result = stmt.executeQuery();
		Date entry = null;
		if(result.next())
			entry = result.getDate(1);

		if(result != null) result.close();
		if(stmt != null) stmt.close();

		return entry;
	}

	public static void updateProduct(Product p) throws SQLException {
		PreparedStatement pStmt = conn.
				prepareStatement("UPDATE Product SET Type = ?, Legal = ?, ProductName = ?, Packing = ?, "
						+ "Supplier = ?, Company = ?, ExpiryDate = ?, Composition = ?, Description = ?, PriceCarton = ?, "
						+ "PriceBox = ?, PriceBP = ?, PricePill = ?, PriceCartonCR = ?, PriceBoxCR = ?, PriceBPCR = ?, PricePillCR = ?, "
						+ "QtyCarton = ?, QtyBox = ?, QtyBP = ?, QtyPill = ?, BoxPCarton = ?, BPPBox = ?, PillPBP = ?, State = ?"
						+ " WHERE ProductID = ?");

		pStmt.setString(1, p.getType());
		pStmt.setBoolean(2, p.getLegal());
		pStmt.setString(3, p.getName());
		pStmt.setString(4, p.getPacking());
		pStmt.setString(5, p.getSupplier());
		pStmt.setString(6, p.getCompany());
		pStmt.setDate(7, new Date(p.getdExpiry().getTime()));
		pStmt.setString(8, p.getComposition());
		pStmt.setString(9, p.getDescription());
		pStmt.setDouble(10, p.getPriceCarton());
		pStmt.setDouble(11, p.getPriceBox());
		pStmt.setDouble(12, p.getPriceBP());
		pStmt.setDouble(13, p.getPricePill());
		pStmt.setDouble(14, p.getPriceCartonCR());
		pStmt.setDouble(15, p.getPriceBoxCR());
		pStmt.setDouble(16, p.getPriceBPCR());
		pStmt.setDouble(17, p.getPricePillCR());
		pStmt.setInt(18, p.getQtyCartons());
		pStmt.setInt(19, p.getQtyBoxes());
		pStmt.setInt(20, p.getQtyBP());
		pStmt.setInt(21, p.getQtyPills());
		pStmt.setInt(22, p.getBoxPCarton());
		pStmt.setInt(23, p.getBPPBox());
		pStmt.setInt(24, p.getPillPBP());
		pStmt.setString(25, p.getProductState().toString());
		pStmt.setString(26, p.getProductID());
		pStmt.execute();

		if(pStmt != null)
			pStmt.close();

	}

	public static void deleteProduct(String id) throws SQLException{
		PreparedStatement pStmt = conn
					.prepareStatement("DELETE FROM Product WHERE ProductID = ?");
		pStmt.setString(1, id);
		pStmt.execute();

		if(pStmt != null)
			pStmt.close();

	}

	public static int getProductCount() throws SQLException {

		Statement stmt = conn
				.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet result = stmt.executeQuery("SELECT COUNT(*) FROM Product");
		int value = -1;
		while(result.next()){
			value = result.getInt(1);
		}

		if(result != null)
			result.close();
		if(stmt != null)
			stmt.close();

		return value;
	}

	public static ObservableList<Product> getProductList() throws SQLException {

			ObservableList<Product> list = FXCollections.observableArrayList();

			Statement stmt = conn
					.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet result = stmt.executeQuery("SELECT * FROM Product");
			while (result.next()) {
				Product temp = new Product(result.getString(Fields.ID.toString()), result.getString(Fields.NAME.toString()),
						result.getString(Fields.TYPE.toString()), result.getString(Fields.COMPOSITION.toString()),
						result.getString(Fields.DESCRIPTION.toString()), result.getString(Fields.SUPPLIER.toString()),
						result.getString(Fields.COMPANY.toString()), result.getBoolean(Fields.LEGAL.toString()),
						result.getString(Fields.PACKING.toString()), result.getDate(Fields.EXPIRY.toString()),
						 new Date(result.getTimestamp(Fields.ENTRY.toString()).getTime()),result.getInt(Fields.QTY_CARTONS.toString()),
						 result.getInt(Fields.QTY_BOX.toString()), result.getInt(Fields.QTY_BP.toString()),
						 result.getInt(Fields.QTY_PILLS.toString()),  result.getInt(Fields.BOX_P_CARTON.toString()),
							result.getInt(Fields.BP_P_BOX.toString()), result.getInt(Fields.PILL_P_BP.toString()),
						 result.getDouble(Fields.PRICE_CARTON.toString()), result.getDouble(Fields.PRICE_BOX.toString()),
						 result.getDouble(Fields.PRICE_BP.toString()),
						result.getDouble(Fields.PRICE_PILL.toString()), result.getDouble(Fields.PRICE_CARTON_CR.toString()),
						result.getDouble(Fields.PRICE_BOX_CR.toString()), result.getDouble(Fields.PRICE_BP_CR.toString()),
						result.getDouble(Fields.PRICE_PILL_CR.toString()), ProductState.valueOf(result.getString(Fields.STATE.toString()).toUpperCase()));
				list.add(temp);
			}

			if(result != null)
				result.close();
			if(stmt != null)
				stmt.close();

			return list;
	}

	public static ArrayList<Product> searchProduct(String query, Fields searchBy) throws SQLException {

		ArrayList<Product> list = new ArrayList<>();
		String sql = "";
		switch(searchBy) {
		case ID:
			sql = "SELECT * FROM Product WHERE State = \'" + ProductState.LOCKED + "\' OR " + searchBy.toString() + " LIKE '%" + query + "%'";
			break;
		case LEGAL:
			boolean isLegal = query.equals("legal") ? true : false;
			sql = "SELECT * FROM Product WHERE State = \'" + ProductState.LOCKED + "\' OR " + searchBy.toString() + " = " + isLegal;
			break;
		case TYPE:
			query = query.replace(",", "\',\'");
			sql = "SELECT * FROM Product WHERE State = \'" + ProductState.LOCKED + "\' OR " + searchBy.toString() + " IN ('" + query + "')";
			break;
		case EXPIRY:
		case ENTRY:
		case QTY_CARTONS:
		case QTY_BOX:
		case QTY_BP:
		case PRICE_CARTON:
		case PRICE_CARTON_CR:
		case PRICE_BOX:
		case PRICE_BOX_CR:
		case PRICE_BP:
		case PRICE_BP_CR:
		case PRICE_PILL:
		case PRICE_PILL_CR:
		case BOX_P_CARTON:
		case BP_P_BOX:
		case PILL_P_BP:
			sql = "SELECT * FROM Product WHERE State = \'" + ProductState.LOCKED + "\' OR (" + searchBy.toString() + " BETWEEN " + query + ")";
			break;
		default:
			sql = "SELECT * FROM Product WHERE State = \'" + ProductState.LOCKED + "\' OR REGEXP_MATCHES(LOWER("
					+ searchBy.toString() + "),'.*" + query + ".*')";
		}
		PreparedStatement pStmt = conn.prepareStatement(sql);

		ResultSet result = pStmt.executeQuery();
		while(result.next()) {
			list.add(new Product (
				result.getString("ProductID"),
				result.getString("ProductName"),
				result.getString("Type"),
				result.getString("Composition"),
				result.getString("Description"),
				result.getString("Supplier"),
				result.getString("Company"),
				result.getBoolean("Legal"),
				result.getString("Packing"),
				result.getDate("ExpiryDate"),
				new Date(result.getTimestamp("Entry").getTime()),
				result.getInt("QtyCarton"),
				result.getInt("QtyBox"),
				result.getInt("QtyBP"),
				result.getInt("QtyPill"),
				result.getInt("BoxPCarton"),
				result.getInt("BPPBox"),
				result.getInt("PillPBP"),
				result.getDouble("PriceCarton"),
				result.getDouble("PriceBox"),
				result.getDouble("PriceBP"),
				result.getDouble("PricePill"),
				result.getDouble("PriceCartonCR"),
				result.getDouble("PriceBoxCR"),
				result.getDouble("PriceBPCR"),
				result.getDouble("PricePillCR"),
				ProductState.valueOf(result.getString("State").toUpperCase())
			));
		}
		if(result != null) result.close();
		if(pStmt != null) pStmt.close();

		return list;
	}

	public static void updateState(String id, ProductState state) throws SQLException {
		PreparedStatement pStmt = null;
		pStmt = conn.prepareStatement("UPDATE Product SET State = ? WHERE ProductID = ?");

		pStmt.setString(1, state.toString());
		pStmt.setString(2,  id);
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

