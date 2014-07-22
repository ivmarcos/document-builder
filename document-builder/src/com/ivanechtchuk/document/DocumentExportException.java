package com.ivanechtchuk.document;

class DocumentExportException extends RuntimeException{

	private String message;

	public DocumentExportException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
