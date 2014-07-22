package com.ivanechtchuk.document;

class DocumentExpressionException extends RuntimeException{

	private String message;

	public DocumentExpressionException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
