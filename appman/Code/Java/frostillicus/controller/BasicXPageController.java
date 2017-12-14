package frostillicus.controller;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import lotus.domino.*;

public class BasicXPageController implements XPageController {
	private static final long serialVersionUID = 1L;

	public BasicXPageController() { }

	public void beforePageLoad() throws Exception { }
	public void afterPageLoad() throws Exception { }

	public void beforeRenderResponse(PhaseEvent event) throws Exception { }
	public void afterRenderResponse(PhaseEvent event) throws Exception { }

	public void afterRestoreView(PhaseEvent event) throws Exception { }

	protected static Object resolveVariable(String varName) {
		return ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), varName);
	}

	public boolean isEditable() { return false; }



	public String getServerAbbreviatedName() throws NotesException {
		Session session = ExtLibUtil.getCurrentSession();
		String server = session.getServerName();
		Name name = session.createName(server);
		String abbreviatedName = name.getAbbreviated();
		name.recycle();
		return abbreviatedName;
	}
}
