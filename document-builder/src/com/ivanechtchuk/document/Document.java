package com.ivanechtchuk.document;

import java.util.List;

public class Document {

	private String name;
	private String value;
	private String var;
	private List<Column> columns;
	private ColumnGroup columnGroup;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public List<Column> getColumns() {
		return columns;
	}
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}
	public String getVar() {
		return var;
	}
	public void setVar(String var) {
		this.var = var;
	}
	public ColumnGroup getColumnGroup() {
		return columnGroup;
	}
	public void setColumnGroup(ColumnGroup columnGroup) {
		this.columnGroup = columnGroup;
	}
	
	public boolean hasGroup() {
		return columnGroup!=null;
	}

}
