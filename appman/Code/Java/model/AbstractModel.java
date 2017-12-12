package model;

import java.io.Serializable;
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
public abstract class AbstractModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private String documentId;
	private String databaseServer;
	private String databaseFilePath;
	
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
	
	protected abstract boolean doSave() throws Exception;
	
	/**
	 * Saves the model object to the backend document.
	 * 
	 * @return <code>true</code> if the save was successful; <code>false</code> otherwise
	 * @throws Exception if there is a problem saving the object
	 */
	public boolean save() throws Exception {
		if(doSave()) {
			Document doc = getDocument();
			return doc.save();
		} else {
			return false;
		}
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
}
