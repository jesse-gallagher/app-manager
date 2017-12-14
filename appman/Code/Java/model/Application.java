package model;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

public class Application extends AbstractModel {
	private static final long serialVersionUID = 1L;
<<<<<<< HEAD
	
	// TODO figure out why these annotations aren't visible from the plugin
	@Column
	private String title;
	@Column
	private String server;
	@Column
	private String filePath;
	@Column
	private String replicaId;
	@Column
	private String hidden;
	@Column
	private int documentCount;
	
	public Application() {
		super();
	}
=======
>>>>>>> parent of 9f252d2... Switched model.Application to use CMSDocument

	public Application(Document doc) throws NotesException {
		super(doc);
	}

<<<<<<< HEAD
	public Application(final Document document) {
		super(document);
	}
	
	// ******************************************************************************
	// * CMSDocument methods
	// ******************************************************************************
	
	@Override
	protected void initialize() {
		
	}
	
	protected void doLoadDocument(Document doc, boolean useRichText, boolean loadAttachments) throws Exception {
		this.title = doc.getItemValueString("title");
		this.filePath = doc.getItemValueString("filePath");
		this.server = doc.getItemValueString("server");
		this.replicaId = doc.getItemValueString("replicaId");
		this.hidden = doc.getItemValueString("hidden");
		this.documentCount = doc.getItemValueInteger("documentCount");
	}

	@Override
	protected void doSaveNewDocument(Document doc) throws Exception {
		
	}
	
	@Override
	protected void doSaveDocument(Document doc) throws Exception {
		doc.replaceItemValue("title", this.title);
		doc.replaceItemValue("filePath", this.filePath);
		doc.replaceItemValue("server", this.server);
		doc.replaceItemValue("replicaId", this.replicaId);
		doc.replaceItemValue("hidden", this.hidden);
		doc.replaceItemValue("documentCount", this.documentCount);
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
=======
	public Application(Database database) throws NotesException {
		super(database);
	}

	@Override
	protected boolean doSave() throws Exception {
		return true;
	}

>>>>>>> parent of 9f252d2... Switched model.Application to use CMSDocument
}
