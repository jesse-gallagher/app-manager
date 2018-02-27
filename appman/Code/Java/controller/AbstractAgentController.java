package controller;

import java.util.Date;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.JSFUtil;
import frostillicus.controller.BasicXPageController;
import lotus.domino.Database;
import lotus.domino.Document;
import model.DXLAgent;
import model.ServerInfo;

public abstract class AbstractAgentController extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	protected abstract String getNoteID();
	
	private DXLAgent agent;

	@Override
	public void beforePageLoad() throws Exception {
		fetchAgentInfo();
	}

	private void fetchAgentInfo() throws Exception {
		Database database = ExtLibUtil.getCurrentDatabase();
		this.agent = DXLAgent.forNoteID(database, getNoteID());
	}


	public String getServer() throws Exception {
		String agentName = agent.getName();
		ServerInfo serverInfo = ServerInfo.fromAgentName(agentName);
		return serverInfo.getSourceServer();
	}
	public void setServer(String server) throws Exception {
		String targetServer = getTargetServer();
		ServerInfo serverInfo = new ServerInfo(server, targetServer);

		agent.setName(serverInfo.toAgentName());
		agent.setRunOnServer(serverInfo.getSourceServer());
	}
	
	public String getTargetServer() throws Exception {
		String agentName = agent.getName();
		ServerInfo serverInfo = ServerInfo.fromAgentName(agentName);
		return serverInfo.getTargetServer();
	}
	public void setTargetServer(String targetServer) throws Exception {
		String server = getServer();
		ServerInfo serverInfo = new ServerInfo(server, targetServer);
		agent.setName(serverInfo.toAgentName());
	}

	public String getScheduleType() throws Exception {
		return agent.getScheduleType();
	}
	public void setScheduleType(String type) throws Exception {
		agent.setScheduleType(type);
	}

	// Monthly schedule
	public String getDateInMonth() throws Exception {
		return agent.getDateInMonth();
	}
	public void setDateInMonth(String dateInMonth) throws Exception {
		agent.setDateInMonth(dateInMonth);
	}

	// Weekly schedule
	public String getDayOfWeek() throws Exception {
		return agent.getDayOfWeek();
	}
	public void setDayOfWeek(String dayOfWeek) throws Exception {
		agent.setDayOfWeek(dayOfWeek);
	}

	// Daily schedule
	public Date getStartTime() throws Exception {
		return agent.getStartTime();
	}
	public void setStartTime(Date startTime) throws Exception {
		agent.setStartTime(startTime);
	}

	public String getEnabled() throws Exception {
		return String.valueOf(agent.isEnabled());
	}
	public void setEnabled(String enabled) throws Exception {
		agent.setEnabled(Boolean.valueOf(enabled));
	}

	public String save() {
		try {
			Database database = ExtLibUtil.getCurrentDatabase();
			Database signerDB = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().getDatabase(database.getServer(), database.getFilePath());

			agent.save(signerDB);

			fetchAgentInfo();
			JSFUtil.addMessage("confirmation", "Agent updated");
		} catch(Exception e) {
			JSFUtil.addMessage("error", "Exception: " + e.toString());

			return "xsp-failure";
		}

		return "xsp-success";
	}

	public String delete() {
		try {
			Database database = ExtLibUtil.getCurrentDatabase();
			Database signerDB = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().getDatabase(database.getServer(), database.getFilePath());
			Document agentDoc = signerDB.getDocumentByID(getNoteID());
			agentDoc.remove(true);
		} catch(Exception e) {
			JSFUtil.addMessage("error", "Exception: " + e.toString());

			return "xsp-failure";
		}
		return "deleted-agent";
	}
}
