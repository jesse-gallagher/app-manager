package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import util.BeanTabularListAdapter;

import frostillicus.controller.BasicXPageController;

import lotus.domino.*;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class Problems_MismatchedReplicaIDs extends BasicXPageController {
	private static final long serialVersionUID = 1L;
	
	private static final TreeSet<String> KNOWN_SYSTEM_PATHS;
	static {
		KNOWN_SYSTEM_PATHS = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		KNOWN_SYSTEM_PATHS.addAll(Arrays.asList(
			"certsrv.nsf",
			"cppfbws.nsf",
			"daoscat.nsf",
			"dbdirman.nsf",
			"doladmin.nsf",
			"iNotes/Forms9.nsf",
			"log.nsf",
			"reports.nsf",
			"schema.nsf",
			"webadmin.nsf"
		));
	}

	public BeanTabularListAdapter<model.Application> getApplications() throws NotesException {
		List<model.Application> result = new ArrayList<model.Application>();
		
		Database database = ExtLibUtil.getCurrentDatabase();
		View view = database.getView("Problems\\Mismatched Replica IDs");
		view.setAutoUpdate(false);
		ViewNavigator nav = view.createViewNav();
		nav.setCacheGuidance(400);
		ViewEntry categoryEntry = nav.getFirst();
		while(categoryEntry != null) {
			if(categoryEntry.isCategory() && categoryEntry.getChildCount() > 1) {
				List<?> columnValues = categoryEntry.getColumnValues();
				String filePath = StringUtil.toString(columnValues.get(0));
				if(!KNOWN_SYSTEM_PATHS.contains(filePath)) {
					ViewEntry databaseEntry = nav.getChild(categoryEntry);
					while(databaseEntry != null) {
						Document databaseDoc = databaseEntry.getDocument();
						model.Application app = new model.Application(databaseDoc);
						result.add(app);
						databaseEntry = nav.getNextSibling(databaseEntry);
					}
				}
			}
			categoryEntry = nav.getNextSibling(categoryEntry);
		}
		
		return new BeanTabularListAdapter<model.Application>(result, "documentId");
	}
}
