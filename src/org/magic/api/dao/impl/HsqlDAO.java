package org.magic.api.dao.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCollection;
import org.magic.api.beans.MagicDeck;
import org.magic.api.beans.MagicEdition;
import org.magic.api.interfaces.AbstractMagicDAO;

public class HsqlDAO extends AbstractMagicDAO{

	static final Logger logger = LogManager.getLogger(HsqlDAO.class.getName());
    Connection con;
    List<MagicCard> listNeeded ;
    
    public HsqlDAO() throws ClassNotFoundException, SQLException {
    	 super();	
 		if(!new File(confdir, getName()+".conf").exists()){
 			props.put("DRIVER", "org.hsqldb.jdbc.JDBCDriver");
 			props.put("URL", System.getProperty("user.home")+"/magicDeskCompanion/db");
 			props.put("DBNAME", "magicDB");
 			props.put("LOGIN", "SA");
 			props.put("PASS", "");
 		save();
 		}
	}
    
    
	public void init() throws ClassNotFoundException, SQLException {
	      logger.debug("init HsqlDB");
		  Class.forName(props.getProperty("DRIVER"));
	      con=DriverManager.getConnection("jdbc:hsqldb:"+props.getProperty("URL")+"/"+props.getProperty("DBNAME"),props.getProperty("LOGIN"),props.getProperty("PASS"));
		  
		  createDB();
		
	 }
	 
	 public boolean createDB()
	 {
		 try{
		 	con.createStatement().executeUpdate("create table cards (name varchar(250), mcard OBJECT, edition varchar(20), cardprovider varchar(50),collection varchar(250))");
		 	logger.debug("Create table Cards");
		 	con.createStatement().executeUpdate("create table decks (name varchar(45),mcard OBJECT)");
		 	logger.debug("Create table decks");
		 	con.createStatement().executeUpdate("create table collections (name varchar(250) PRIMARY KEY)");
		 	logger.debug("Create table collections");
		 	con.createStatement().executeUpdate("insert into collections values ('Library')");
		 	con.createStatement().executeUpdate("insert into collections values ('Needed')");
		 	con.createStatement().executeUpdate("insert into collections values ('For sell')");
		 	logger.debug("populate collections");
		 	
		 	
		 	return true;
		 }catch(SQLException e)
		 {
			 logger.debug(getName()+ ": Base already exist");
			 return false;
		 }
		 
	 }

	@Override
	public void saveCard(MagicCard mc, MagicCollection collection) throws SQLException {
		
		logger.debug("saving " + mc +" in " + collection);
		
		PreparedStatement pst = con.prepareStatement("insert into cards values (?,?,?,?,?)");
		 pst.setString(1, mc.getName());
		 pst.setObject(2, mc);
		 pst.setString(3, mc.getEditions().get(0).getId());
		 pst.setString(4, "");
		 pst.setString(5, collection.getName());
		 
		 pst.executeUpdate();
		
	}

	@Override
	public void removeCard(MagicCard mc, MagicCollection collection) throws SQLException {
		logger.debug("remove " + mc + " from " + collection);
		PreparedStatement pst = con.prepareStatement("delete from cards where name=? and edition=? and collection=?");
		 pst.setString(1, mc.getName());
		 pst.setString(2, mc.getEditions().get(0).getId());
		 pst.setString(3, collection.getName());
		 pst.executeUpdate();
	}

	public List<MagicCard> getCardsFromCollection(MagicCollection collection,MagicEdition me) throws SQLException
	{
		
		String sql ="select * from cards where collection= ? and edition = ?";
		
		if(me==null)
			sql ="select * from cards where collection= ?";
		
		PreparedStatement pst=con.prepareStatement(sql);	
		pst.setString(1, collection.getName());
		
		if(me!=null)
			pst.setString(2, me.getId());
		
		ResultSet rs = pst.executeQuery();
		List<MagicCard> list = new ArrayList<MagicCard>();
		while(rs.next())
		{
			list.add((MagicCard) rs.getObject("mcard"));
		}
	
	return list;
	}
	
	
	@Override
	public List<MagicCard> getCardsFromCollection(MagicCollection collection) throws SQLException {
		return getCardsFromCollection(collection,null);
	}

	@Override
	public MagicCollection getCollection(String name) throws SQLException {
		PreparedStatement pst=con.prepareStatement("select * from collections where name= ?");	
			pst.setString(1, name);
		
		ResultSet rs = pst.executeQuery();
		
		if(rs.next())
		{
			MagicCollection mc = new MagicCollection();
			mc.setName(rs.getString("name"));
			
			return mc;
		}
		
		return null;
	}

	 
	
