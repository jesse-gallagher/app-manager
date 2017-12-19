package controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import frostillicus.JSFUtil;
import frostillicus.controller.BasicXPageController;
import lotus.domino.*;

import java.util.*;

import javax.faces.context.FacesContext;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.xml.*;

public class Config_UpdaterAgents extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	private List<Map<String, Object>> agentInfo;

	@Override
	public void beforePageLoad() throws Exception {
		this.agentInfo = new ArrayList<Map<String, Object>>();

		Database database = ExtLibUtil.getCurrentDatabase();
		DxlExporter exporter = ExtLibUtil.getCurrentSession().createDxlExporter();

		NoteCollection agentNotes = database.createNoteCollection(false);
		agentNotes.setSelectAgents(true);
		agentNotes.setSelectionFormula(" @Begins($Title; '$$UpdaterAgent-') ");
		agentNotes.buildCollection();

		String noteId = agentNotes.getFirstNoteID();
		while(!noteId.isEmpty()) {
			Map<String, Object> thisAgent = new HashMap<String, Object>();
			thisAgent.put("noteId", noteId);

			Document agentDoc = database.getDocumentByID(noteId);
			// Get the DXL and strip out the doctype to avoid errors looking for the DTD
			String dxl = exporter.exportDxl(agentDoc).replaceAll("<\\!DOCTYPE.*>", "");
			XMLDocument xmlDoc = new XMLDocument();
			xmlDoc.loadString(dxl);

			XMLNode agentNode = xmlDoc.selectSingleNode("//agent");
			thisAgent.put("server", JSFUtil.strRight(agentNode.getAttribute("name"), "$$UpdaterAgent-"));
			thisAgent.put("enabled", !agentNode.getAttribute("enabled").equals("false"));

			XMLNode schedule = xmlDoc.selectSingleNode("//schedule");
			String scheduleType = schedule.getAttribute("type");
			thisAgent.put("scheduleType", scheduleType);
			if(scheduleType.equals("monthly")) {
				thisAgent.put("scheduleDayInMonth", schedule.getAttribute("dayinmonth"));
			}

			agentInfo.add(thisAgent);

			noteId = agentNotes.getNextNoteID(noteId);
		}

		Collections.sort(this.agentInfo, new AgentListComparator());
	}

	public void createAgent() throws Exception {
		String server = (String)ExtLibUtil.getViewScope().get("createUpdaterAgentServer");
		if(!StringUtil.isEmpty(server)) {
			Name serverName = ExtLibUtil.getCurrentSession().createName(server);
			String canonName = serverName.getCanonical();

			// Find the template agent to copy
			Database database = ExtLibUtil.getCurrentDatabase();
			DxlExporter exporter = ExtLibUtil.getCurrentSession().createDxlExporter();
			NoteCollection agentNotes = database.createNoteCollection(false);
			agentNotes.setSelectAgents(true);
			agentNotes.setSelectionFormula(" $Title='$$UpdaterAgentTemplate' ");
			agentNotes.buildCollection();
			Document templateDoc = database.getDocumentByID(agentNotes.getFirstNoteID());

			String dxl = exporter.exportDxl(templateDoc).replaceAll("<\\!DOCTYPE.*>", "");
			XMLDocument xmlDoc = new XMLDocument();
			xmlDoc.loadString(dxl);

			// Clear out the original note info
			XMLNode noteinfo = xmlDoc.selectSingleNode("//noteinfo");
			noteinfo.getParentNode().removeChild(noteinfo);

			// Now fill in the correct information
			xmlDoc.selectSingleNode("//agent").setAttribute("name", "$$UpdaterAgent-" + canonName);
			xmlDoc.selectSingleNode("//schedule").setAttribute("runlocation", "specific");
			xmlDoc.selectSingleNode("//schedule").setAttribute("runserver", canonName);
			xmlDoc.selectSingleNode("//agent").setAttribute("runonbehalfof", canonName);

			Database signerDB = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().getDatabase(database.getServer(), database.getFilePath());

			DxlImporter importer = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().createDxlImporter();
			importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_CREATE);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			importer.importDxl(xmlDoc.getXml(), signerDB);
			if(importer.getImportedNoteCount() < 1) {
				throw new RuntimeException(importer.getLog());
			}

			// Now fetch and sign the doc
			Document agentDoc = signerDB.getDocumentByID(importer.getFirstImportedNoteID());
			agentDoc.sign();
			agentDoc.save();

			JSFUtil.appRedirect("/Config_UpdaterAgent.xsp?agentId=" + importer.getFirstImportedNoteID());

		}
	}
	
	public List<Map<String, Object>> getAgentInfo() {
		return agentInfo;
	}
	
	@SuppressWarnings("unchecked")
	public boolean isCurrentServer() throws NotesException {
		Session session = ExtLibUtil.getCurrentSession();
		String currentServer = session.getServerName();
		Map<String, Object> currentAgent = (Map<String, Object>)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "agent");
		return StringUtil.equalsIgnoreCase(currentServer, StringUtil.toString(currentAgent.get("server")));
	}
	
	@SuppressWarnings("unchecked")
	public String getAgentUrl() throws UnsupportedEncodingException {
		Map<String, Object> currentAgent = (Map<String, Object>)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "agent");
		String server = (String)currentAgent.get("server");
		String name = "$$UpdaterAgent-" + server;
		return JSFUtil.getContextPath() + "/" + URLEncoder.encode(name, "UTF-8");
	}
	
	// ******************************************************************************
	// * Manual Run
	// ******************************************************************************
	
	public void manualRun() throws NotesException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		String server = StringUtil.toString(viewScope.get("manualRunServer"));
		if(StringUtil.isNotEmpty(server)) {
			Database database = ExtLibUtil.getCurrentDatabase();
			Document tempDoc = database.createDocument();
			tempDoc.replaceItemValue("Server", server);
			Agent agent = database.getAgent("Update Database List Manual");
			agent.runWithDocumentContext(tempDoc);
		}
	}
	
	// ******************************************************************************
	// * Internal utility methods
	// ******************************************************************************

	public static class AgentListComparator implements Comparator<Map<String, Object>> {

		public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
			return ((String)arg0.get("server")).compareToIgnoreCase((String)arg1.get("server"));
		}

	}
}
