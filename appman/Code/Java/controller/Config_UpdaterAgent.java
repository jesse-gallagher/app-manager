package controller;

import frostillicus.JSFUtil;

import java.util.*;

public class Config_UpdaterAgent extends AbstractAgentController {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected String getNoteID() {
		Map<String, String> param = JSFUtil.getParam();
		return param.get("agentId");
	}
}
