package helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.Name;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.DataObject;

public class AppHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private TemplateNameTranslator templateNames;
	
	public String getAppName() throws NotesException {
		Database database = ExtLibUtil.getCurrentDatabase();
		return database.getTitle();
	}

	public DataObject getTemplateNames() {
		if(this.templateNames == null) {
			this.templateNames = new TemplateNameTranslator();
		}
		return this.templateNames;
	}
	
	@SuppressWarnings("unchecked")
	public List<SelectItem> getKnownServers() throws NotesException {
		Session session = ExtLibUtil.getCurrentSession();
		Database names = session.getDatabase("", "names.nsf");
		View servers = names.getView("($Servers)");
		List<String> serverNames = servers.getColumnValues(0);
		List<SelectItem> result = new ArrayList<SelectItem>(serverNames.size());
		
		String current = session.getServerName();
		Name currentName = session.createName(current);
		SelectItem currentItem = new SelectItem(current, currentName.getAbbreviated() + " (Current)");
		result.add(currentItem);
		
		for(String server : serverNames) {
			if(!StringUtil.equals(server, current)) {
				Name name = session.createName(server);
				SelectItem selectItem = new SelectItem(server, name.getAbbreviated());
				result.add(selectItem);
			}
		}
		
		return result;
	}


	public class TemplateNameTranslator implements Serializable, DataObject {
		private static final long serialVersionUID = 1L;

		public Object getValue(Object templateName) {
			return templateName;
		}

		public Class<?> getType(Object templateName) { return String.class; }
		public boolean isReadOnly(Object templateName) { return true; }
		public void setValue(Object templateName, Object databaseTitle) { }

	}
}