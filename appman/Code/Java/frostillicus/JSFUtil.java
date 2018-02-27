package frostillicus;


import javax.faces.context.FacesContext;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import lotus.domino.*;
import lotus.notes.addins.DominoServer;

import java.io.*;
import java.util.*;

public enum JSFUtil {
	;

	public static String strLeftBack(String input, String delimiter) {
		return input.substring(0, input.lastIndexOf(delimiter));
	}
	public static String strRightBack(String input, String delimiter) {
		return input.substring(input.lastIndexOf(delimiter) + delimiter.length());
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> getFlashScope() {
		return (Map<Object, Object>)ExtLibUtil.resolveVariable("flashScope");
	}
	@SuppressWarnings("unchecked")
	public static void addMessage(String type, String message) {
		Map<Object, Object> flashScope = getFlashScope();
		List<Object> messages = (List<Object>)flashScope.get(type + "Messages");
		if(messages == null) {
			messages = new ArrayList<Object>();
			flashScope.put(type + "Messages", messages);
		}
		messages.add(message);
	}
	@SuppressWarnings("unchecked")
	public static Map<String, String> getParam() {
		return (Map<String, String>)ExtLibUtil.resolveVariable("param");
	}

	@SuppressWarnings("unchecked")
	public static boolean isDocEditableBy(Document doc, String userName) throws NotesException {
		Database database = ExtLibUtil.getCurrentDatabase();

		// Easy check first
		int level = database.queryAccess(userName);
		if(level >= ACL.LEVEL_EDITOR) {
			return true;
		} else if(level < ACL.LEVEL_AUTHOR) {
			return false;
		}

		// Gather the effective user names and roles
		DominoServer server = new DominoServer(database.getServer());
		Collection<String> rawNames = (Collection<String>)server.getNamesList(userName);
		Set<String> lowerNames = new HashSet<String>();
		for(String name : rawNames) { lowerNames.add(name.toLowerCase()); }
		for(String role : (List<String>)database.queryAccessRoles(userName)) { lowerNames.add(role.toLowerCase()); }

		// Now look through all the items in the doc to see if they're an Authors field
		// If so, check to see if any of the names or roles are in them
		for(Item item : (List<Item>)doc.getItems()) {
			if(item.getType() == Item.AUTHORS) {
				Set<String> itemLowerNames = new HashSet<String>();
				for(String name : (List<String>)item.getValues()) { itemLowerNames.add(name.toLowerCase()); }
				itemLowerNames.retainAll(lowerNames);
				if(itemLowerNames.size() > 0) {
					// Then at least one name is common between them
					item.recycle();
					return true;
				}
			}

			item.recycle();
		}
		doc.recycle();

		return false;
	}

	public static String getContextPath() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
	}
	public static void appRedirect(String appPath) throws IOException {
		String cleanPath = appPath.startsWith("/") ? appPath : "/" + appPath;
		FacesContext.getCurrentInstance().getExternalContext().redirect(getContextPath() + cleanPath);
	}
}