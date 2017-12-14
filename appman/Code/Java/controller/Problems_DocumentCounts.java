package controller;

import java.util.ArrayList;
import java.util.List;

import net.cmssite.endeavour60.util.EndeavourUtil;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.View;
import org.openntf.domino.ViewEntry;
import org.openntf.domino.ViewNavigator;

import util.BeanTabularListAdapter;

import frostillicus.xsp.controller.BasicXPageController;

public class Problems_DocumentCounts extends BasicXPageController {
	private static final long serialVersionUID = 1L;
	
	public static final double DIVERGENCE = 0.02;

	public BeanTabularListAdapter<model.Application> getApplications() {
		List<model.Application> result = new ArrayList<model.Application>();
		
		Database database = EndeavourUtil.getDatabase();
		View view = EndeavourUtil.getView(database, "Problems\\Divergent Document Counts");
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
		
		return new BeanTabularListAdapter<model.Application>(result, "unid");
	}
}
