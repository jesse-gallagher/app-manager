package controller;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import frostillicus.controller.BasicDocumentController;
import lotus.domino.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Application extends BasicDocumentController {
	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");

	public void createDesignBackup() throws NotesException {
		Database database = ExtLibUtil.getCurrentDatabase();
		Session sessionAsSigner = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess();
		Database signerDB = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());

		String newpath = getBackupPath() + database.getReplicaID() + "-" + TIMESTAMP_FORMAT.format(new Date()) + ".ntf";
		System.out.println("Storing backup in " + newpath);
		signerDB.createCopy(database.getServer(), newpath);
		System.out.println("backup complete");
	}

	public String getBackupPath() throws NotesException {
		String sep = System.getProperty("file.separator");
		String programPath = ExtLibUtil.getCurrentSession().getEnvironmentString("NotesProgram", true);
		String result = programPath + sep + "appman" + sep + "backups" + sep;
		return result;
	}
}
