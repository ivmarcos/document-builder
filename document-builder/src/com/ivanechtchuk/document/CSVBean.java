package com.ivanechtchuk.document;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import br.com.bb.psi.query.Generic;
import br.com.bb.psi.query.QueryConverter;
import au.com.bytecode.opencsv.CSVWriter;

@RequestScoped
public class CSVBean {

	public void  export(String fileName, String[] headers, List<String[]> values)  {
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.addHeader("Content-disposition", "attachment; filename="+fileName+".csv");
		ServletOutputStream out;
		try {
			out = response.getOutputStream();
			BufferedWriter bfWriter = new BufferedWriter(new OutputStreamWriter(out));
			CSVWriter writer = new CSVWriter(bfWriter, ';');
			writer.writeNext(headers);
			writer.writeAll(values);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	
	
}
