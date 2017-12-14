package controller;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import frostillicus.xsp.controller.BasicXPageController;
import lotus.domino.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Application extends BasicXPageController {
	private static final long serialVersionUID = 1L;

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
