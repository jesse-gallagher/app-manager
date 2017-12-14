package util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.model.TabularDataModel;
import com.ibm.xsp.model.ViewRowData;


/**
 * This class wraps a provided {@link List} of arbitrary Java beans to provide an appropriate
 * interface for data views, view panels, etc.
 * 
 * @author Jesse Gallagher
 * @since 1.6.0
 */
public class BeanTabularListAdapter<T> extends TabularDataModel implements Serializable {
	public class BeanDataModelAdapter extends DataModel implements Serializable, ViewRowData {
		private static final long serialVersionUID = 1L;
		private T obj;
		private int index;

		public BeanDataModelAdapter(final T obj, final int index) {
			this.obj = obj;
			this.index = index;
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public Object getRowData() {
			return this;
		}

		@Override
		public int getRowIndex() {
			return index;
		}

		@Override
		public T getWrappedData() {
			return obj;
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
			return getProperty(obj, columnName);
		}

		public String getOpenPageURL(final String pageName, final boolean readOnly) {
			if(StringUtil.isEmpty(pageName)) {
				return "";
			} else {
				return pageName + (pageName.contains("?") ? "&" : "?") + "documentId=" + getProperty(obj, documentIdProp);
			}
		}

		public Object getValue(final String columnName) {
			return getProperty(obj, columnName);
		}

		public boolean isReadOnly(final String columnName) {
			return false;
		}

		public void setColumnValue(final String columnName, final Object value) {
			setProperty(obj, columnName, value);
		}
	}

	private static final long serialVersionUID = 1L;

	private List<BeanDataModelAdapter> list;
	private Set<String> selectedIds = new HashSet<String>();
	private final String documentIdProp;

	public BeanTabularListAdapter(final List<T> list, String documentIdProp) {
		this.list = new ArrayList<BeanDataModelAdapter>(list.size());
		for(int i = 0; i < list.size(); i++) {
			this.list.add(new BeanDataModelAdapter(list.get(i), i));
		}
		this.documentIdProp = documentIdProp;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public BeanDataModelAdapter getRowData() {
		return list.get(getRowIndex());
	}

	@Override
	public void addSelectedId(final String id) {
		this.selectedIds.add(id);
	}

	@Override
	public void removeSelectedId(final String id) {
		this.selectedIds.remove(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator getSelectedIds() {
		return selectedIds.iterator();
	}

	@Override
	public void clearSelectedIds() {
		this.selectedIds.clear();
	}

	@Override
	public boolean isSelectedId(final String id) {
		return selectedIds.contains(id);
	}

	public List<T> getSelectedObjects() {
		List<T> result = new ArrayList<T>(this.selectedIds.size());
		for(BeanDataModelAdapter adapter : list) {
			T obj = adapter.getWrappedData();
			if(selectedIds.contains(getProperty(obj, documentIdProp))) {
				result.add(obj);
			}
		}
		return result;
	}

	@Override
	public String getRowId() {
		int rowIndex = getRowIndex();
		if(rowIndex >= 0) {
			BeanDataModelAdapter adapter = list.get(rowIndex);
			return getProperty(adapter.getWrappedData(), documentIdProp);
		} else {
			return null;
		}
	}
	
	// ******************************************************************************
	// * Internal utility methods
	// ******************************************************************************
	
	@SuppressWarnings("unchecked")
	private static <T> T getProperty(Object bean, String name) {
		try {
			PropertyDescriptor propDesc = new PropertyDescriptor(name, bean.getClass());
			Method getter = propDesc.getReadMethod();
			Object val = getter.invoke(bean);
			return (T)val;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void setProperty(Object bean, String name, Object value) {
		try {
			PropertyDescriptor propDesc = new PropertyDescriptor(name, bean.getClass());
			Method setter = propDesc.getWriteMethod();
			setter.invoke(bean, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}
}