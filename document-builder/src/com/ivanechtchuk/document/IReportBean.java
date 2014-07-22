package com.ivanechtchuk.document;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import org.apache.log4j.Logger;

import br.com.bb.psi.annotation.Conecta;
import br.com.bb.psi.constants.Conexao;
import br.com.bb.psi.exception.ConnectionURLException;

@RequestScoped
public class IReportBean implements Serializable {
	
	static Logger logger = Logger.getLogger(IReportBean.class.getName());
	
	@Inject @Conecta
	Connection connection;

	private static final String DEFAULT_CONNECTION = Conexao.GLOBAL;
	private static final long serialVersionUID = 1L;

	public enum Type{
		PDF, DOCX, PPT, XLSX, ODT, XLS
	}

	private JasperPrint jasperPrint;
	private boolean viewInPage;
	private String reportTitle;
	private String reportPath;
	private String connectionURL = DEFAULT_CONNECTION;
	private Type type = Type.PDF;
	private Collection<?> list;
	private JRBeanCollectionDataSource dataSource;
	private Map<String, Object> parameters = new HashMap<String, Object>();
	
	public IReportBean() {
		parameters.put("REPORT_LOCALE", new Locale("pt", "BR"));
	}
	
	public IReportBean title(String reportTitle) {
		this.reportTitle = reportTitle.replace(" ", "_").replace("/", "-");
		return this;
	}
	
	public IReportBean type(Type type) {
		this.type = type;
		return this;
	}
	
	public IReportBean connectionURL(String url) {
		this.connectionURL = url;
		return this;
	}
	
	public IReportBean collection(Collection<?> list) {
		this.list = list;
		this.dataSource = new JRBeanCollectionDataSource(list);
		return this;
	}

	public IReportBean report(String reportPath) {
		parameters.put("SUBREPORT_DIR", getRealFolder(reportPath));
		this.reportPath = getRealPath(reportPath);
		return this;
	}
	
	public IReportBean parameters(Map<String, Object> parameters) {
		this.parameters.putAll(parameters);
		return this;
	}
	
	public IReportBean param(String key, Object value) {
		this.parameters.put(key, value);
		return this;
	}
	
	public IReportBean image(String name, String path) {
		parameters.put(name, getFile(path));
		return this;
	}
	
	public void view() {
		viewInPage = true;
		create();
	}
	
