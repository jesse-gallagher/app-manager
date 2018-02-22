package controller;

import frostillicus.JSFUtil;
import frostillicus.controller.BasicXPageController;
import lotus.domino.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.xml.*;
import java.util.*;
import java.text.SimpleDateFormat;

import model.ServerInfo;

public class Config_UpdaterAgent extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	private XMLDocument xmlDoc;
	private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("'T'HHmmss',00'");

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
		String agentName = xmlDoc.selectSingleNode("//agent").getAttribute("name");
		ServerInfo serverInfo = ServerInfo.fromAgentName(agentName);
		return serverInfo.getSourceServer();
	}
	public void setServer(String server) throws Exception {
		String targetServer = getTargetServer();
		ServerInfo serverInfo = new ServerInfo(server, targetServer);

		xmlDoc.selectSingleNode("//agent").setAttribute("name", serverInfo.toAgentName());
		xmlDoc.selectSingleNode("//schedule").setAttribute("runserver", serverInfo.getSourceServer());
	}
	
	public String getTargetServer() throws Exception {
		String agentName = xmlDoc.selectSingleNode("//agent").getAttribute("name");
		ServerInfo serverInfo = ServerInfo.fromAgentName(agentName);
		return serverInfo.getTargetServer();
	}
	public void setTargetServer(String targetServer) throws Exception {
		String server = getServer();
		ServerInfo serverInfo = new ServerInfo(server, targetServer);
		xmlDoc.selectSingleNode("//agent").setAttribute("name", serverInfo.toAgentName());
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

	public String delete() {
		try {
			Database database = ExtLibUtil.getCurrentDatabase();
			Database signerDB = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().getDatabase(database.getServer(), database.getFilePath());
			Document agentDoc = signerDB.getDocumentByID(JSFUtil.getParam().get("agentId"));
			agentDoc.remove(true);
		} catch(Exception e) {
			JSFUtil.addMessage("error", "Exception: " + e.toString());

			return "xsp-failure";
		}
		return "deleted-agent";
	}
}
