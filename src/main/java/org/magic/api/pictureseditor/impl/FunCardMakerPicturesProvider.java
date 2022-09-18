package org.magic.api.pictureseditor.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicEdition;
import org.magic.api.exports.impl.PDFExport;
import org.magic.api.interfaces.abstracts.AbstractPicturesEditorProvider;
import org.magic.game.model.abilities.LoyaltyAbilities;
import org.magic.game.model.factories.AbilitiesFactory;
import org.magic.services.network.MTGHttpClient;
import org.magic.services.network.RequestBuilder;
import org.magic.services.network.RequestBuilder.METHOD;
import org.magic.services.network.URLTools;
import org.magic.tools.ImageTools;

import com.google.gson.JsonElement;

public class FunCardMakerPicturesProvider extends AbstractPicturesEditorProvider {

	
	private static final String HYBRIDE = "HYBRIDE";
	private static final String DOMAIN="funcardmaker.thaledric.fr";
	private static final String WEBSITE="http://"+DOMAIN;
	private static final String GENERATE_URL =WEBSITE+"/generate.php";
	private static final String UPLOAD_URL =WEBSITE+"/upload.php";

	private MTGHttpClient httpclient;

	
	
	public FunCardMakerPicturesProvider() {
		super();
	}
	
	@Override
	public MOD getMode() {
		return MOD.FILE;
	}
	
	private void connect()
	{
		httpclient = URLTools.newClient();
		
	}
	
	@Override
	public void setFoil(Boolean b) {
		// do nothing
		
	}

	@Override
	public void setTextSize(int size) {
		//do nothing
		
	}

	@Override
	public void setCenter(boolean center) {
		//do nothing
		
	}

	@Override
	public void setColorIndicator(boolean selected) {
		// do nothing
		
	}

	@Override
	public void setColorAccentuation(String c) {
		
		if(c.length()>1)
		{
			c=c.charAt(0)+"/"+c.charAt(1);
		}
		
		setProperty(HYBRIDE,c);
		
	}

	@Override
	public BufferedImage getPicture(MagicCard mc, MagicEdition me) throws IOException {
		if(httpclient==null)
			connect();
		
		
		
		
		RequestBuilder build= httpclient.build();
		
					 build.method(METHOD.POST).url(GENERATE_URL)
					 	  .addContent("width", "791")
						  .addContent("height", "1107")
						  .addContent("fields[title]", mc.getName())
						  .addContent("fields[type]", mc.getFullType())
						  .addContent("fields[capa]", mc.getText())
						  .addContent("fields[ta]", mc.getFlavor())
						  .addContent("fields[illustrator]", mc.getArtist())
						  .addContent("fields[copyright]",getString("COPYRIGHT"))
						  .addContent("fields[cm]",mc.getCost());
						   
						    
						    if(mc.isPlaneswalker())
						    {
						    	List<LoyaltyAbilities> abs = AbilitiesFactory.getInstance().getLoyaltyAbilities(mc);
						    	build.addContent("template", "modern-planeswalker"+abs.size());
						    	build.addContent("fields[loyalty-base]", String.valueOf(mc.getLoyalty()));
						    	for(var i=0;i<abs.size();i++)
						    	{
						    		build.addContent("fields[capa"+(i+1)+"-cost]", abs.get(i).getCost().toString().trim());
						    		build.addContent("fields[capa"+(i+1)+"]", abs.get(i).getEffect().toString().trim());
						    	}
						    }
						    else
						    {
						    	build.addContent("template", getString("LAYOUT_OLD_MODERN").toLowerCase()+"-basic");
						    }
						    
						    String colorBase;
						    
						    if(mc.isArtifact())
						    	colorBase="a";
						    else if(mc.isLand())
						    	colorBase="l"; 
						    else if(mc.getColors().isEmpty())
						    	colorBase="c";
						    else 
						    {
						    	if(mc.getColors().size()==1)
						    		colorBase=mc.getColors().get(0).getCode();
						    	else if (mc.getColors().size()>1)
						    		colorBase="m";
						    	else
						    		colorBase=mc.getColors().get(0).getCode();
						    }
						    
						    if(!getString(HYBRIDE).isEmpty())
						    	colorBase=getString(HYBRIDE).toLowerCase();
						    
						    
						    build.addContent("fields[background-base]", colorBase.toLowerCase());
						    build.addContent("fields[background-texture]", colorBase.toLowerCase());
						    
						    if(mc.isCreature())
						    	build.addContent("fields[fe]",mc.getPower()+"/"+mc.getToughness());
						    
						    if(mc.getRarity()!=null)
						    	build.addContent("fields[se-rarity]",mc.getRarity().name().substring(0,1).toLowerCase());
							
						    if(mc.getImageName()!=null && !mc.getImageName().startsWith("http"))
						    {
						    	var f = new File(mc.getImageName());
						    	if(f.exists())
						    	{
						    		String filename=upload(f);
						    		build.addContent("fields[illustration]",filename);
						    	}
						    }

						    build.addHeader(URLTools.HOST, DOMAIN)
							   	 .addHeader(URLTools.ORIGIN, WEBSITE)
							     .addHeader(URLTools.REFERER,WEBSITE);
							
						    logger.trace(build);
						    
						    String ret = httpclient.toString(httpclient.execute(build));
						    logger.trace("RESPONSE: {}",ret);
						    
						    JsonElement el = URLTools.toJson(ret);
		
		return ImageTools.readBase64(el.getAsJsonObject().get("image").getAsString());
	}

	
	private String upload(File f) throws IOException {
		if(httpclient==null)
			connect();
		
		var builder = MultipartEntityBuilder.create();
								builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
								builder.addPart("fcm-file-media", new FileBody(f, ContentType.DEFAULT_BINARY));
								builder.addTextBody("fcm-field-illuscrop-x", "0");
								builder.addTextBody("fcm-field-illuscrop-y", "0");
								builder.addTextBody("fcm-field-illuscrop-w", "46");
								builder.addTextBody("fcm-field-illuscrop-h", "7");
								builder.addTextBody("MAX_FILE_SIZE", "104857600");
		
		HttpEntity ent = builder.build();
		Map<String,String> map = httpclient.buildMap()
									.put("Host", DOMAIN)
									.put("Origin", WEBSITE)
									.put("Referer",WEBSITE)
									.put("X-Requested-With","XMLHttpRequest").build();
	            
				var response = URLTools.toJson(httpclient.toString(httpclient.doPost(UPLOAD_URL, ent, map)));
				 logger.trace("response: {}",response);
				 
				 if(response.getAsJsonObject().get("error")!=null)
					 throw new IOException(response.getAsJsonObject().get("error").getAsString());
			
				 return response.getAsJsonObject().get("filepath").getAsString();
				 
				 
	}

	
	@Override
	public Map<String, String> getDefaultAttributes() {
		return Map.of("COPYRIGHT", "(c)2021-Wizards of the coast",
							   "LAYOUT_OLD_MODERN","modern",
							   HYBRIDE,"");
	}
	
	@Override
	public String getName() {
		return "FunCardMaker";
	}
	
	@Override
	public String getVersion() {
		return "0.4.1-alpha";
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(PDFExport.class.getResource("/icons/plugins/smf.png"));
	}
	
	@Override
	public STATUT getStatut() {
		return STATUT.BETA;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj ==null)
			return false;
		
		return hashCode()==obj.hashCode();
	}
	
	
}
