package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;
import model.AbstractViewRowDataModel;

import util.DataModelTabularListAdapter;

import frostillicus.controller.BasicXPageController;

public class Problems_DocumentCounts extends BasicXPageController {
	private static final long serialVersionUID = 1L;
	
	public static final double DIVERGENCE = 0.02;

	public DataModelTabularListAdapter getApplications() throws NotesException {
		List<AbstractViewRowDataModel> result = new ArrayList<AbstractViewRowDataModel>();
		
		Database database = ExtLibUtil.getCurrentDatabase();
		View view = database.getView("Problems\\Divergent Document Counts");
		view.setAutoUpdate(false);
		ViewNavigator nav = view.createViewNav();
		nav.setCacheGuidance(400);
		ViewEntry categoryEntry = nav.getFirst();
		while(categoryEntry != null) {
			if(categoryEntry.isCategory() && categoryEntry.getChildCount() > 1) {
				// Then check to see if the document-count values are within a couple percent
				
				List<String> unids = new ArrayList<String>(); // House the UNIDs while checking
				List<Double> counts = new ArrayList<Double>();
				ViewEntry databaseEntry = nav.getChild(categoryEntry);
				while(databaseEntry != null) {
					unids.add(databaseEntry.getUniversalID());
					
					List<?> columnValues = databaseEntry.getColumnValues();
					Double count = (Double)columnValues.get(1);
					counts.add(count);
					
					databaseEntry = nav.getNextSibling(databaseEntry);
				}
				
				// Look through the counts to find if any diverge by more than the limit
				boolean diverge = false;
				for(int i = 0; i < counts.size()-1; i++) {
					double count = counts.get(i);
					for(int j = i; j < counts.size(); j++) {
						double compareCount = counts.get(j);
						double pct = count / compareCount;
						double diff = Math.abs(pct - 1.0f);
						if(diff >= DIVERGENCE) {
							diverge = true;
							break;
						}
					}
				}
				if(diverge) {
					for(String unid : unids) {
						Document doc = database.getDocumentByUNID(unid);
						model.Application app = new model.Application(doc);
						result.add(app);
					}
				}
			}
			categoryEntry = nav.getNextSibling(categoryEntry);
		}
		
		return new DataModelTabularListAdapter(result, "documentId", Arrays.asList(""));
	}
}
