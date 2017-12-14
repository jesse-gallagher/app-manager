package model;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

public class Application extends AbstractModel {
	private static final long serialVersionUID = 1L;
	
	private String title;
	private String server;
	private String filePath;
	private String replicaId;
	private String hidden;
	private int documentCount;
	
	public Application(Database database) throws NotesException {
		super(database);
	}

	public Application(final Document doc) throws NotesException {
		super(doc);
		loadDocument(doc);
	}
	
	protected void loadDocument(Document doc) throws NotesException {
		this.title = doc.getItemValueString("title");
		this.filePath = doc.getItemValueString("filePath");
		this.server = doc.getItemValueString("server");
		this.replicaId = doc.getItemValueString("replicaId");
		this.hidden = doc.getItemValueString("hidden");
		this.documentCount = doc.getItemValueInteger("documentCount");
	}

	@Override
	protected boolean doSave(Document doc) throws Exception {
		doc.replaceItemValue("title", this.title);
		doc.replaceItemValue("filePath", this.filePath);
		doc.replaceItemValue("server", this.server);
		doc.replaceItemValue("replicaId", this.replicaId);
		doc.replaceItemValue("hidden", this.hidden);
		doc.replaceItemValue("documentCount", this.documentCount);
		return true;
	}
	
	// ******************************************************************************
	// * Getters/Setters
	// ******************************************************************************

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getReplicaId() {
		return replicaId;
	}

	public void setReplicaId(String replicaId) {
		this.replicaId = replicaId;
	}
	
	public String getHidden() {
		return hidden;
	}
	public void setHidden(String hidden) {
		this.hidden = hidden;
	}
	
	public int getDocumentCount() {
		return documentCount;
	}
	public void setDocumentCount(int documentCount) {
		this.documentCount = documentCount;
	}
}
