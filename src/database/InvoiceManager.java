package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;

import model.Invoice;
import model.Line;
import model.PrintableInvoice;
import model.Product;

public class InvoiceManager {

	private static Savepoint savepoint = null;

	private static Connection conn = DatabaseManager.getInstance().getConnection();

	public enum Fields {
		INVOICE_ID("InvoiceID"),
		INVOICE_ID_RANGE("InvoiceID"),
		CLIENT_ID("ClientID"),
		CLIENT_NAME("ClientName"),
		TOTAL_AMOUNT("TotalAmount"),
		TOTAL_AMOUNT_RANGE("TotalAmount"),
		ENTRY("Entry");

		private String value;

		Fields(String value){ this.value = value; }

		@Override
		public String toString() {
			return value;
		}
	}

	public static void setSavepoint() throws SQLException {
		if(conn != null)
			savepoint = conn.setSavepoint();
		else {
			conn = DatabaseManager.getInstance().getConnection();
			savepoint = conn.setSavepoint();
		}
	}

	public static void commit() throws SQLException {
		if(savepoint != null) {
			conn.commit();
			savepoint = null;
		}
	}

	public static void rollBack() throws SQLException {
		if(savepoint != null) {
			conn.rollback(savepoint);
			savepoint = null;
		}
	}

	public static int createInvoice() throws SQLException {
		int invoiceID = -1;
		PreparedStatement idStmt = conn
				.prepareStatement("SELECT MAX(InvoiceID) FROM Invoice");
		PreparedStatement pStmt = conn
				.prepareStatement("INSERT INTO Invoice VALUES (?, NULL, NOW(), NULL, NULL);");
		ResultSet result = idStmt.executeQuery();
		if(result.next())
			invoiceID = result.getInt(1)+1;
		else
			invoiceID = 1;

		pStmt.setInt(1, invoiceID);
		pStmt.execute();

		if(result != null) result.close();
		if(idStmt != null) idStmt.close();
		if(pStmt != null) pStmt.close();

		return invoiceID;
	}
	// Called when invoice create button is clicked
	public static void updateEntry() throws SQLException {
		PreparedStatement pStmt = conn
				.prepareStatement("UPDATE Invoice SET Entry = NOW()");

		pStmt.execute();

		if(pStmt != null) pStmt.close();
	}

