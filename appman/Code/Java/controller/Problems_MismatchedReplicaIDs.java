package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.DataModelTabularListAdapter;

import frostillicus.controller.BasicXPageController;

import lotus.domino.*;
import model.AbstractViewRowDataModel;
import model.CategoryModel;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class Problems_MismatchedReplicaIDs extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public DataModelTabularListAdapter getApplications() throws NotesException {
		List<AbstractViewRowDataModel> result = new ArrayList<AbstractViewRowDataModel>();
		
		Database database = ExtLibUtil.getCurrentDatabase();
		View view = database.getView("Problems\\Mismatched Replica IDs");
		view.setAutoUpdate(false);
		ViewNavigator nav = view.createViewNav();
		nav.setCacheGuidance(400);
		ViewEntry categoryEntry = nav.getFirst();
		while(categoryEntry != null) {
			if(categoryEntry.isCategory() && categoryEntry.getChildCount() > 1) {
				categoryEntry.setPreferJavaDates(true);
				List<?> columnValues = categoryEntry.getColumnValues();
				String filePath = StringUtil.toString(columnValues.get(0));
				Map<String, Object> vals = new HashMap<String, Object>();
				vals.put("filePath", filePath);
				result.add(new CategoryModel(vals));
				
				ViewEntry databaseEntry = nav.getChild(categoryEntry);
				while(databaseEntry != null) {
					Document databaseDoc = databaseEntry.getDocument();
					model.Application app = new model.Application(databaseDoc);
					result.add(app);
					databaseEntry = nav.getNextSibling(databaseEntry);
				}
			}
			categoryEntry = nav.getNextSibling(categoryEntry);
		}
		
		return new DataModelTabularListAdapter(result, "documentId", Arrays.asList("filePath"));
	}
}