	public void create() throws ConnectionURLException {
		try {
			if (dataSource==null){
				jasperPrint = JasperFillManager.fillReport(reportPath, parameters, connection);
			}else {
				jasperPrint = JasperFillManager.fillReport(reportPath, parameters, dataSource);
			}
			onType(type);
		} catch (JRException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void print(Type type, String reportPath, Map<String, Object> parameters, Collection<Object> collection) {
		JRBeanCollectionDataSource beanCollectionDataSource=new JRBeanCollectionDataSource(collection);
		reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(reportPath);        
		try {
			jasperPrint = JasperFillManager.fillReport(reportPath, parameters, beanCollectionDataSource);
			JasperPrintManager.printPage(jasperPrint, 0, true);
		} catch (JRException e) {
			e.printStackTrace();
		}
	}

	public void print(Type type, String reportPath, Map<String, Object> parameters, Connection connection) {
		reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(reportPath);        
		try {
			jasperPrint = JasperFillManager.fillReport(reportPath, parameters, connection);
			JasperPrintManager.printPage(jasperPrint, 0, true);
		} catch (JRException e) {
			e.printStackTrace();
		}
	}
	
	public void print(Type type, String reportPath, Map<String, Object> parameters) {
		reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(reportPath);        
		try {
			jasperPrint = JasperFillManager.fillReport(reportPath, parameters);
			JasperPrintManager.printPage(jasperPrint, 0, true);
		} catch (JRException e) {
			e.printStackTrace();
		}
	}

	public void view(Type type, String reportPath, Map<String, Object> parameters, Connection connection) {
		reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(reportPath);        
		try {
			jasperPrint = JasperFillManager.fillReport(reportPath, parameters, connection);
		} catch (JRException e) {
			e.printStackTrace();
		}
	}
	
	private void onType(Type type) throws JRException, IOException {
		switch (type) {
		case PDF:
			PDF();
			break;
		case DOCX:
			DOCX();
			break;
		case PPT:
			PPT();
			break;
		case XLSX:
			XLSX();
			break;
		case ODT:
			ODT();
			break;	
		case XLS:
			XLS();
			break;
		}	
	}
	
	protected void PDF() throws JRException, IOException{
		HttpServletResponse response=(HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();  
		response.addHeader("Content-disposition", "attachment; filename="+reportTitle+".pdf");  
		if(!viewInPage) {
			ServletOutputStream ouputStream=response.getOutputStream();  
			JasperExportManager.exportReportToPdfStream(jasperPrint, ouputStream);
		}else {
			byte[] bytes = null;  
			bytes =  JasperExportManager.exportReportToPdf(jasperPrint);
			response.reset();
			response.setContentType("application/pdf");  
			response.setContentLength(bytes.length);  
			ServletOutputStream ouputStream = response.getOutputStream();  
			ouputStream.write(bytes, 0, bytes.length);  
		}
		FacesContext.getCurrentInstance().responseComplete();
	}

	protected void DOCX() throws JRException, IOException{
		HttpServletResponse httpServletResponse=(HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
		httpServletResponse.addHeader("Content-disposition", "attachment; filename="+reportTitle+".docx");
		ServletOutputStream servletOutputStream=httpServletResponse.getOutputStream();
		JRDocxExporter docxExporter=new JRDocxExporter();
		docxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		docxExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, servletOutputStream);
		docxExporter.exportReport();
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	protected void XLSX() throws JRException, IOException{
		HttpServletResponse httpServletResponse=(HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
		httpServletResponse.addHeader("Content-disposition", "attachment; filename="+reportTitle+".xlsx");
		ServletOutputStream servletOutputStream=httpServletResponse.getOutputStream();
		JRXlsxExporter docxExporter=new JRXlsxExporter();
		docxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		docxExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, servletOutputStream);
		docxExporter.exportReport();
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	protected void XLS() throws JRException, IOException{
		HttpServletResponse httpServletResponse=(HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
		httpServletResponse.addHeader("Content-disposition", "attachment; filename="+reportTitle+".xlsx");
		ServletOutputStream servletOutputStream=httpServletResponse.getOutputStream();
		JRXlsExporter exporter = new JRXlsExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, servletOutputStream);
		exporter.exportReport();
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	protected void ODT() throws JRException, IOException{
		HttpServletResponse httpServletResponse=(HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
		httpServletResponse.addHeader("Content-disposition", "attachment; filename="+reportTitle+".odt");
		ServletOutputStream servletOutputStream=httpServletResponse.getOutputStream();
		JROdtExporter docxExporter=new JROdtExporter();
		docxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		docxExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, servletOutputStream);
		docxExporter.exportReport();
		FacesContext.getCurrentInstance().responseComplete();
	}

	protected void PPT() throws JRException, IOException{
		HttpServletResponse httpServletResponse=(HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
		httpServletResponse.addHeader("Content-disposition", "attachment; filename="+reportTitle+".pptx");
		ServletOutputStream servletOutputStream=httpServletResponse.getOutputStream();
		JRPptxExporter docxExporter=new JRPptxExporter();
		docxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		docxExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, servletOutputStream);
		docxExporter.exportReport();
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	private File getFile(String path) {
		return new File(FacesContext.getCurrentInstance().getExternalContext().getRealPath(path));
	}
	
	private String getRealFolder(String path) {
		return getFile(path).getParent()+"/";
	}
	
	private String getRealPath(String path) {
		return FacesContext.getCurrentInstance().getExternalContext().getRealPath(path);
	}
	

}