	public static Line addLineToInvoice(Line line, int invoiceID) throws Exception {

		ProductManager.Fields[] temp = getPriceBy(line.getPriceBy());
		String priceBy = temp[0].toString();
		ProductManager.Fields qtyType = temp[1];

		// Write new product quantities
		updateProductQuantities(line, invoiceID, qtyType, true);

		PreparedStatement updatePStmt = null;
		System.out.println("Price " + priceBy);
		PreparedStatement linePStmt = conn
				.prepareStatement("INSERT INTO Line VALUES (?, ?, ?, ?, "
				+ "(SELECT " + priceBy + " FROM Product WHERE ProductID = ?), ?, NULL, ?, NULL);");

		linePStmt.setInt(1, invoiceID);
		linePStmt.setString(2, line.getProductID());
		linePStmt.setInt(3, line.getLineNumber());
		linePStmt.setString(4, line.getPriceBy());
		// Price is retrieved automatically
		linePStmt.setString(5, line.getProductID());
		linePStmt.setInt(6, line.getQuantity());
		linePStmt.setDouble(7, line.getDiscount());

		linePStmt.execute();

		updatePStmt = conn
				.prepareStatement("UPDATE Line SET SubTotal = Quantity * Price "
				+ "WHERE InvoiceID = ? AND LineNumber = ?");
		updatePStmt.setInt(1, invoiceID);
		updatePStmt.setInt(2, line.getLineNumber());
		updatePStmt.execute();

		updatePStmt = conn
				.prepareStatement("UPDATE Line SET NetTotal = (SubTotal-(SubTotal*Discount)/100) "
				+ "WHERE InvoiceID = ? AND LineNumber = ?");
		updatePStmt.setInt(1, invoiceID);
		updatePStmt.setInt(2, line.getLineNumber());
		updatePStmt.execute();

		PreparedStatement stmt = conn
				.prepareStatement("SELECT * FROM Line WHERE InvoiceID = ? AND LineNumber = ?");
		PreparedStatement stmtProduct = conn
				.prepareStatement("SELECT ProductName, Packing FROM Product WHERE ProductID = ?");

		stmt.setInt(1, invoiceID);
		stmt.setInt(2, line.getLineNumber());


		ResultSet res = stmt.executeQuery();
		Line addedLine = null;
		while(res.next()) {
			addedLine = new Line();
			String pid = res.getString("ProductID");
			stmtProduct.setString(1, pid);
			ResultSet resProduct = stmtProduct.executeQuery();
			while(resProduct.next()) {
				addedLine.setName(resProduct.getString("ProductName"));
				addedLine.setPacking(resProduct.getString("Packing"));
			}
			addedLine.setLineNumber(res.getInt("LineNumber"));
			addedLine.setPrice(res.getDouble("Price"));
			addedLine.setQuantity(res.getInt("Quantity"));
			addedLine.setPriceBy(res.getString("PriceBy"));
			addedLine.setProductID(pid);
			addedLine.setDiscount(res.getDouble("Discount"));
			addedLine.setSubTotal(res.getDouble("SubTotal"));
			addedLine.setNetTotal(res.getDouble("NetTotal"));
		}

		if(res != null) res.close();
		if(stmt != null) stmt.close();
		if(updatePStmt != null) updatePStmt.close();
		if(linePStmt != null) linePStmt.close();

		return addedLine;
	}
	public static Line updateLine(Line oldLine, Line newLine, int invoiceID) throws Exception {

		ProductManager.Fields[] temp = getPriceBy(newLine.getPriceBy());
		String priceBy = temp[0].toString();
		ProductManager.Fields qtyType = temp[1];

		// Restore old product quantities
		updateProductQuantities(oldLine, invoiceID, getPriceBy(oldLine.getPriceBy())[1], false);

		// Write new product quantities
		updateProductQuantities(newLine, invoiceID, qtyType, true);
		PreparedStatement updatePStmt = null;

		PreparedStatement linePStmt = conn
				.prepareStatement("UPDATE Line SET ProductID = ?, Discount = ?, Quantity = ?, PriceBy = ?, Price = "
						+ "(SELECT " + priceBy + " FROM Product WHERE ProductID = ?) WHERE LineNumber = ? AND InvoiceID = ?");
		linePStmt.setString(1, newLine.getProductID());
		linePStmt.setDouble(2, newLine.getDiscount());
		linePStmt.setInt(3, newLine.getQuantity());
		linePStmt.setString(4, newLine.getPriceBy());
		linePStmt.setString(5, newLine.getProductID());
		linePStmt.setInt(6, newLine.getLineNumber());
		linePStmt.setInt(7, invoiceID);
		linePStmt.execute();

		updatePStmt = conn
				.prepareStatement("UPDATE Line SET SubTotal = Quantity * Price "
						+ "WHERE InvoiceID = ? AND LineNumber = ?");
		updatePStmt.setInt(1, invoiceID);
		updatePStmt.setInt(2, newLine.getLineNumber());
		updatePStmt.execute();

		updatePStmt = conn
				.prepareStatement("UPDATE Line SET NetTotal = (SubTotal-(SubTotal*Discount)/100) "
						+ "WHERE InvoiceID = ? AND LineNumber = ?");
		updatePStmt.setInt(1, invoiceID);
		updatePStmt.setInt(2, newLine.getLineNumber());
		updatePStmt.execute();


		PreparedStatement stmt = conn
				.prepareStatement("SELECT * FROM Line WHERE InvoiceID = ? AND LineNumber = ?");
		PreparedStatement stmtProduct = conn
				.prepareStatement("SELECT ProductName, Packing FROM Product WHERE ProductID = ?");

		stmt.setInt(1, invoiceID);
		stmt.setInt(2, newLine.getLineNumber());

		ResultSet res = stmt.executeQuery();
		Line addedLine = null;
		while(res.next()) {
			addedLine = new Line();
			String pid = res.getString("ProductID");
			stmtProduct.setString(1, pid);
			ResultSet resProduct = stmtProduct.executeQuery();
			while(resProduct.next()) {
				addedLine.setName(resProduct.getString("ProductName"));
				addedLine.setPacking(resProduct.getString("Packing"));
			}
			addedLine.setLineNumber(res.getInt("LineNumber"));
			addedLine.setPrice(res.getDouble("Price"));
			addedLine.setQuantity(res.getInt("Quantity"));
			addedLine.setPriceBy(res.getString("PriceBy"));
			addedLine.setProductID(pid);
			addedLine.setDiscount(res.getDouble("Discount"));
			addedLine.setSubTotal(res.getDouble("SubTotal"));
			addedLine.setNetTotal(res.getDouble("NetTotal"));
		}

		if(res != null) res.close();
		if(stmt != null) stmt.close();
		if(updatePStmt != null) updatePStmt.close();
		if(linePStmt != null) linePStmt.close();

		return addedLine;
	}

