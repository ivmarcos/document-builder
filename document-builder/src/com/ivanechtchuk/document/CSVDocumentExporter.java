package com.ivanechtchuk.document;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import br.com.bb.psi.util.Reflections;

public class CSVDocumentExporter implements DocumentExporter{

	static Logger logger = Logger.getLogger(CSVDocumentExporter.class.getName());
	//TESTE
	
	@Override
	public void export(DocumentWrapper documentData) {
		logger.info("Exporting csv...");
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.addHeader("Content-disposition", "attachment; filename="+documentData.getTitle()+".csv");
		ServletOutputStream out;
		try {
			out = response.getOutputStream();
			BufferedWriter bfWriter = new BufferedWriter(new OutputStreamWriter(out));
			CSVWriter writer = new CSVWriter(bfWriter, ';');
			DocumentParser dr = new DocumentParser(documentData);
			writer.writeNext(dr.getDocumentHeaders());
			writer.writeAll(dr.documentDataToString());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	
}
