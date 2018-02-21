package model;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class ServerInfo {
	private static final String AGENT_PREFIX = "$$UpdaterAgent-";
	private static final String CURRENT_SUFFIX = " (Current)";
	
	public static ServerInfo fromAgentName(String agentName) throws NotesException {
		String names = agentName.substring(AGENT_PREFIX.length());
		String sourceServer, targetServer;
		int delimIndex = names.indexOf('~');
		if(delimIndex > -1) {
			sourceServer = names.substring(0, delimIndex);
			targetServer = names.substring(delimIndex+1);
		} else {
			sourceServer = names;
			targetServer = null;
		}
		
		return new ServerInfo(sourceServer, targetServer);
	}
	
	private final String sourceServer;
	private final String targetServer;
	
	public ServerInfo(String sourceServer, String targetServer) throws NotesException {
		if(StringUtil.isEmpty(sourceServer)) {
			throw new IllegalArgumentException("sourceServer cannot be empty");
		}
		this.sourceServer = cleanServerName(sourceServer);
		this.targetServer = cleanServerName(targetServer);
	}
	
	public String getSourceServer() {
		return sourceServer;
	}
	
	public String getTargetServer() {
		return targetServer;
	}
	
	public String toAgentName() {
		StringBuilder result = new StringBuilder(AGENT_PREFIX);
		result.append(sourceServer);
		if(StringUtil.isNotEmpty(targetServer) && !StringUtil.equalsIgnoreCase(sourceServer, targetServer)) {
			result.append('~');
			result.append(targetServer);
		}
		return result.toString();
	}
	
	private static String cleanServerName(String serverName) throws NotesException {
		if(StringUtil.isEmpty(serverName)) {
			return null;
		}
		
		Session session = ExtLibUtil.getCurrentSession();
		String workingName = serverName;
		if(workingName.endsWith(CURRENT_SUFFIX)) {
			workingName = workingName.substring(0, workingName.length()-CURRENT_SUFFIX.length());
		}
		Name name = session.createName(workingName);
		return name.getCanonical();
	}
}
