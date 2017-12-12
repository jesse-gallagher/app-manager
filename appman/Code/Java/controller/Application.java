package controller;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import frostillicus.xsp.controller.BasicXPageController;
import lotus.domino.*;
import java.util.*;
import java.text.SimpleDateFormat;

import net.cmssite.endeavour60.util.EndeavourUtil;

public class Application extends BasicXPageController {
	private static final long serialVersionUID = 1L;
	
	public model.Application getApplication() {
		Map<String, Object> viewScope = EndeavourUtil.getViewScope();
		String cacheKey = getClass().getName() + ".application";
		if(!viewScope.containsKey(cacheKey)) {
			Map<String, String> param = EndeavourUtil.getParam();
			String documentId = param.get("documentId");
			net.cmssite.endeavour60.Document docRef = new net.cmssite.endeavour60.Document(documentId, EndeavourUtil.getDatabase().getFilePath());
			viewScope.put(cacheKey, new model.Application(docRef));
		}
		return (model.Application)viewScope.get(cacheKey);
	}
	
	public String save() {
		return "xsp-success";
	}

	private static SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");

	public void createDesignBackup() throws NotesException {
		// TODO make this actually point to the right DB
		Database database = ExtLibUtil.getCurrentDatabase();
		Session sessionAsSigner = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess();
		Database signerDB = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());

		String newpath = getBackupPath() + database.getReplicaID() + "-" + TIMESTAMP_FORMAT.format(new Date()) + ".ntf";
		signerDB.createCopy(database.getServer(), newpath);
	}

	public String getBackupPath() throws NotesException {
		String sep = System.getProperty("file.separator");
		String programPath = ExtLibUtil.getCurrentSession().getEnvironmentString("NotesProgram", true);
		String result = programPath + sep + "appman" + sep + "backups" + sep;
		return result;
	}
}
