package model;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import lotus.domino.*;

/**
 * Common base class for Domino-document-backed objects.
 * 
 * @author Jesse Gallagher
 *
 */
public abstract class AbstractModel extends AbstractViewRowDataModel {
	private static final long serialVersionUID = 1L;

	private String documentId;
	private String databaseServer;
	private String databaseFilePath;
	private int rowIndex;
	
	/**
	 * Constructs a model object from an existing document.
	 * 
	 * @param doc the document to wrap
	 * @throws NotesException if there is a Domino API problem wrapping the document
	 */
	public AbstractModel(Document doc) throws NotesException {
		setDocument(doc);
	}
	
	/**
	 * Constructs a model object for a new document in the provided database.
	 * 
	 * @param database to house the document
	 * @throws NotesException if there is a Domino API problem accessing the database
	 */
	public AbstractModel(Database database) throws NotesException {
		setDatabase(database);
	}
	
	protected abstract boolean doSave(Document doc) throws Exception;
	
	/**
	 * Saves the model object to the backend document.
	 * 
	 * @return <code>true</code> if the save was successful; <code>false</code> otherwise
	 * @throws Exception if there is a problem saving the object
	 */
	public final boolean save() throws Exception {
		Document doc = getDocument();
		if(doSave(doc)) {
			return doc.save();
		} else {
			return false;
		}
	}
	
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	
	// ******************************************************************************
	// * DataModel and ViewRowData methods
	// ******************************************************************************
	
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
		return rowIndex;
	}

	@Override
	public AbstractModel getWrappedData() {
		return this;
	}

	@Override
	public boolean isRowAvailable() {
		return true;
	}

	@Override
	public void setRowIndex(final int rowIndex) {
		this.rowIndex = rowIndex;
	}

	@Override
	public void setWrappedData(final Object obj) {
		// NOP
	}

	public ColumnInfo getColumnInfo(final String columnName) {
		return null;
	}

	public Object getColumnValue(final String columnName) {
		return getProperty(this, columnName);
	}

	public String getOpenPageURL(final String pageName, final boolean readOnly) {
		if(StringUtil.isEmpty(pageName)) {
			return "";
		} else {
			return pageName + (pageName.contains("?") ? "&" : "?") + "documentId=" + getDocumentId();
		}
	}

	public Object getValue(final String columnName) {
		return getProperty(this, columnName);
	}

	public boolean isReadOnly(final String columnName) {
		return false;
	}

	public void setColumnValue(final String columnName, final Object value) {
		setProperty(this, columnName, value);
	}
	
	// ******************************************************************************
	// * Implementation utility methods
	// ******************************************************************************
	
	/**
	 * Returns a {@link Document} instance for this object. This document is
	 * kept in the current request scope.
	 */
	public Document getDocument() throws NotesException {
		Map<String, Object> requestScope = ExtLibUtil.getRequestScope();
		String cacheKey = getClass().getName() + System.identityHashCode(this);
		if(!requestScope.containsKey(cacheKey)) {
			Session session = ExtLibUtil.getCurrentSession();
			Database database = session.getDatabase(this.databaseServer, this.databaseFilePath);
			if(StringUtil.isEmpty(this.documentId)) {
				requestScope.put(cacheKey, database.createDocument());
			} else {
				requestScope.put(cacheKey, database.getDocumentByUNID(this.documentId));
			}
		}
		return (Document)requestScope.get(cacheKey);
	}
	
	// ******************************************************************************
	// * Internal utility methods
	// ******************************************************************************
	
	private void setDocument(Document doc) throws NotesException {
		this.documentId = doc.getUniversalID();
		Database database = doc.getParentDatabase();
		setDatabase(database);
	}
	private void setDatabase(Database database) throws NotesException {
		this.databaseServer = database.getServer();
		this.databaseFilePath = database.getFilePath();
	}
	
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
