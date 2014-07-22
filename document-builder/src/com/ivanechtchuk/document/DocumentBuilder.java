package com.ivanechtchuk.document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;


public class DocumentBuilder {
	
	static Logger logger = Logger.getLogger(DocumentBuilder.class.getName());
	
	private DocumentWrapper documentData;
	
	public DocumentTitleBuilder xml(String xml) {
		InputStream stream = new ByteArrayInputStream(xml.getBytes());
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser = factory.newSAXParser();
			DocumentXMLHandler handler = new DocumentXMLHandler();
			parser.parse(stream, handler);
			Document document = handler.getDocumentsMap().values().toArray(new Document[] {})[0];
			System.out.println(document.getName());
			documentData = new DocumentWrapper();
			documentData.setDocument(document);
			return new DocumentTitleBuilder(documentData);
		}catch (Exception e) {
			e.printStackTrace();
			throw new DocumentBuilderException(e.getMessage());
		}
	}

	public DocumentTitleBuilder name(String name) {
		Document document = DocumentRepository.getDocument(name);
		if (document==null) throw new DocumentBuilderException("No documents found by name "+name);
		documentData = new DocumentWrapper();
		documentData.setDocument(document);
		return new DocumentTitleBuilder(documentData);
	}
	
	public DocumentBuilder clearRepository() {
		DocumentRepository.clear();
		return this;
	}
	
	public class DocumentTitleBuilder{
		DocumentWrapper documentData;
		public DocumentTitleBuilder(DocumentWrapper documentData){
			this.documentData = documentData;
		}
		public DocumentDataBuilder title(String title){
			documentData.setTitle(title.replace(" ", "-"));
			return new DocumentDataBuilder(documentData);
		}
	}
	
	public class DocumentDataBuilder{
		DocumentWrapper documentData;
		public DocumentDataBuilder(DocumentWrapper documentData){
			this.documentData = documentData;
		}
		public DocumentTypeBuilder data(Collection<?> data) {
			documentData.setValues(data);
			return new DocumentTypeBuilder(documentData);
		}
	}
	
	public class DocumentTypeBuilder{
		
		DocumentWrapper documentData;
		public DocumentTypeBuilder(DocumentWrapper documentData) {
			this.documentData = documentData;
		}
		
		public void csv() {
			DocumentExporter exporter = new CSVDocumentExporter();
			exporter.export(documentData);
		}
		public void xls() {
			DocumentExporter exporter = new XLSDocumentExporter();
			exporter.export(documentData);
		}
	}
}
