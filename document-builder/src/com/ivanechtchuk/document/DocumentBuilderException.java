package com.ivanechtchuk.document;

class DocumentBuilderException extends RuntimeException{

	private String message;

	public DocumentBuilderException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
