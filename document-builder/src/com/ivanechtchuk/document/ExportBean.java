package com.ivanechtchuk.document;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import au.com.bytecode.opencsv.CSVWriter;

@RequestScoped
public class ExportBean {
	
	private static final String ENCODING = "ISO-8859-1";
	
	private String getCorrectFileName(String fileName) {
		return fileName.replace(" ", "_").replace("/", "-");
	}

	private String getDataFileName() {
		return new SimpleDateFormat("dd'.'MM'.'yyyy_HH'h'mm").format(Calendar.getInstance().getTime());
	}
	public void  csv(String fileName, String[] headers, List<String[]> values)  {
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.addHeader("Content-disposition", "attachment; filename="+getCorrectFileName(fileName+"_"+getDataFileName())+".csv");
		ServletOutputStream out;
		try {
			out = response.getOutputStream();
			BufferedWriter bfWriter = new BufferedWriter(new OutputStreamWriter(out, ENCODING));
			CSVWriter writer = new CSVWriter(bfWriter, ';');
			writer.writeNext(headers);
			writer.writeAll(values);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	public void xls(String fileName, String[] headers, List<String[]> values)  {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(fileName);
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.addHeader("Content-disposition", "attachment; filename="+getCorrectFileName(fileName+"_"+getDataFileName())+".xls");
		ServletOutputStream out;
		try {
			out = response.getOutputStream();
			int line = 0;
			HSSFRow rowHeader = sheet.createRow(line);
			int columnHeader = 0;
			for (String value : headers) {
				rowHeader.createCell(columnHeader).setCellValue(value);
				columnHeader++;
			}
			line++;
			for (String[] columns : values) {
				HSSFRow row = sheet.createRow(line);
				int column = 0;
				for (String value : columns) {
					row.createCell(column).setCellValue(value);
					column++;
				}
				line++;
			}
			workbook.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	public void xlsx(String fileName, String[] headers, List<String[]> values) {
		
	}
}
