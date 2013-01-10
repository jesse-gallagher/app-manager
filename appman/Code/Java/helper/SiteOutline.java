package helper;

import java.net.URLEncoder;
import java.util.*;

//import javax.faces.context.FacesContext;

//import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import lotus.domino.*;

public class SiteOutline extends ArrayList<Map<String, Object>> {
	private static final long serialVersionUID = 1L;
	private int counter = 0;

	public SiteOutline() throws Exception {
		Database database = ExtLibUtil.getCurrentDatabase();

		Outline outline = database.getOutline("XSPOutline");
		OutlineEntry entry = outline.getFirst();
		while(entry != null) {
			entry = processNode(database, outline, entry, this);
		}
		outline.recycle();
	}

	private OutlineEntry processNode(Database database, Outline outline, OutlineEntry entry, List<Map<String, Object>> root) throws Exception {
		counter++;
		if(counter > 1000) { throw new Exception("oh boy"); }
		int level = entry.getLevel();


		Map<String, Object> thisEntry = new HashMap<String, Object>();
		thisEntry.put("label", entry.getLabel());
		root.add(thisEntry);

		switch(entry.getType()) {
		case OutlineEntry.OUTLINE_OTHER_UNKNOWN_TYPE:
			// Must be a container
			thisEntry.put("type", "container");
			List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
			thisEntry.put("children", children);

			// Look for its children
			OutlineEntry nextEntry = outline.getNext(entry);
			while(nextEntry != null && nextEntry.getLevel() > level) {
				nextEntry = processNode(database, outline, nextEntry, children);
			}

			return nextEntry;
		case OutlineEntry.OUTLINE_TYPE_NAMEDELEMENT:
			thisEntry.put("type", "view");
			//			View view = database.getView(entry.getNamedElement());
			//			if(view != null) {
			//				this.handledViews.add(view.getUniversalID());
			//
			//				ITreeNode leafNode = createViewNode(entry);
			//				if(root == null) {
			//					addChild(leafNode);
			//				} else {
			//					root.addChild(leafNode);
			//				}
			//				view.recycle();
			//			}

			break;
		case OutlineEntry.OUTLINE_TYPE_URL:
			thisEntry.put("type", "url");

			if(entry.getImagesText().isEmpty()) {
				thisEntry.put("image", "/.ibmxspres/domino/icons/ecblank.gif");
			} else {
				thisEntry.put("image", "/" + urlEncode(entry.getImagesText()) + "?Open&ImgIndex=1");
			}
			String pageName = (entry.getURL().startsWith("/") ? "" : "/") + entry.getURL();
			thisEntry.put("href", pageName);
			//			urlNode.setSelected(pageName.startsWith(JSFUtil.getViewRoot().getPageName()));
			break;
		}

		return outline.getNext(entry);
	}

	private String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch(Exception e) { return value; }
	}
}
