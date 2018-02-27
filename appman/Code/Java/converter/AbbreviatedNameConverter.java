package converter;

import javax.faces.convert.Converter;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import java.io.Serializable;
import lotus.domino.*;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class AbbreviatedNameConverter implements Converter, Serializable {
	private static final long serialVersionUID = 1L;

	public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
		return value;
	}

	public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
		try {
			if(value instanceof Collection) {
				return ((Collection<?>)value).stream()
					.filter(Objects::nonNull)
					.map(String::valueOf)
					.map(AbbreviatedNameConverter::abbreviateName)
					.collect(Collectors.joining(", "));
			}
			return abbreviateName(String.valueOf(value));
		} catch(Exception e) {
			return e.toString();
		}
	}

	private static String abbreviateName(String name) {
		try {
			Name notesName = ExtLibUtil.getCurrentSession().createName(name);
			String result = notesName.getAbbreviated();
			notesName.recycle();
			return result;
		} catch(NotesException ne) {
			throw new RuntimeException(ne);
		}
	}
}
