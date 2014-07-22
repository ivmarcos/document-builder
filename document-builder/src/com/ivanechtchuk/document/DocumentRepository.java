package com.ivanechtchuk.document;

import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


class DocumentRepository {
	
	private static Map<String, Document> documentsMap;
	private static final String XML_DOCUMENT = "documents.xml";
	
	private static void load() {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			DocumentXMLHandler handler = new DocumentXMLHandler();
			InputStream stream = DocumentRepository.class.getClassLoader().getResourceAsStream(XML_DOCUMENT);
			parser.parse(stream, handler);
			documentsMap = handler.getDocumentsMap();
		}catch(Exception e ) {
			e.printStackTrace();
		}
	}
	
	public static void clear() {
		documentsMap=null;
	}

	public static Document getDocument(String name) {
		if (documentsMap==null) load();
		return documentsMap.get(name);
	}
	

}