	public static void updateProductQuantities(Line line, int invoiceID, ProductManager.Fields qtyType, boolean deduct) throws Exception {

		PreparedStatement updatePStmt = null;

		updatePStmt = conn
				.prepareStatement("SELECT QtyCarton, QtyBox, QtyBP, QtyPill, BoxPCarton, BPPBox, PillPBP FROM Product WHERE ProductID = ?");
		updatePStmt.setString(1, line.getProductID());

		ResultSet res = updatePStmt.executeQuery();

		if(res.next()) {
			line.setQtyCartons(res.getInt("QtyCarton"));
			line.setQtyBoxes(res.getInt("QtyBox"));
			line.setQtyBP(res.getInt("QtyBP"));
			line.setQtyPills(res.getInt("QtyPill"));
			line.setBoxPCarton(res.getInt("BoxPCarton"));
			line.setBPPBox(res.getInt("BPPBox"));
			line.setPillPBP(res.getInt("PillPBP"));
		}else
			throw new Exception("Selected product couldn't be retrieved!");

		line = deductQuantity(line, qtyType, deduct);

		updatePStmt = conn.prepareStatement("UPDATE Product Set QtyCarton = ?, QtyBox = ?, QtyBP = ?, QtyPill = ? WHERE ProductID = ?");
		updatePStmt.setInt(1, line.getQtyCartons());
		updatePStmt.setInt(2, line.getQtyBoxes());
		updatePStmt.setInt(3, line.getQtyBP());
		updatePStmt.setInt(4, line.getQtyPills());
		updatePStmt.setString(5, line.getProductID());
		updatePStmt.execute();

		if(res != null) res.close();
		if(updatePStmt != null) updatePStmt.close();

	}

	public static ProductManager.Fields[] getPriceBy(String priceBy) throws Exception {
		ProductManager.Fields[] values = new ProductManager.Fields[2];
		if(priceBy.equals(Product.PriceType.CARTON.toString())) {
			values[0] = ProductManager.Fields.PRICE_CARTON;
			values[1] = ProductManager.Fields.QTY_CARTONS;
		}
		else if(priceBy.equals(Product.PriceType.BOX.toString())) {
			values[0] = ProductManager.Fields.PRICE_BOX;
			values[1] = ProductManager.Fields.QTY_BOX;
		}
		else if(priceBy.equals(Product.PriceType.BP.toString())) {
			values[0] = ProductManager.Fields.PRICE_BP;
			values[1] = ProductManager.Fields.QTY_BP;
		}
		else if(priceBy.equals(Product.PriceType.PILL.toString())) {
			values[0] = ProductManager.Fields.PRICE_PILL;
			values[1] = ProductManager.Fields.QTY_PILLS;
		}
		else if(priceBy.equals(Product.PriceType.CARTON_CR.toString())) {
			values[0] = ProductManager.Fields.PRICE_CARTON_CR;
			values[1] = ProductManager.Fields.QTY_CARTONS;
		}
		else if(priceBy.equals(Product.PriceType.BOX_CR.toString())) {
			values[0] = ProductManager.Fields.PRICE_BOX_CR;
			values[1] = ProductManager.Fields.QTY_BOX;
		}
		else if(priceBy.equals(Product.PriceType.BP_CR.toString())) {
			values[0] = ProductManager.Fields.PRICE_BP_CR;
			values[1] = ProductManager.Fields.QTY_BP;
		}
		else if(priceBy.equals(Product.PriceType.PILL_CR.toString())) {
			values[0] = ProductManager.Fields.PRICE_PILL_CR;
			values[1] = ProductManager.Fields.QTY_PILLS;
		} else {
			throw new Exception("No priceBy match in InvoiceManager::getPriceBy");
		}
		return values;
	}

