package model;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

public class Application extends AbstractModel {
	private static final long serialVersionUID = 1L;

	public Application(Document doc) throws NotesException {
		super(doc);
	}

	public Application(Database database) throws NotesException {
		super(database);
	}

	@Override
	protected boolean doSave() throws Exception {
		return true;
	}

}
