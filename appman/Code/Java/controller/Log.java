package controller;

import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;

import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import frostillicus.JSFUtil;
import frostillicus.controller.BasicXPageController;

public class Log extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public String getLogBody() throws NotesException {
		DominoDocument doc = JSFUtil.getVariableValue("doc");
		boolean isConvertMime = JSFUtil.getSession().isConvertMime();
		try {
			JSFUtil.getSession().setConvertMime(false);
			Document lotusDoc = doc.getDocument();
			MIMEEntity logBody = lotusDoc.getMIMEEntity("Log");
			if(logBody != null) {
				return logBody.getContentAsText();
			} else {
				return "(no log text available)";
			}
		} finally {
			JSFUtil.getSession().setConvertMime(isConvertMime);
		}
	}
}
