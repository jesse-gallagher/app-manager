package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.model.SelectItem;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.controller.BasicXPageController;

import lotus.domino.*;

public class Config_Maintenance extends BasicXPageController {
	private static final long serialVersionUID = 1L;
	
	private static final String CACHE_KEY = Config_Maintenance.class.getName() + "_existingServerNames";
	
	@SuppressWarnings("unchecked")
	public List<SelectItem> getExistingServerNames() throws NotesException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		if(!viewScope.containsKey(CACHE_KEY)) {
			View stubs = ExtLibUtil.getCurrentDatabase().getView("Databases\\Servers");
			stubs.setAutoUpdate(false);
			List<String> serverNames = stubs.getColumnValues(0);
			Set<String> uniqueNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			uniqueNames.addAll(serverNames);
			List<SelectItem> result = new ArrayList<SelectItem>();
			Session session = ExtLibUtil.getCurrentSession();
			for(String server : uniqueNames) {
				Name name = session.createName(server);
				SelectItem item = new SelectItem(server, name.getAbbreviated());
				result.add(item);
			}
			viewScope.put(CACHE_KEY, result);
		}
		return (List<SelectItem>)viewScope.get(CACHE_KEY);
	}

	public void deleteStubs() throws NotesException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		String server = StringUtil.toString(viewScope.get("deleteStubsServer"));
		if(StringUtil.isNotEmpty(server)) {
			// Make sure it's in DN format
			Session session = ExtLibUtil.getCurrentSession();
			Name name = session.createName(server);
			server = name.getCanonical();
			name.recycle();
			
			View stubs = ExtLibUtil.getCurrentDatabase().getView("Databases\\By Server+ReplicaID");
			stubs.setAutoUpdate(false);
			DocumentCollection docs = stubs.getAllDocumentsByKey(server, true);
			docs.removeAll(true);
			
			viewScope.remove(CACHE_KEY);
			viewScope.remove("deleteStubsServer");
		}
	}
}
