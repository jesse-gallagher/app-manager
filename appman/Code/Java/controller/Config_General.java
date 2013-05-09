package controller;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import java.util.*;
import frostillicus.controller.BasicDocumentController;
import lotus.domino.*;

public class Config_General extends BasicDocumentController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();

		Database database = ExtLibUtil.getCurrentDatabase();
		View config = database.getView("Configuration");
		config.setAutoUpdate(false);
		ViewEntry general = config.getEntryByKey("General", true);
		if(general == null) {
			Document generalConfig = database.createDocument();
			generalConfig.replaceItemValue("Form", "Configuration\\General");
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
