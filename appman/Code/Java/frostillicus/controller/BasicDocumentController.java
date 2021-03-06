package frostillicus.controller;

import frostillicus.JSFUtil;

import javax.faces.context.FacesContext;
import lotus.domino.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import java.util.*;

public class BasicDocumentController extends BasicXPageController implements DocumentController {
	private static final long serialVersionUID = 1L;

	public void queryNewDocument() throws Exception { }
	public void postNewDocument() throws Exception { }
	public void queryOpenDocument() throws Exception { }
	public void postOpenDocument() throws Exception {
		DominoDocument doc = this.getDoc();
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		viewScope.put("$REF", doc.getValue("$REF"));
	}
	public void querySaveDocument() throws Exception { }
	public void postSaveDocument() throws Exception { }

	public String save() throws Exception {
		DominoDocument doc = this.getDoc();

		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		viewScope.put("$REF", doc.getValue("$REF"));

		boolean isNewNote = doc.isNewNote();
		if(doc.save()) {
			Database database = doc.getParentDatabase();
			if(database.isFTIndexed()) {
				database.updateFTIndex(false);
			}
			JSFUtil.addMessage("confirmation", doc.getValue("Form") + " " + (isNewNote ? "created" : "updated") + " successfully.");
			return "xsp-success";
		} else {
			JSFUtil.addMessage("error", "Save failed");
			return "xsp-failure";
		}
	}
	public String cancel() throws Exception {
		return "xsp-cancel";
	}
	public String delete() throws Exception {
		DominoDocument doc = this.getDoc();

		String formName = (String)doc.getValue("Form");
		doc.getDocument(true).remove(true);
		JSFUtil.addMessage("confirmation", formName + " deleted.");
		return "xsp-success";
	}

	public String getDocumentId() {
		try {
			return this.getDoc().getDocument().getUniversalID();
		} catch(Exception e) { return ""; }
	}

	@Override
	public boolean isEditable() { return this.getDoc().isEditable(); }

	public boolean isEditableBy(String userName) throws NotesException {
		Document doc = this.getDoc().getDocument();
		return JSFUtil.isDocEditableBy(doc, userName);
	}
	public boolean isUserEditable() throws NotesException {
		return this.isEditableBy(ExtLibUtil.getCurrentSession().getEffectiveUserName());
	}

	protected DominoDocument getDoc() {
		return (DominoDocument)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "doc");
	}
}
