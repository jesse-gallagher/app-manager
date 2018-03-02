package model;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.xpath.XPathExpressionException;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.xml.XMLDocument;
import com.raidomatic.xml.XMLNode;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DxlExporter;
import lotus.domino.DxlImporter;
import lotus.domino.NotesException;

public class DXLAgent implements Serializable {
	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("'T'HHmmss',00'");
	
	public static DXLAgent forNoteID(Database database, String noteId) throws Exception {
		Document agentDoc = database.getDocumentByID(noteId);
		DxlExporter exporter = ExtLibUtil.getCurrentSession().createDxlExporter();

		String xml = exporter.exportDxl(agentDoc).replaceAll("<\\!DOCTYPE.*>", "");
		return new DXLAgent(xml);
	}

	private final XMLDocument xmlDoc = new XMLDocument();
	
	private DXLAgent(String dxl) throws Exception {
		xmlDoc.loadString(dxl);
		
		this.name = xmlDoc.selectSingleNode("//agent").getAttribute("name");
		this.runOnServer = xmlDoc.selectSingleNode("//schedule").getAttribute("runserver");
		this.scheduleType = xmlDoc.selectSingleNode("//schedule").getAttribute("type");
		this.dateInMonth = xmlDoc.selectSingleNode("//schedule").getAttribute("dateinmonth");
		this.dayOfWeek = xmlDoc.selectSingleNode("//schedule").getAttribute("dayofweek");
		XMLNode timeNode = xmlDoc.selectSingleNode("//schedule/starttime/datetime");
		if(timeNode != null) {
			this.startTime = TIME_FORMAT.parse(timeNode.getText()).getTime();
		} else {
			this.startTime = -1;
		}
		this.enabled = !"false".equals(xmlDoc.selectSingleNode("//agent").getAttribute("enabled"));
		this.noReplace = "true".equals(xmlDoc.selectSingleNode("//agent").getAttribute("noreplace"));
	}
	
	private String name;
	private String runOnServer;
	private String scheduleType;
	private String dateInMonth;
	private String dayOfWeek;
	private long startTime;
	private boolean enabled;
	private boolean noReplace;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getRunOnServer() {
		return this.runOnServer;
	}
	public void setRunOnServer(String runOnServer) {
		this.runOnServer = runOnServer;
	}
	
	public String getScheduleType() {
		return this.scheduleType;
	}
	
	public void setScheduleType(String type) {
		this.scheduleType = type;
	}
	
	// Monthly schedule
	public String getDateInMonth() {
		return this.dateInMonth;
	}
	public void setDateInMonth(String dateInMonth) throws XPathExpressionException {
		if(StringUtil.isNotEmpty(dateInMonth)) {
			this.dateInMonth = dateInMonth;
		}
	}
	
	// Weekly schedule
	public String getDayOfWeek() throws XPathExpressionException {
		return dayOfWeek;
	}
	public void setDayOfWeek(String dayOfWeek) throws XPathExpressionException {
		if(StringUtil.isNotEmpty(dayOfWeek)) {
			this.dayOfWeek = dayOfWeek;
		}
	}
	
	// Daily schedule
	public Date getStartTime() throws XPathExpressionException, ParseException {
		if(this.startTime == -1) {
			return null;
		} else {
			return new Date(this.startTime);
		}
	}
	public void setStartTime(Date startTime) throws XPathExpressionException {
		if(startTime != null) {
			this.startTime = startTime.getTime();
		} else {
			this.startTime = -1;
		}
	}
	
	public boolean isEnabled() throws XPathExpressionException {
		return enabled;
	}
	public void setEnabled(boolean enabled) throws XPathExpressionException {
		this.enabled = enabled;
	}
	
	public void setNoReplace(boolean noReplace) {
		this.noReplace = noReplace;
	}
	public boolean isNoReplace() {
		return noReplace;
	}
	
	public void save(Database database) throws NotesException, IOException, XPathExpressionException {
		if(StringUtil.isNotEmpty(name)) {
			xmlDoc.selectSingleNode("//agent").setAttribute("name", name);
		}
		xmlDoc.selectSingleNode("//schedule").setAttribute("runserver", runOnServer);
		xmlDoc.selectSingleNode("//schedule").setAttribute("type", scheduleType);
		if(StringUtil.isNotEmpty(dateInMonth)) {
			xmlDoc.selectSingleNode("//schedule").setAttribute("dateinmonth", dateInMonth);
		}
		if(StringUtil.isNotEmpty(dayOfWeek)) {
			xmlDoc.selectSingleNode("//schedule").setAttribute("dayofweek", dayOfWeek);
		}
		if(this.startTime != -1) {
			XMLNode schedule = xmlDoc.selectSingleNode("//schedule");
			XMLNode starttime = schedule.selectSingleNode("starttime");
			if(starttime == null) { starttime = schedule.addChildElement("starttime"); }
			XMLNode datetime = starttime.selectSingleNode("datetime");
			if(datetime == null) {
				datetime = starttime.addChildElement("datetime");
				datetime.setAttribute("dst", "false");
			}
			datetime.setText(TIME_FORMAT.format(new Date(startTime)));
		}
		xmlDoc.selectSingleNode("//agent").setAttribute("enabled", String.valueOf(enabled));
		xmlDoc.selectSingleNode("//agent").setAttribute("noreplace", String.valueOf(noReplace));
		
		DxlImporter importer = database.getParent().createDxlImporter();
		try {
			importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_CREATE);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			importer.importDxl(xmlDoc.getXml(), database);
	
			// Now fetch and sign the doc
			Document agentDoc = database.getDocumentByID(importer.getFirstImportedNoteID());
			agentDoc.sign();
			agentDoc.save();
			agentDoc.recycle();
		} catch(NotesException ne) {
			if("NotesException: DXL importer operation failed".equals(ne.toString())) {
				throw new RuntimeException(importer.getLog());
			}
		}
	}
}