	public static void deleteLine(Line line, int invoiceID) throws Exception {

		updateProductQuantities(line, invoiceID, getPriceBy(line.getPriceBy())[1], false);

		PreparedStatement pStmt = conn
				.prepareStatement("DELETE FROM Line WHERE LineNumber = ? AND InvoiceID = ?");
		pStmt.setInt(1, line.getLineNumber());
		pStmt.setInt(2, invoiceID);
		pStmt.execute();
		if(pStmt != null)	pStmt.close();

		PreparedStatement updatePStmt = conn
				.prepareStatement("UPDATE Line SET LineNumber = LineNumber - 1 WHERE InvoiceID = ? AND LineNumber > ?");
		updatePStmt.setInt(1, invoiceID);
		updatePStmt.setInt(2, line.getLineNumber());
		updatePStmt.execute();
		if(updatePStmt != null)	updatePStmt.close();


	}

	public static void updateInvoiceClient(String clientID, int invoiceID) throws SQLException {
		PreparedStatement pStmt = conn
						.prepareStatement("UPDATE Invoice SET ClientID = ? WHERE InvoiceID = ?");

		pStmt.setString(1, clientID);
		pStmt.setInt(2, invoiceID);
		pStmt.execute();
		if(pStmt != null)	pStmt.close();

	}

	public static void updateInvoiceAmount(int id) throws SQLException {
		PreparedStatement pStmt = conn
				.prepareStatement("UPDATE Invoice SET TotalAmount = "
					+ "(SELECT SUM(NetTotal) FROM Line WHERE InvoiceID = ?) WHERE InvoiceID = ?");

		pStmt.setInt(1, id);
		pStmt.setInt(2, id);
		pStmt.execute();

	}

	public static void deleteInvoice(int id) throws SQLException {

		PreparedStatement pStmt = conn
				.prepareStatement("DELETE FROM Invoice WHERE InvoiceID = ?");
		pStmt.setInt(1, id);
		pStmt.execute();
	}

	public static ArrayList<Invoice> getInvoiceList() throws SQLException {

		ArrayList<Invoice> list = new ArrayList<>();

		PreparedStatement stmt = conn
				.prepareStatement("SELECT * FROM Invoice");
		PreparedStatement pStmt = conn
				.prepareStatement("SELECT ClientName FROM Client WHERE ClientID = ?");

		ResultSet result = stmt.executeQuery();
		ResultSet res = null;
		while (result.next()) {
			String cid = result.getString("ClientID");
			pStmt.setString(1, cid);
			res = pStmt.executeQuery();
			while(res.next()) {
				Invoice in = new Invoice(result.getInt("InvoiceID"), cid, res.getString("ClientName"),
						new Date(result.getTimestamp("Entry").getTime()),
						result.getDouble("TotalAmount"), result.getBoolean("Locked"));
				list.add(in);
			}
		}

		if(res != null) res.close();
		if(pStmt != null) pStmt.close();
		if(result != null) result.close();
		if(stmt != null) stmt.close();

		return list;
}

	public static void updateLock(int id, boolean locked) throws SQLException {
		PreparedStatement pStmt = null;
		pStmt = conn
				.prepareStatement("UPDATE Invoice SET Locked = ? WHERE InvoiceID = ?");

		pStmt.setBoolean(1, locked);
		pStmt.setInt(2,  id);

		pStmt.execute();

		if(pStmt != null)
			pStmt.close();
	}

	public static Invoice getInvoice(int id) throws SQLException {
		Invoice in = null;
		PreparedStatement pStmt = null;
		pStmt = conn
				.prepareStatement("SELECT * FROM Invoice WHERE InvoiceID = ?");
		PreparedStatement stmt = conn
				.prepareStatement("SELECT ClientName FROM Client WHERE ClientID = ?");

		String clientID = null;
		pStmt.setInt(1,  id);
		ResultSet res = pStmt.executeQuery();
		ResultSet res2 = null;

		while(res.next()) {
			clientID = res.getString("ClientID");
			in = new Invoice(id, res.getString("ClientID"), null, new Date(res.getTimestamp("Entry").getTime()), res.getDouble("TotalAmount"),
					res.getBoolean("Locked"));
			stmt.setString(1, clientID);
			res2 = stmt.executeQuery();
			while(res2.next()) {
				in.setClientName(res2.getString("ClientName"));
			}
		}

		if(res2 != null)
			res2.close();
		if(res != null)
			res.close();
		if(stmt != null)
			stmt.close();
		if(pStmt != null)
			pStmt.close();
		return in;
	}

