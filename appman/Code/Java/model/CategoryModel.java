package model;

import java.util.Map;

public class CategoryModel extends AbstractViewRowDataModel {
	private static final long serialVersionUID = 1L;
	
	private Map<String, Object> props;
	private int index;
	
	public CategoryModel(Map<String, Object> props) {
		this.props = props;
		System.out.println("Constructed category for " + props);
	}

	@Override
	public int getRowCount() {
		return 1;
	}

	@Override
	public CategoryModel getRowData() {
		return this;
	}

	@Override
	public int getRowIndex() {
		return index;
	}

	@Override
	public CategoryModel getWrappedData() {
		return this;
	}

	@Override
	public boolean isRowAvailable() {
		return true;
	}

	@Override
	public void setRowIndex(final int index) {
		this.index = index;
	}

	@Override
	public void setWrappedData(final Object obj) {
		// NOP
	}

	public ColumnInfo getColumnInfo(final String columnName) {
		return null;
	}

	public Object getColumnValue(final String columnName) {
		System.out.println("getColumnValue for " + columnName + " = " + props.get(columnName));
		return props.get(columnName);
	}

	public String getOpenPageURL(final String pageName, final boolean readOnly) {
		return "";
	}

	public Object getValue(final String columnName) {
		System.out.println("getValue for " + columnName + " = " + props.get(columnName));
		return props.get(columnName);
	}

	public boolean isReadOnly(final String columnName) {
		return false;
	}

	public void setColumnValue(final String columnName, final Object value) {
		props.put(columnName, value);
	}
}
