package controller;

import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import frostillicus.controller.BasicXPageController;

public class Log extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public String getLogBody() throws NotesException {
		DominoDocument doc = (DominoDocument)ExtLibUtil.resolveVariable("doc");
		boolean isConvertMime = ExtLibUtil.getCurrentSession().isConvertMime();
		try {
			ExtLibUtil.getCurrentSession().setConvertMime(false);
			Document lotusDoc = doc.getDocument();
			MIMEEntity logBody = lotusDoc.getMIMEEntity("Log");
			if(logBody != null) {
				return logBody.getContentAsText();
			} else {
				return "(no log text available)";
			}
		} finally {
			ExtLibUtil.getCurrentSession().setConvertMime(isConvertMime);
		}
	}
}
