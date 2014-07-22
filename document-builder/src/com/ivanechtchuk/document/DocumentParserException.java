package com.ivanechtchuk.document;

class DocumentParserException extends RuntimeException{

	private String message;

	public DocumentParserException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
