package frostillicus;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import lotus.domino.*;

public class FTIndexEnsurer {
	public FTIndexEnsurer() throws NotesException {
		try {
			Database database = ExtLibUtil.getCurrentDatabase();
			Session sessionAsSigner = ExtLibUtil.getCurrentSessionAsSigner();
			Database signerDB = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());

			if(!signerDB.isFTIndexed()) {
				System.out.println("Indexing DB");
				signerDB.createFTIndex(0, false);
			}
		} catch(Exception ne) {
			ne.printStackTrace();
		}
	}
}
