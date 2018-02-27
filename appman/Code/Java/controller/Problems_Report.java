package controller;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.controller.BasicXPageController;
import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

public class Problems_Report extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public String getReport() throws NotesException {
		Database database = ExtLibUtil.getCurrentDatabase();
		Document doc = database.createDocument();
		Agent agent = database.getAgent("Run HTMLGenerator");
		agent.runWithDocumentContext(doc);
		
		return doc.getItemValueString("Report");
	}
}
