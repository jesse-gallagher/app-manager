package controller;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import lotus.domino.*;

public class Config_Notifications extends AbstractConfigDocumentController {
	private static final long serialVersionUID = 1L;
	
	private final AbstractAgentController agentController = new AbstractAgentController() {
		private static final long serialVersionUID = 1L;

		@Override protected String getNoteID() {
			try {
				Database database = ExtLibUtil.getCurrentDatabase();
				NoteCollection notes = database.createNoteCollection(false);
				notes.setSelectAgents(true);
				notes.setSelectionFormula("$TITLE='Send Email Notifications'");
				notes.buildCollection();
				return notes.getFirstNoteID();
			} catch(NotesException ne) {
				throw new RuntimeException(ne);
			}
		}
	};

	@Override
	protected String getConfigurationDocumentName() {
		return "Notifications";
	}

	public AbstractAgentController getAgentController() {
		return agentController;
	}
	
	@Override
	public void beforePageLoad() throws Exception {
		super.beforePageLoad();
		this.agentController.beforePageLoad();
	}
	
	@Override
	public String save() throws Exception {
		this.agentController.save();
		return super.save();
	}
}
