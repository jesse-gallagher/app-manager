package converter;

import javax.faces.convert.Converter;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import java.io.Serializable;
import lotus.domino.*;
import java.util.List;

public class AbbreviatedNameConverter implements Converter, Serializable {
	private static final long serialVersionUID = 1L;

	public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
		return value;
	}

	public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
		try {
			if(value instanceof List) {
				String result = "";
				for(Object name : (List<?>)value) {
					if(result.length() > 0) { result += ", "; }
					result += abbreviateName(String.valueOf(name));
				}
				return result;
			}
			return abbreviateName(String.valueOf(value));
		} catch(Exception e) {
			return e.toString();
		}
	}

	private String abbreviateName(String name) throws NotesException {
		Name notesName = ExtLibUtil.getCurrentSession().createName(name);
		String result = notesName.getAbbreviated();
		notesName.recycle();
		return result;
	}
}
