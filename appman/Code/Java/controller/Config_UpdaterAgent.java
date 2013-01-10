package controller;

import frostillicus.JSFUtil;
import frostillicus.controller.BasicXPageController;
import lotus.domino.*;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.xml.*;
import java.util.*;

public class Config_UpdaterAgent extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	private XMLDocument xmlDoc;

	@Override
	public void beforePageLoad() throws Exception {
		fetchAgentInfo();
	}

	private void fetchAgentInfo() throws Exception {
		Map<String, String> param = JSFUtil.getParam();
		String noteId = param.get("agentId");

		Database database = ExtLibUtil.getCurrentDatabase();
		Document agentDoc = database.getDocumentByID(noteId);
		DxlExporter exporter = ExtLibUtil.getCurrentSession().createDxlExporter();

		this.xmlDoc = new XMLDocument();
		String xml = exporter.exportDxl(agentDoc).replaceAll("<\\!DOCTYPE.*>", "");
		xmlDoc.loadString(xml);
	}


	public String getServer() throws Exception {
		return JSFUtil.strRight(xmlDoc.selectSingleNode("//agent").getAttribute("name"), "$$UpdaterAgent-");
	}
	public void setServer(String server) throws Exception {
		Name serverName = ExtLibUtil.getCurrentSession().createName(server);
		String canonName = serverName.getCanonical();
		serverName.recycle();

		xmlDoc.selectSingleNode("//agent").setAttribute("name", "$$UpdaterAgent-" + canonName);
		xmlDoc.selectSingleNode("//schedule").setAttribute("runserver", canonName);
		xmlDoc.selectSingleNode("//agent").setAttribute("runonbehalfof", canonName);
	}

	public String getScheduleType() throws Exception {
		return xmlDoc.selectSingleNode("//schedule").getAttribute("type");
	}
	public void setScheduleType(String type) throws Exception {
		xmlDoc.selectSingleNode("//schedule").setAttribute("type", type);
	}

	// Monthly schedule
	public String getDateInMonth() throws Exception {
		return xmlDoc.selectSingleNode("//schedule").getAttribute("dateinmonth");
	}
	public void setDateInMonth(String dateInMonth) throws Exception {
		xmlDoc.selectSingleNode("//schedule").setAttribute("dateinmonth", dateInMonth);
	}

	public String getEnabled() throws Exception {
		return xmlDoc.selectSingleNode("//agent").getAttribute("enabled").equals("false") ? "false" : "true";
	}
	public void setEnabled(String enabled) throws Exception {
		xmlDoc.selectSingleNode("//agent").setAttribute("enabled", enabled);
	}


	public String save() {
		DxlImporter importer = null;
		try {
			Database database = ExtLibUtil.getCurrentDatabase();
			Database signerDB = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().getDatabase(database.getServer(), database.getFilePath());

			importer = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().createDxlImporter();
			importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_CREATE);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			importer.importDxl(xmlDoc.getXml(), signerDB);

			// Now fetch and sign the doc
			Document agentDoc = signerDB.getDocumentByID(importer.getFirstImportedNoteID());
			agentDoc.sign();
			agentDoc.save();
			agentDoc.recycle();

			fetchAgentInfo();
			JSFUtil.addMessage("confirmation", "Agent updated");
		} catch(Exception e) {
			JSFUtil.addMessage("error", "Exception: " + e.toString());

			if("NotesException: DXL importer operation failed".equals(e.toString())) {
				try {
					JSFUtil.addMessage("error", importer.getLog());
				} catch(NotesException ne) { }
			}

			return "xsp-failure";
		}

		return "xsp-success";
	}
}
