package org.magic.services.workers;

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.magic.api.beans.MTGNotification;
import org.magic.api.beans.MTGNotification.MESSAGE_TYPE;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicDeck;
import org.magic.api.interfaces.MTGCardsExport;
import org.magic.api.interfaces.abstracts.AbstractCardExport;
import org.magic.gui.abstracts.AbstractBuzyIndicatorComponent;
import org.magic.services.MTGControler;
import org.magic.services.MTGLogger;
import org.utils.patterns.observer.Observable;
import org.utils.patterns.observer.Observer;

public class CardExportWorker extends SwingWorker<Void, MagicCard> {

	protected MTGCardsExport exp;
	protected Logger logger = MTGLogger.getLogger(this.getClass());
	protected Observer o;
	protected List<MagicCard> cards = null;
	protected AbstractBuzyIndicatorComponent buzy;
	private File f;
	private MagicDeck export;
	private Exception err;
	
	
	
	public CardExportWorker(MTGCardsExport exp,List<MagicCard> export,AbstractBuzyIndicatorComponent buzy,File f) {
		this.exp=exp;
		this.buzy=buzy;
		this.f=f;
		this.export=MagicDeck.toDeck(export);
		err=null;
		o=(Observable obs, Object c)->publish((MagicCard)c);
		exp.addObserver(o);
	}
	
	public CardExportWorker(MTGCardsExport exp,MagicDeck export,AbstractBuzyIndicatorComponent buzy,File f) {
		this.exp=exp;
		this.buzy=buzy;
		this.f=f;
		this.export=export;
		err=null;
		o=(Observable obs, Object c)->publish((MagicCard)c);
		exp.addObserver(o);
	}
	
	
	
	@Override
	protected Void doInBackground(){
		try {
			exp.export(export, f);
		} catch (Exception e) {
			err=e;
			logger.error("error export with " + exp,e);
		}
		return null;
	}
	
	@Override
	protected void process(List<MagicCard> chunks) {
		buzy.progressSmooth(chunks.size());
	}
	
	@Override
	protected void done() {
		try {
			exp.removeObserver(o);
		} catch (Exception e) {
			logger.error(e);
		}
		buzy.end();
		
		if(err!=null)
		{
			MTGControler.getInstance().notify(new MTGNotification(MTGControler.getInstance().getLangService().getError(),err));
		}
		else
		{
			MTGControler.getInstance().notify(new MTGNotification(
					exp.getName() + " "+ MTGControler.getInstance().getLangService().get("FINISHED"),
					MTGControler.getInstance().getLangService().combine("EXPORT", "FINISHED"),
					MESSAGE_TYPE.INFO
					));
		}
		
	}
	
}
