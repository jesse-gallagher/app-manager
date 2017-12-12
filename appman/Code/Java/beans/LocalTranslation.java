package beans;

import net.cmssite.endeavour60.beans.AbstractTranslationBean;
import net.cmssite.endeavour60.util.EndeavourUtil;

public class LocalTranslation extends AbstractTranslationBean {
	private static final long serialVersionUID = 1L;
	
	public static final String BEAN_NAME = "eLocaleApp";
	
	public static LocalTranslation get() {
		LocalTranslation existing = (LocalTranslation)EndeavourUtil.resolveVariable(BEAN_NAME);
		return existing == null ? new LocalTranslation() : existing;
	}

	@Override
	protected String getBundleName() {
		return "Endeavour";
	}

}
