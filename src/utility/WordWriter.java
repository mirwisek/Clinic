package utility;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import database.InvoiceManager;
import model.Line;
import model.PrintableInvoice;

public class WordWriter {

	public static final String INPUT_FILE_NAME = "invoice_template.docx";
	public static final String OUTPUT_FILE_NAME = "temp.docx";

	private static final int INITIAL_LINE_ROWS = 7;

	private static int initialRowHeight = 0;

	public static void write(int invoiceID) throws Exception {
		PrintableInvoice invoice = InvoiceManager.getPrintableInvoice(invoiceID);

		XWPFDocument docx = new XWPFDocument(new FileInputStream(INPUT_FILE_NAME));
		List<XWPFTable> tablesList = docx.getTables();
		XWPFTable inTable = tablesList.get(0);
		XWPFTable clientTable = tablesList.get(1);
		XWPFTable lineTable = tablesList.get(2);
		XWPFTable totalTable = tablesList.get(3);

		XWPFParagraph para =  inTable.getRow(1).getCell(1).getParagraphs().get(0);
		XWPFRun run = para.createRun();
		run.setText("Invoice # ");
		run.setText(invoice.getInvoiceID());
		run.addBreak();
		run.setText(invoice.getEntry());

		para =  clientTable.getRow(0).getCell(1).getParagraphs().get(0);
		run = para.createRun();
		run.setText(invoice.getClientID());
		run.addBreak();
		run.setText(invoice.getClientName());
		run.addBreak();
		run.setText(invoice.getClientPhone());

		addLines(lineTable, invoice);

		totalTable.getRow(0).getCell(1).setText(invoice.getTotalAmount());

		docx.write(new FileOutputStream(OUTPUT_FILE_NAME));


		docx.close();
	}

	private static void addLines(XWPFTable table, PrintableInvoice invoice) {
		List<XWPFTableRow> rows = table.getRows();
		// Do consider header row
		int linesCount = invoice.getLinesCount() + 1;
		// Retrieve the second row instead of header row
		initialRowHeight = rows.get(1).getHeight();
		while(linesCount >= table.getNumberOfRows())
			table.createRow();
		rows = table.getRows();
		int row = 1;
		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");

		for(Line line : invoice.getLines()) {
			rows.get(row).getCell(0).setText(String.valueOf(line.getLineNumber()));
			rows.get(row).getCell(1).setText(line.getName());
			rows.get(row).getCell(2).setText(line.getProductID());
			rows.get(row).getCell(3).setText(String.valueOf(line.getQuantity()));
			rows.get(row).getCell(4).setText(String.valueOf(line.getDiscount()));
			rows.get(row).getCell(5).setText(String.valueOf(line.getPacking()));
			rows.get(row).getCell(6).setText(f.format(line.getdExpiry()));
			rows.get(row).getCell(7).setText(String.valueOf(line.getPrice()));
			rows.get(row).getCell(8).setText(line.getNetTotalFormated());
			row++;
		}
		if(table.getNumberOfRows() > INITIAL_LINE_ROWS) {
			int height = getCalculatedHeight(table.getNumberOfRows());
			// Equally distribute rows
			for(XWPFTableRow r : rows) {
				r.setHeight(height);
			}
		}
	}

	private static int getCalculatedHeight(int finalNumberOfRows) {
		int tableHeight = initialRowHeight * INITIAL_LINE_ROWS;
		return tableHeight/finalNumberOfRows;
	}

}
