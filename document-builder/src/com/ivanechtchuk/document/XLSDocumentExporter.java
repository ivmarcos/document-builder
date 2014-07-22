package com.ivanechtchuk.document;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;

public class XLSDocumentExporter implements DocumentExporter {
	
	static Logger logger = Logger.getLogger(XLSDocumentExporter.class.getName());
	
	private Document document;
	private DocumentExpressionResolver resolver;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	private HSSFCellStyle headerStyle;
	private Map<Column, HSSFCellStyle> columnStyles;
	private Map<Column, DataType> columnDataTypes;
	
	public void export(DocumentWrapper wrapper) {
		long begin = System.currentTimeMillis();
		document = wrapper.getDocument();
		workbook = new HSSFWorkbook();
		resolver = new DocumentExpressionResolver(wrapper);
		sheet = workbook.createSheet(wrapper.getTitle());
		DocumentParser parser = new DocumentParser(wrapper);
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.addHeader("Content-disposition", "attachment; filename="+wrapper.getTitle()+".xls");
		ServletOutputStream out;
		try {
			out = response.getOutputStream();
			int rowIndex = 1;
			if (document.hasGroup()) {
				buildHeadersGrouped();
				rowIndex = document.getColumnGroup().getRows().size();
			}else {
				buildHeaders();
			}
			if (wrapper.getValues().isEmpty()) {
				logger.trace("Empty values.");
			}else {
				logger.trace("Putting values...");
				parseColumns(wrapper, parser);
				for (Object instance : wrapper.getValues()) {
					HSSFRow row = sheet.createRow(rowIndex);
					row.setHeight(XLSDocumentDefaults.ROW_HEIGHT);
					int columnIndex = 0;
					try {
						for (Column column : document.getColumns()) {
							Cell cell = row.createCell(columnIndex++);
							Object cellValue =  resolver.resolve(instance, parser.getColumnExpressions(column));
							logger.trace("Value "+cellValue);
							putCellValue(column, cell, cellValue);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				rowIndex++;
				}
			}
			//autoSizeColumns();
			long end = System.currentTimeMillis();
			logger.trace("XLS Export performance: "+(end-begin)+"ms.");
			workbook.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	private void autoSizeColumns() {
		for (int i = 0; i <= document.getColumns().size();i++) {
			sheet.autoSizeColumn(i);
		}
	}
	
	private void buildHeaders() {
		int columnIndex = 0;
		HSSFRow rowHeader = sheet.createRow(0);
		for (Column column : document.getColumns()) {
			Cell cell = rowHeader.createCell(columnIndex++);
			cell.setCellValue(column.getHeader());
			cell.setCellStyle(getHeaderStyle());
		}
	}
	
	private void parseColumns(DocumentWrapper documentData, DocumentParser parser) {
		logger.trace("Mapping columns...");
		Object instance = documentData.getValues().toArray()[0];
		columnStyles = new HashMap<Column, HSSFCellStyle>();
		columnDataTypes = new HashMap<Column, DataType>();
		try {
			for (Column column : document.getColumns()) {
				Object value = resolver.resolve(instance, parser.getColumnExpressions(column));
				if (value==null) {
					mapColumn(column, DataType.NULL);
				}else if (value.getClass()==String.class) {
					mapColumn(column, DataType.STRING);
				}else if(value.getClass()==int.class||value.getClass()==Integer.class) {
					mapColumn(column, DataType.INTEGER);
				}else if(value.getClass()==Date.class||value.getClass()==java.sql.Date.class||value.getClass()==java.sql.Timestamp.class) {
					mapColumn(column, DataType.DATE);
				}else if(value.getClass()==Long.class||value.getClass()==long.class) {
					mapColumn(column, DataType.LONG);
				}else if(value.getClass()==Double.class||value.getClass()==double.class) {
					mapColumn(column, DataType.DOUBLE);
				}else if(value.getClass().isEnum()) {
					mapColumn(column, DataType.ENUM);
				}else if(value.getClass()==Boolean.class||value.getClass()==boolean.class) {
					mapColumn(column, DataType.BOOLEAN);
				}else {
					mapColumn(column, DataType.OTHER);
					logger.warn("Type "+value.getClass().getName()+" not checked to set in cell value, converting to string");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void mapColumn(Column column, DataType dataType) {
		columnDataTypes.put(column, dataType);
		HSSFCellStyle columnStyle = workbook.createCellStyle();
		columnStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		columnStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		columnStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		columnStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		columnStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		columnStyle.setLeftBorderColor(HSSFColor.GREY_25_PERCENT.index);
		columnStyle.setRightBorderColor(HSSFColor.GREY_25_PERCENT.index);
		columnStyle.setTopBorderColor(HSSFColor.GREY_25_PERCENT.index);
		columnStyle.setBottomBorderColor(HSSFColor.GREY_25_PERCENT.index);
		switch (dataType) {
		case STRING:
			break;
		case DATE:
			columnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			columnStyle.setDataFormat(workbook.createDataFormat().getFormat(column.getFormat()));
			break;
		case DOUBLE:
			columnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			break;
		case INTEGER: case LONG:
			columnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			break;
		default:
			columnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			break;
		}
		columnStyles.put(column, columnStyle);
	}
	
	
	private void buildHeadersGrouped() {
		int maxX, maxY, xM, yM;
		maxX = 0; maxY = 0;
		for (Column column : document.getColumnGroup().getRows().get(0).getColumns()) {
			maxX += column.getColspan()>0? column.getColspan() : 1;
		}
		maxY = document.getColumnGroup().getRows().size()-1;
		int area[][] = new int[maxX+2][maxY+2];
		int x, y;
		x = 0;
		y=0;
		Position position = new Position(x,y);
		logger.trace("maxY: "+maxY+" maxX: "+maxX);
		for (Row row : document.getColumnGroup().getRows()) {
			position = getNextPosition(position.getX(), position.getY(), maxX, maxY, area);
			logger.trace("Creating new row at "+position.getY()+"---------------------------------------");
			for (Column column : row.getColumns()) {
				x = position.getX();
				y = position.getY();
				HSSFRow rowHeader = sheet.getRow(y);
				if (rowHeader==null) {
					rowHeader = sheet.createRow(y);
					rowHeader.setHeight(XLSDocumentDefaults.ROW_HEIGHT);
				}
				HSSFCell cell = rowHeader.getCell(x);
				if (cell==null) cell = rowHeader.createCell(x);		
				cell.setCellType(Cell.CELL_TYPE_STRING);
				logger.trace(rowHeader.getRowNum());
				cell.setCellValue(column.getHeader());
				logger.trace("New cell at "+position+" ----------");
				logger.trace("	Header:"+column.getHeader()+" Colspan:"+column.getColspan()+" Rowspan: "+column.getRowspan());
				xM = column.getColspan()>0? x+column.getColspan()-1 : x;
				yM = column.getRowspan()>0? y+column.getRowspan()-1 : y;
				logger.trace("	Merging from X"+x+"Y"+y+" to X"+xM+"Y"+yM);
				addRangeStyle(x,xM,y,yM);
				if (column.getColspan()>0 || column.getRowspan()>0) {
					CellRangeAddress range = new CellRangeAddress(y,yM,x,xM);
					sheet.addMergedRegion(range);
				}
				fillArea(x,xM,y,yM,area);
				logger.trace("End Cell  ----------");
				x = column.getColspan()>0? xM + 1 : x + 1;
				position = getNextPosition(x, y, maxX, maxY, area);
			}
			logger.trace("End of columns in row");
			position.setX(0);
			position.setY(y+1);
			logger.trace("Start of new row at "+position.toString());
		}
	}
	
	private void addRangeStyle(int x1, int x2, int y1, int y2) {
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				HSSFRow row = sheet.getRow(y);;
				if (row==null) row = sheet.createRow(y);
				Cell cell = row.getCell(x);
				if (cell==null) cell = row.createCell(x);
				cell.setCellStyle(getHeaderStyle());
			}
		}
	}
	
	private HSSFCellStyle getHeaderStyle() {
		if (headerStyle==null) {
			headerStyle = workbook.createCellStyle();
			HSSFFont headerFont = workbook.createFont();
			headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			headerStyle.setFont(headerFont);
			headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			headerStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			headerStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
			headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			headerStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			headerStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			headerStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			headerStyle.setLeftBorderColor(HSSFColor.GREY_40_PERCENT.index);
			headerStyle.setRightBorderColor(HSSFColor.GREY_40_PERCENT.index);
			headerStyle.setTopBorderColor(HSSFColor.GREY_40_PERCENT.index);
			headerStyle.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
		}
		return headerStyle;
	}
	
	private void fillArea(int x1, int x2, int y1, int y2, int[][] area) {
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				logger.trace("	Filled X"+x+"Y"+y);
				area[x][y] = 1;
			}
		}
	}
	
	private void putCellValue(Column column, Cell cell, Object value) {
		HSSFCellStyle style = columnStyles.get(column);
		if (style!=null) cell.setCellStyle(style);
		if (value==null) return;
		logger.trace("Putting value in column "+column.getHeader() );
		switch (columnDataTypes.get(column)) {
		case STRING:
			cell.setCellValue((String) value);
			break;
		case DATE:
			cell.setCellValue((Date) value);
			break;
		case DOUBLE:
			cell.setCellValue((Double) value);
			break;
		case INTEGER:
			cell.setCellValue((Integer) value);
			break;
		case LONG:
			cell.setCellValue((Long) value);
			break;
		case OTHER:
			cell.setCellValue(value.toString());
			break;
		case BOOLEAN:
			cell.setCellValue((Boolean) value);
			break;
		case ENUM:
			cell.setCellValue(value.toString());
			break;
		}
	}
	

	private Position getNextPosition(int x, int y, int maxX, int maxY, int[][] area) {
		for (int xc = x; xc <= maxX; xc++) {
				for (int yc = 0; yc <= maxY; yc++) {
				logger.trace("Checking X"+xc+"Y"+yc);
				if (area[xc][yc]==0) return new Position(xc, yc);
			}
		}
		return new Position(x, y);
	}
}
