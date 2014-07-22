package com.ivanechtchuk.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class DocumentXMLHandler extends DefaultHandler{
	
	static Logger logger = Logger.getLogger(DocumentXMLHandler.class.getName());
	
	private Map<String, Document> documentsMap;
	Document document = null;
	Column column = null;
	ColumnGroup columnGroup = null;
	Row row = null;
	boolean group;
	int id = 0;
		
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		logger.trace("start of "+qName);
		switch (qName) {
		case "documents":
			documentsMap = new HashMap<String, Document>();
			break;
		case "document":
			if (documentsMap==null) documentsMap = new HashMap<String, Document>();
			document = new Document();
			document.setName(attributes.getValue("name"));
			document.setValue(attributes.getValue("value"));
			document.setVar(attributes.getValue("var"));
			break;
		case "columngroup":
			columnGroup = new ColumnGroup();
			group = true;
			break;
		case "row":
			logger.trace("Adding new row");
			row = new Row();
			break;
		case "column":
			column = new Column();
			column.setId(id++);
			column.setHeader(attributes.getValue("header"));
			column.setValue(attributes.getValue("value"));
			column.setFormat(attributes.getValue("format"));
			if (attributes.getValue("colspan")!=null) column.setColspan(Integer.valueOf(attributes.getValue("colspan")));
			if (attributes.getValue("rowspan")!=null) column.setRowspan(Integer.valueOf(attributes.getValue("rowspan")));
			break;
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		logger.trace("end of "+qName);
		switch (qName) {
		case "document":
			documentsMap.put(document.getName(), document);
			break;
		case "column":
			logger.trace("adding columns...");
			if (group) {
				if (row.getColumns()==null) {
					row.setColumns(new ArrayList<Column>());
				}
				row.getColumns().add(column);
				logger.trace("adding to row group");
			}else {
				if (document.getColumns()==null) {
					document.setColumns(new ArrayList<Column>());
				}
				document.getColumns().add(column);
				logger.trace("adding to document");
			}
			break;
		case "row":
			if (document.getColumnGroup()==null) {
				document.setColumnGroup(columnGroup);
			}
			if (document.getColumnGroup().getRows()==null) {
				document.getColumnGroup().setRows(new ArrayList<Row>());
			}
			document.getColumnGroup().getRows().add(row);
			break;
		case "columngroup":
			group = false;
			break;
		}
	}
	
	public void characters(char[] ch, int start, int length) {
	}

	public Map<String, Document> getDocumentsMap() {
		return documentsMap;
	}

}
