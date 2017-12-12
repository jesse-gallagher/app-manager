package controller;

import net.cmssite.endeavour60.util.EndeavourUtil;
import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;

import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import frostillicus.xsp.controller.BasicXPageController;

public class Log extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public String getLogBody() throws NotesException {
		DominoDocument doc = EndeavourUtil.resolveVariable("doc");
		boolean isConvertMime = EndeavourUtil.getSession().isConvertMime();
		try {
			EndeavourUtil.getSession().setConvertMime(false);
			Document lotusDoc = doc.getDocument();
			MIMEEntity logBody = lotusDoc.getMIMEEntity("Log");
			if(logBody != null) {
				return logBody.getContentAsText();
			} else {
				return "(no log text available)";
			}
		} finally {
			EndeavourUtil.getSession().setConvertMime(isConvertMime);
		}
	}
}
