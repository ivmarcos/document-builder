package com.ivanechtchuk.document;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

class DocumentParser {
	
	static Logger logger = Logger.getLogger(DocumentParser.class.getName());
	
	private final DocumentWrapper documentData;
	private DocumentExpressionResolver reflection;
	private Document document;
	private String[] headers;
	private Map<Column, String> expressionsMap;
	private Map<Column, String[]> multipleExpressionsMap;
	private List<String[]> dataString;
	
	public DocumentParser(DocumentWrapper documentData) {
		this.documentData = documentData;
		document = documentData.getDocument();
	}
	
	public List<String[]> documentDataToString() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (dataString==null) {
			dataString = new ArrayList<String[]>();
			int size = document.getColumns().size();
			List<String> expressions = new ArrayList<String>(getExpressionsMap().values());
			Collections.reverse(expressions);
			for (Object instance : documentData.getValues()) {
				int i = 0;
				String[] value = new String[size];
				for (String expression : expressions) {
					Object result = reflection.resolveExpression(instance, expression);
					if (result==null) result = "";
					value[i++] = result.toString();
				}
				dataString.add(value);
			}
		}
		return dataString;
	}
	
	public String[] getDocumentHeaders() {
		if (headers==null) {
			headers = new String[document.getColumns().size()];
			int i = 0;
			for (Column column : document.getColumns()) {
				headers[i++] = column.getHeader();
			}
		}
		return headers;
	}
	
	public String getColumnExpression(Column column) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (expressionsMap==null) mapExpressions();
		return expressionsMap.get(column);
	}
	
	public String[] getColumnExpressions(Column column) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (multipleExpressionsMap==null) parseMultipleExpressions();
		return multipleExpressionsMap.get(column);
	}
	
	private void mapExpressions() {
		expressionsMap = new HashMap<Column, String>();
		for (Column column : document.getColumns()) {
			String expression = new String(column.getValue());
			if (expression.startsWith("#{"+document.getVar()+".")) {
				expression = expression.replaceFirst(document.getVar()+".", "");
			}
			expression =  expression.replaceAll("(\\})|(\\{)|#", "");
			expressionsMap.put(column, expression);
		}
	}
	
	private void parseMultipleExpressions() {
		multipleExpressionsMap = new HashMap<Column, String[]>();
		for (Column column : document.getColumns()) {
			String columnExpression = new String(column.getValue());
			List<String> expressionsList = new ArrayList<String>();
			for (String expression : columnExpression.split("[#]")){
				for (String ex : expression.split("\\}")) {
					if (!ex.trim().isEmpty()) {
						expressionsList.add(ex.indexOf("{")>-1? ex+"}" : ex);
					}
				}
			}
			multipleExpressionsMap.put(column, expressionsList.toArray(new String[] {}));
		}
	}

	private Map<Column, String> getExpressionsMap() {
		if (expressionsMap==null) mapExpressions();
		return expressionsMap;
	}


	
}