	@Override
	public void saveCollection(MagicCollection c) throws SQLException {

		PreparedStatement pst = con.prepareStatement("insert into collections values (?)");
		 pst.setString(1, c.getName());
		 
		 pst.executeUpdate();
	}

	@Override
	public void removeCollection(MagicCollection c) throws SQLException {
		
		if(c.getName().equals("Library"))
			throw new SQLException(c.getName() + " can not be deleted");
		
		PreparedStatement pst = con.prepareStatement("delete from collections where name = ?");
		 pst.setString(1, c.getName());
		 pst.executeUpdate();
		 
		 
		 pst = con.prepareStatement("delete from cards where collection = ?");
		 pst.setString(1, c.getName());
		 pst.executeUpdate();
		
	}

	@Override
	public List<MagicCollection> getCollections() throws SQLException {
		PreparedStatement pst=con.prepareStatement("select * from collections");	
		ResultSet rs = pst.executeQuery();
		List<MagicCollection> colls = new ArrayList<MagicCollection>();
		while(rs.next())
		{
			MagicCollection mc = new MagicCollection();
			mc.setName(rs.getString(1));
			colls.add(mc);
		}
		return colls;
	}

	@Override
	public void removeEdition(MagicEdition me, MagicCollection col) throws SQLException {
		logger.debug("remove " + me + " from " + col);
		PreparedStatement pst = con.prepareStatement("delete from cards where edition=? and collection=?");
		 pst.setString(1, me.getId());
		 pst.setString(2, col.getName());
		 pst.executeUpdate();
		
	}

	@Override
	public int getCardsCount(MagicCollection cols) throws SQLException {
		
		String sql = "select count(*) from cards ";
			
		if(cols!=null)
			sql+=" where collection = '" + cols.getName()+"'";
		
		
		Statement st = con.createStatement();
		logger.debug(sql);
		
		
		ResultSet rs = st.executeQuery(sql);
		rs.next();
		return rs.getInt(1);
	}

	@Override
	public String getDBLocation() {
		return props.getProperty("URL");
	}

	@Override
	public long getDBSize() {
		return FileUtils.sizeOfDirectory(new File(props.getProperty("URL")));
	}
	
	
	

	@Override
	public MagicCard loadCard(String name, MagicCollection collection) throws SQLException {
		PreparedStatement pst=con.prepareStatement("select * from cards where collection= ? and name= ?");	
		pst.setString(1, collection.getName());
		pst.setString(2, name);
		ResultSet rs = pst.executeQuery();
		return (MagicCard) rs.getObject("mcard");
	}

	@Override
	public List<String> getEditionsIDFromCollection(MagicCollection collection) throws SQLException {
		String sql ="select distinct(edition) from cards where collection=?";
		
		PreparedStatement pst=con.prepareStatement(sql);	
		pst.setString(1, collection.getName());
		ResultSet rs = pst.executeQuery();
		List<String> list = new ArrayList<String>();
		while(rs.next())
		{
			list.add(rs.getString("edition"));
		}
	
	return list;
	}

	@Override
	public List<MagicCard> listCards() throws SQLException {
		String sql ="select * from cards";
		
		PreparedStatement pst=con.prepareStatement(sql);	
		
		ResultSet rs = pst.executeQuery();
		List<MagicCard> list = new ArrayList<MagicCard>();
		while(rs.next())
		{
			list.add((MagicCard) rs.getObject("mcard"));
		}
	
	return list;
	}

	public String getName() {
		return "hSQLdb";
	}


	@Override
	public List<MagicDeck> listDeck() throws SQLException {
		String sql ="select * from decks";
		
		PreparedStatement pst=con.prepareStatement(sql);	
		
		ResultSet rs = pst.executeQuery();
		List<MagicDeck> list = new ArrayList<MagicDeck>();
		while(rs.next())
		{
			list.add((MagicDeck) rs.getObject("mcard"));
		}
	
	return list;
	}


	@Override
	public void saveDeck(MagicDeck d) throws SQLException {

		logger.debug("saving " + d);
		PreparedStatement pst = con.prepareStatement("insert into decks values (?,?)");
		 pst.setString(1, d.getName());
		 pst.setObject(2, d);
		 //pst.setString(3,d.getColors());
		 pst.executeUpdate();
		
	}


	@Override
	public void deleteDeck(MagicDeck d) throws SQLException {
		// TODO Auto-generated method stub
		
	}


}


