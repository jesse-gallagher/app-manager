package controller;

import java.util.Map;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.View;
import lotus.domino.ViewEntry;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.controller.BasicDocumentController;

public abstract class AbstractConfigDocumentController extends BasicDocumentController {
	private static final long serialVersionUID = 1L;

	protected abstract String getConfigurationDocumentName();
	
	@Override
	public void beforePageLoad() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();

		Database database = ExtLibUtil.getCurrentDatabase();
		View config = database.getView("Configuration");
		config.setAutoUpdate(false);
		String name = getConfigurationDocumentName();
		ViewEntry general = config.getEntryByKey(name, true);
		if(general == null) {
			Document generalConfig = database.createDocument();
			generalConfig.replaceItemValue("Form", "Configuration\\" + name);
			generalConfig.save();

			viewScope.put("configDocumentId", generalConfig.getUniversalID());

			generalConfig.recycle();
		} else {
			viewScope.put("configDocumentId", general.getUniversalID());
			general.recycle();
		}
		config.refresh();
		config.recycle();
	}
}