	public static ArrayList<Invoice> searchInvoice(String query, Fields searchBy) throws SQLException {

		ArrayList<Invoice> list = new ArrayList<>();
		String sql = "";
		switch(searchBy) {
		case INVOICE_ID:
		case CLIENT_ID:
		case TOTAL_AMOUNT:
			sql = "SELECT * FROM Invoice WHERE Locked = TRUE OR " + searchBy.toString() + " LIKE '%" + query + "%'";
			break;
		case ENTRY:
		case TOTAL_AMOUNT_RANGE:
		case INVOICE_ID_RANGE:
			sql = "SELECT * FROM Invoice WHERE Locked = TRUE OR " + searchBy.toString() + " BETWEEN " + query;
			break;
		case CLIENT_NAME:
			sql = "SELECT * FROM Invoice WHERE Locked = TRUE OR ClientID = (SELECT ClientID FROM Client WHERE REGEXP_MATCHES(LOWER("
					+ searchBy.toString() + "),'.*" + query + ".*'))";
		}

		PreparedStatement pStmt = conn.prepareStatement(sql);

		PreparedStatement stmt = conn
				.prepareStatement("SELECT ClientName FROM Client WHERE ClientID = ?");

		String clientID = null;
		ResultSet result = pStmt.executeQuery();
		ResultSet res = pStmt.executeQuery();
		while(result.next()) {
			clientID = result.getString("ClientID");
			stmt.setString(1, clientID);
			Invoice in = new Invoice(result.getInt("InvoiceID"), clientID, null, new Date(result.getTimestamp("Entry").getTime()),
					result.getDouble("TotalAmount"), result.getBoolean("Locked"));
			res = stmt.executeQuery();
			while(res.next())
				in.setClientName(res.getString("ClientName"));
			list.add(in);
		}

		if(res != null) res.close();
		if(stmt != null) stmt.close();
		if(result != null) result.close();
		if(pStmt != null) pStmt.close();

		return list;
	}

	public static ArrayList<Line> getLinesList(int invoiceID) throws SQLException {

		PreparedStatement stmt = conn
				.prepareStatement("SELECT * FROM Line WHERE InvoiceID = ? ORDER BY LineNumber");
		PreparedStatement st = conn
				.prepareStatement("SELECT ProductName, Packing, ExpiryDate FROM Product WHERE ProductID = ?");
		stmt.setInt(1, invoiceID);
		ArrayList<Line> list = new ArrayList<>();
		ResultSet res = stmt.executeQuery();
		ResultSet result = null;
		while(res.next()) {
			String pid = res.getString("ProductID");
			Line line = new Line(res.getInt("LineNumber"), res.getDouble("Price"), res.getInt("Quantity"), res.getString("PriceBy"),
					pid, res.getDouble("Discount"));
			line.setSubTotal(res.getDouble("SubTotal"));
			line.setNetTotal(res.getDouble("NetTotal"));
			st.setString(1, pid);
			result = st.executeQuery();
			while(result.next()) {
				line.setName(result.getString("ProductName"));
				line.setPacking(result.getString("Packing"));
				line.setdExpiry(result.getDate("ExpiryDate"));
				list.add(line);
			}
		}
		if(result != null) result.close();
		if(res != null) res.close();
		if(st != null) st.close();
		if(stmt != null) stmt.close();
		return list;
	}

