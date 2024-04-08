package org.magic.services;

import static org.magic.services.tools.MTG.getEnabledPlugin;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;
import org.magic.api.beans.MTGCard;
import org.magic.api.beans.MTGCollection;
import org.magic.api.beans.MTGEdition;
import org.magic.api.interfaces.MTGCardsProvider;
import org.magic.api.interfaces.MTGDao;
import org.magic.services.logging.MTGLogger;
import org.magic.services.tools.MTG;
import org.utils.patterns.observer.Observer;

public class CardsManagerService {

	private static Logger logger = MTGLogger.getLogger(CardsManagerService.class);


	private CardsManagerService()
	{

	}

	public static MTGEdition detectEdition(String desc) {
		
		try {
			for(var ed : MTG.getEnabledPlugin(MTGCardsProvider.class).listEditions()) {
				var index = desc.indexOf(ed.getSet());
				if(index>=0)
					return ed;
			}
			return null;
		} catch (IOException e) {
			return null;
		}
	
	}

	

	public static MTGCard switchEditions(MTGCard mc, MTGEdition ed)
	{
		try {

			if(mc.isDoubleFaced())
				return getEnabledPlugin(MTGCardsProvider.class).searchCardByCriteria("faceName",mc.getName(), ed, false).get(0);
			else
				return getEnabledPlugin(MTGCardsProvider.class).searchCardByName(mc.getName(), ed, true).get(0);
		} catch (IOException e) {
			logger.error("{} is not found in {}",mc,ed);
			return mc;
		}
	}

	public static void removeCard(MTGCard mc , MTGCollection collection) throws SQLException
	{

		getEnabledPlugin(MTGDao.class).removeCard(mc, collection);

		if(MTGControler.getInstance().get("collections/stockAutoDelete").equals("true"))
		{
			getEnabledPlugin(MTGDao.class).listStocks(mc, collection,true).forEach(st->{
				try{
					getEnabledPlugin(MTGDao.class).deleteStock(st);
				}
				catch(Exception e)
				{
					logger.error(e);
				}
			});
		}

	}

	public static void moveCard(MTGCard mc, MTGCollection from, MTGCollection to,Observer o) throws SQLException
	{
		if(o!=null)
			getEnabledPlugin(MTGDao.class).addObserver(o);

		getEnabledPlugin(MTGDao.class).moveCard(mc, from,to);
	}

	public static void moveCard(MTGEdition ed, MTGCollection from, MTGCollection to,Observer o) throws SQLException
	{
		if(o!=null)
			getEnabledPlugin(MTGDao.class).addObserver(o);


		getEnabledPlugin(MTGDao.class).moveEdition(ed, from,to);

	}

	public static void saveCard(MTGCard mc , MTGCollection collection,Observer o) throws SQLException
	{

		if(o!=null)
			getEnabledPlugin(MTGDao.class).addObserver(o);

		getEnabledPlugin(MTGDao.class).saveCard(mc, collection);

	
		if(MTGControler.getInstance().get("collections/stockAutoAdd").equals("true"))
		{
			var st = MTGControler.getInstance().getDefaultStock();
			st.setProduct(mc);
			st.setMagicCollection(collection);
			getEnabledPlugin(MTGDao.class).saveOrUpdateCardStock(st);
		}

		if(o!=null)
			getEnabledPlugin(MTGDao.class).removeObserver(o);
	}



}
