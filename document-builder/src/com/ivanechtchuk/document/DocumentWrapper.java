package com.ivanechtchuk.document;

import java.util.Collection;

class DocumentWrapper {

	private Document document;
	private Collection<?> values;
	private String title;
	
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Collection<?> getValues() {
		return values;
	}

	public void setValues(Collection<?> values) {
		this.values = values;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	
	
}