	public static PrintableInvoice getPrintableInvoice(int invoiceID) throws SQLException {
		PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM Invoice WHERE InvoiceID = ?");
		PreparedStatement stmt = conn.prepareStatement("SELECT ClientName, PhoneNumber FROM Client WHERE ClientID = ?");

		PrintableInvoice invoice = null;
		String clientID = null;
		pStmt.setInt(1,  invoiceID);
		ResultSet res = pStmt.executeQuery();
		ResultSet res2 = null;

		if(res.next()) {
			clientID = res.getString("ClientID");
			stmt.setString(1, clientID);
			res2 = stmt.executeQuery();
			if(res2.next()) {
				invoice = new PrintableInvoice(invoiceID, clientID, res2.getString("ClientName"), res2.getString("PhoneNumber"),
						new Date(res.getTimestamp("Entry").getTime()), res.getDouble("TotalAmount"), null);
				invoice.setLines(getLinesList(invoiceID));
			}
		}

		if(res2 != null)
			res2.close();
		if(res != null)
			res.close();
		if(stmt != null)
			stmt.close();
		if(pStmt != null)
			pStmt.close();
		return invoice;
	}

	public static int getNextLineNumber(int invoiceID) throws SQLException {
		int lineNumber = -1;
		PreparedStatement stmt = conn.prepareStatement("SELECT MAX(LineNumber) FROM Line WHERE InvoiceID = ?");
		stmt.setInt(1, invoiceID);
		ResultSet res = stmt.executeQuery();
		while(res.next())
			lineNumber = res.getInt(1) + 1;

		if(res != null) res.close();
		if(stmt != null) stmt.close();
		return lineNumber;
	}

	public static int getLineCount(int invoiceID) throws SQLException {
		int count = -1;
		PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Line WHERE InvoiceID = ?");
		stmt.setInt(1, invoiceID);
		ResultSet res = stmt.executeQuery();
		while(res.next())
			count = res.getInt(1);

		if(res != null) res.close();
		if(stmt != null) stmt.close();
		return count;
	}

	public static void setAutoCommitMode(boolean autoCommit) throws SQLException {
		if(conn != null)
			conn.setAutoCommit(autoCommit);
	}

	public static void closeConnection() throws SQLException {
		if(conn != null) {
			conn.close();
		}
	}

	private static Line deductQuantity(Line line, ProductManager.Fields qtyType, boolean deduct) {
		// deduct = false (ADD used for Update), deduct = true (SUBTRACT used for add)
		int finalQty = 0;
		switch(qtyType) {
		case QTY_CARTONS:
			finalQty = line.getQtyCartons();
			if(deduct)
				finalQty -= line.getQuantity();
			else
				finalQty += line.getQuantity();
			line.setQtyCartons(finalQty);
			break;
		case QTY_BOX:
			finalQty = (line.getQtyCartons() * line.getBoxPCarton()  + line.getQtyBoxes());
			if(deduct)
				finalQty -= line.getQuantity();
			else
				finalQty += line.getQuantity();
			line.setQtyCartons(finalQty / line.getBoxPCarton());
			line.setQtyBoxes(finalQty % line.getBoxPCarton());
			break;
		case QTY_BP:
			finalQty = (line.getQtyCartons() * line.getBoxPCarton() * line.getBPPBox()) +
			(line.getQtyBoxes() * line.getBPPBox()) + line.getQtyBP();
			if(deduct)
				finalQty -= line.getQuantity();
			else
				finalQty += line.getQuantity();
			int totalBoxes = finalQty / line.getBPPBox();
			line.setQtyBP(finalQty % line.getBPPBox());
			line.setQtyBoxes(totalBoxes % line.getBoxPCarton());
			line.setQtyCartons(totalBoxes / line.getBoxPCarton());
			break;
		case QTY_PILLS:
			finalQty = (line.getQtyCartons() * line.getBoxPCarton() * line.getBPPBox() * line.getPillPBP()) +
			(line.getQtyBoxes() * line.getBPPBox() * line.getPillPBP()) + (line.getQtyBP() * line.getPillPBP()) + line.getQtyPills();
			if(deduct)
				finalQty -= line.getQuantity();
			else
				finalQty += line.getQuantity();

			int totalBP = finalQty / line.getPillPBP();
			line.setQtyPills(finalQty % line.getPillPBP());

			int pairBoxes = totalBP / line.getBPPBox();
			line.setQtyBP(totalBP % line.getBPPBox());

			line.setQtyBoxes(pairBoxes % line.getBoxPCarton());
			line.setQtyCartons(pairBoxes / line.getBoxPCarton());
			break;
		default:
		}
		return line;
	}

}
