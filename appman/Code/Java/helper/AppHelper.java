package helper;

import java.io.Serializable;
import com.ibm.xsp.model.DataObject;

public class AppHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private TemplateNameTranslator templateNames;

	public DataObject getTemplateNames() {
		if(this.templateNames == null) {
			this.templateNames = new TemplateNameTranslator();
		}
		return this.templateNames;
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