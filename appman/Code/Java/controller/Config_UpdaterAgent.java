package controller;

import frostillicus.xsp.controller.BasicXPageController;
import lotus.domino.*;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.xml.*;
import java.util.*;
import java.text.SimpleDateFormat;

import net.cmssite.endeavour60.util.EndeavourUtil;
import net.cmssite.endeavour60.util.EndeavourStrings;

public class Config_UpdaterAgent extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	private XMLDocument xmlDoc;
	private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("'T'HHmmss',00'");

	@Override
	public void beforePageLoad() throws Exception {
		fetchAgentInfo();
	}

	private void fetchAgentInfo() throws Exception {
		Map<String, String> param = EndeavourUtil.getParam();
		String noteId = param.get("agentId");

		Database database = ExtLibUtil.getCurrentDatabase();
		Document agentDoc = database.getDocumentByID(noteId);
		DxlExporter exporter = ExtLibUtil.getCurrentSession().createDxlExporter();

		this.xmlDoc = new XMLDocument();
		String xml = exporter.exportDxl(agentDoc).replaceAll("<\\!DOCTYPE.*>", "");
		xmlDoc.loadString(xml);
		xmlDoc.selectSingleNode("//agent").setAttribute("runonbehalfof", "");
	}


	public String getServer() throws Exception {
		return EndeavourStrings.strRight(xmlDoc.selectSingleNode("//agent").getAttribute("name"), "$$UpdaterAgent-");
	}
	public void setServer(String server) throws Exception {
		Name serverName = ExtLibUtil.getCurrentSession().createName(server);
		String canonName = serverName.getCanonical();
		serverName.recycle();

		xmlDoc.selectSingleNode("//agent").setAttribute("name", "$$UpdaterAgent-" + canonName);
		xmlDoc.selectSingleNode("//schedule").setAttribute("runserver", canonName);
		//xmlDoc.selectSingleNode("//agent").setAttribute("runonbehalfof", canonName);
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

	// Weekly schedule
	public String getDayOfWeek() throws Exception {
		return xmlDoc.selectSingleNode("//schedule").getAttribute("dayofweek");
	}
	public void setDayOfWeek(String dayOfWeek) throws Exception {
		xmlDoc.selectSingleNode("//schedule").setAttribute("dayofweek", dayOfWeek);
	}

	// Daily schedule
	public Date getStartTime() throws Exception {
		XMLNode timeNode = xmlDoc.selectSingleNode("//schedule/starttime/datetime");
		if(timeNode != null) {
			return TIME_FORMAT.parse(timeNode.getText());
		}
		return null;
	}
	public void setStartTime(Date startTime) throws Exception {
		XMLNode schedule = xmlDoc.selectSingleNode("//schedule");
		XMLNode starttime = schedule.selectSingleNode("starttime");
		if(starttime == null) { starttime = schedule.addChildElement("starttime"); }
		XMLNode datetime = starttime.selectSingleNode("datetime");
		if(datetime == null) {
			datetime = starttime.addChildElement("datetime");
			datetime.setAttribute("dst", "false");
		}
		datetime.setText(TIME_FORMAT.format(startTime));
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
			//Database normalSignerDB = ExtLibUtil.getCurrentSessionAsSigner().getDatabase(database.getServer(), database.getFilePath());

			importer = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().createDxlImporter();
			importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_CREATE);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			importer.importDxl(xmlDoc.getXml(), signerDB);

			// Now fetch and sign the doc
			Document agentDoc = signerDB.getDocumentByID(importer.getFirstImportedNoteID());
			agentDoc.sign();
			agentDoc.save();
			agentDoc.recycle();
			//			Agent agent = database.getAgent(xmlDoc.selectSingleNode("//agent").getAttribute("name"));
			//			agent.save();
			//			agent.recycle();

			fetchAgentInfo();
			EndeavourUtil.flashMessage("confirmation", "Agent updated");
		} catch(Exception e) {
			EndeavourUtil.flashMessage("error", "Exception: " + e.toString());

			if("NotesException: DXL importer operation failed".equals(e.toString())) {
				try {
					EndeavourUtil.flashMessage("error", importer.getLog());
				} catch(NotesException ne) { }
			}

			return "xsp-failure";
		}

		return "xsp-success";
	}

	public String delete() {
		try {
			Database database = ExtLibUtil.getCurrentDatabase();
			Database signerDB = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().getDatabase(database.getServer(), database.getFilePath());
			Document agentDoc = signerDB.getDocumentByID(EndeavourUtil.getParam().get("agentId"));
			agentDoc.remove(true);
		} catch(Exception e) {
			EndeavourUtil.flashMessage("error", "Exception: " + e.toString());

			return "xsp-failure";
		}
		return "deleted-agent";
	}
}
