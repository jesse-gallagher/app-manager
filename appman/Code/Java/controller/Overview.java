package controller;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import frostillicus.controller.BasicXPageController;
import lotus.domino.*;

public class Overview extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public String getServerName() throws NotesException {
		return ExtLibUtil.getCurrentSession().getServerName();
	}
	public int getXPageAppCount() throws NotesException {
		Database database = ExtLibUtil.getCurrentDatabase();
		View xpageApps = database.getView("Databases\\XPage Apps");
		xpageApps.setAutoUpdate(false);
		xpageApps.resortView("Server");
		ViewEntryCollection entries = xpageApps.getAllEntriesByKey(getServerName());
		int count = entries.getCount();
		entries.recycle();
		xpageApps.recycle();
		return count;
	}
}
