package org.magic.api.externalshop.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.groovy.util.Maps;
import org.apache.http.entity.StringEntity;
import org.magic.api.beans.MagicEdition;
import org.magic.api.beans.enums.EnumItems;
import org.magic.api.beans.shop.Category;
import org.magic.api.beans.shop.Contact;
import org.magic.api.beans.shop.Transaction;
import org.magic.api.interfaces.MTGProduct;
import org.magic.api.interfaces.MTGStockItem;
import org.magic.api.interfaces.abstracts.AbstractExternalShop;
import org.magic.api.interfaces.abstracts.extra.AbstractProduct;
import org.magic.api.interfaces.abstracts.extra.AbstractStockItem;
import org.magic.services.MTGControler;
import org.magic.services.network.MTGHttpClient;
import org.magic.services.network.RequestBuilder;
import org.magic.services.network.RequestBuilder.METHOD;
import org.magic.services.network.URLTools;
import org.magic.tools.ImageTools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ShopifyExternalShop extends AbstractExternalShop {
	
	private static final String MYSHOPIFY_COM_API_VERSION = ".myshopify.com/admin/api/2022-01/";
	private static final String CUSTOMERS = "customer";
	private static final String PRODUCTS = "product";
	private static final String VARIANTS = "variant";
	private MTGHttpClient client = URLTools.newClient();
	
	
	
	public static void main(String[] args) throws IOException {
		MTGControler.getInstance().loadAccountsConfiguration();
		
		var it = new ShopifyExternalShop().getContactByEmail("nicolas.pihen@gmail.com");
		
		System.out.println(it);
		System.exit(0);
	}
	
	
	private JsonObject readId(String entityName, Long id) throws IOException 
	{
		var resp=build("https://"+getAuthenticator().get("SUBDOMAIN")+MYSHOPIFY_COM_API_VERSION+entityName.toLowerCase()+"s/"+id+".json",METHOD.GET).execute();
		return URLTools.toJson(resp.getEntity().getContent()).getAsJsonObject().get(entityName).getAsJsonObject();
	}
	
	private JsonArray read(String entityName, Map<String,String> attributs) throws IOException 
	{
		JsonArray arr = new JsonArray();
		var build=build("https://"+getAuthenticator().get("SUBDOMAIN")+MYSHOPIFY_COM_API_VERSION+entityName.toLowerCase()+"s.json",METHOD.GET);
		
		if(attributs!=null && !attributs.isEmpty())
			attributs.entrySet().forEach(e->build.addContent(e.getKey(), e.getValue()));
					 
		var resp = build.execute();
		
		arr.addAll(URLTools.toJson(resp.getEntity().getContent()).getAsJsonObject().get(entityName+"s").getAsJsonArray());
		var next = URLTools.parseLinksHeader(resp.getFirstHeader("Link")).get("next");
		
		
		while(next!=null)
		{
			resp = build(next,METHOD.GET).execute();
			next = URLTools.parseLinksHeader(resp.getFirstHeader("Link")).get("next");
			arr.addAll(URLTools.toJson(resp.getEntity().getContent()).getAsJsonObject().get(entityName+"s").getAsJsonArray());
		}
		return arr;
	}

	private RequestBuilder build(String url,METHOD m) {
		return RequestBuilder.build()
				  .url(url)
				  .method(m)
				  .setClient(client)
				  .addHeader("X-Shopify-Access-Token", getAuthenticator().get("ACCESS_TOKEN"))
				  .addHeader(URLTools.ACCEPT, URLTools.HEADER_JSON)
				  .addContent("limit","250");
	}
	
	private List<MTGStockItem> parseVariants(JsonObject obj) {
		
		var ret = new ArrayList<MTGStockItem>();
		
		for(JsonElement el : obj.get("variants").getAsJsonArray())
		{
				AbstractStockItem<MTGProduct> it = AbstractStockItem.generateDefault();
									  it.setProduct(parseProduct(obj));
									  it.getProduct().setEdition(new MagicEdition(el.getAsJsonObject().get("option3").getAsString(), el.getAsJsonObject().get("option3").getAsString()));
									  it.setId(el.getAsJsonObject().get("id").getAsLong());
									  it.setPrice(el.getAsJsonObject().get("price").getAsDouble());
									  it.setQte(el.getAsJsonObject().get("inventory_quantity").getAsInt());
									  it.setFoil(el.getAsJsonObject().get("option"+getString("FOIL_OPTION_NUMBER")).getAsString().toLowerCase().contains("foil"));
									  ret.add(it);
		}
		return ret;
	}
	
	private MTGProduct parseProduct(JsonObject sp) {
		MTGProduct p = AbstractProduct.createDefaultProduct();
				   p.setProductId(sp.get("id").getAsLong());
				   p.setName(sp.get("title").getAsString());
				   p.setUrl(sp.get("image").getAsJsonObject().get("src").getAsString());
				   notify(p);
		return p;
	}
	
	private Contact parseContact(JsonObject obj) {
		var c = new Contact();
			c.setEmail(obj.get("email").getAsString());
			c.setName(!obj.get("first_name").isJsonNull()?obj.get("first_name").getAsString():"");
			c.setLastName(!obj.get("last_name").isJsonNull()?obj.get("last_name").getAsString():"");
			c.setActive(obj.get("state").getAsString().equalsIgnoreCase("enabled"));
			c.setId(obj.get("id").getAsInt());
			if(obj.get("default_address")!=null) {
				var addrObj = obj.get("default_address").getAsJsonObject();	
				c.setAddress(addrObj.get("address1").getAsString());
				c.setCity(addrObj.get("city").getAsString());
				c.setZipCode(addrObj.get("zip").getAsString());
				c.setCountry(addrObj.get("country").getAsString());
				c.setTelephone(addrObj.get("phone").getAsString());
			}
			
		return c;
	
	}

	@Override
	public List<MTGProduct> listProducts(String name) throws IOException {
		return StreamSupport.stream(read(PRODUCTS,null).spliterator(), true)
							.filter(je->je.getAsJsonObject().get("title").getAsString().toLowerCase().contains(name.toLowerCase()))
							.map(a->parseProduct(a.getAsJsonObject()))
							.toList();
	}
	

	@Override
	protected List<MTGStockItem> loadStock(String name) throws IOException {
		var ret = new ArrayList<MTGStockItem>();
		//need to load all products because title search is strict only
		var arr = StreamSupport.stream(read(PRODUCTS,null).spliterator(), true)
				.filter(je->je.getAsJsonObject().get("title").getAsString().toLowerCase().contains(name.toLowerCase()))
				.toList();

		for(JsonElement a: arr )
		{
			ret.addAll(parseVariants(a.getAsJsonObject()));
		}
		return ret;
	}
	
	@Override
	public List<Contact> listContacts() throws IOException {
		var arr = read(CUSTOMERS,null);
		var ret = new ArrayList<Contact>();
		for(JsonElement je : arr)
		{
			ret.add(parseContact(je.getAsJsonObject()));
		}
		return ret;
		
	}
	
	@Override
	public List<Category> listCategories() throws IOException {
		return  StreamSupport.stream(read(PRODUCTS,Maps.of("fields","product_type")).spliterator(),true).map(je->{
			var s = je.getAsJsonObject().get("product_type").getAsString();
			return new Category(s.hashCode(), s);
			}).distinct().toList();
		
	}

	@Override
	public MTGStockItem getStockById(EnumItems typeStock, Long id) throws IOException {
		
		var obj= readId(VARIANTS,id);
		
		var it = AbstractStockItem.generateDefault();
		  it.setId(obj.get("id").getAsLong());
		  it.setPrice(obj.get("price").getAsDouble());
		  it.setQte(obj.get("inventory_quantity").getAsInt());
		  it.setFoil(obj.get("option1").getAsString().toLowerCase().contains("foil"));
		  it.setProduct(parseProduct(readId(PRODUCTS,obj.get("product_id").getAsLong())));
		  return it;
	
	}

	@Override
	public Contact getContactByEmail(String email) throws IOException {
		var arr = read(CUSTOMERS,Maps.of("email", email));
		try {
			return parseContact(arr.getAsJsonArray().get(0).getAsJsonObject());
		}
		catch(IndexOutOfBoundsException  e)
		{
			return null;
		}
		
	}
	
	@Override
	public Integer saveOrUpdateContact(Contact c) throws IOException {
		return -1;
	}

	@Override
	protected void saveOrUpdateStock(List<MTGStockItem> it) throws IOException {
		// TODO Auto-generated method stub

	}
	
	
	@Override
	public Long createProduct(MTGProduct t, Category c) throws IOException {
		
		var obj = new JsonObject();
		var prodobj = new JsonObject();
			obj.add("product", prodobj);
			
			prodobj.addProperty("title", t.getName());
			prodobj.addProperty("product_type", t.getCategory().getCategoryName());
			var images = new JsonArray();
				var imageObj = new JsonObject();
					imageObj.addProperty("attachment", ImageTools.toBase64(URLTools.extractAsImage(t.getUrl())));
				
				
				images.add(imageObj);
					
		var res = client.doPost("https://"+getAuthenticator().get("SUBDOMAIN")+MYSHOPIFY_COM_API_VERSION+"products.json", new StringEntity(obj.toString()), Map.of("X-Shopify-Access-Token", getAuthenticator().get("ACCESS_TOKEN"),URLTools.ACCEPT, URLTools.HEADER_JSON));
		
		
		System.out.println(res);
		
		return 0L;
	}


	@Override
	public void deleteContact(Contact contact) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Contact getContactByLogin(String login, String passw) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int saveOrUpdateTransaction(Transaction t) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void deleteTransaction(Transaction t) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Transaction getTransactionById(int parseInt) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Transaction> listTransactions(Contact c) throws IOException {
		return new ArrayList<>();
	}



	@Override
	protected List<Transaction> loadTransaction() throws IOException {
		return new ArrayList<>();
	}

	
	@Override
	public boolean enableContact(String token) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return "Shopify";
	}

	@Override
	public List<String> listAuthenticationAttributes() {
		return List.of("SUBDOMAIN", "ACCESS_TOKEN") ;
	}

	@Override
	public Map<String, String> getDefaultAttributes() {
		var m = super.getDefaultAttributes();
			m.put("FOIL_OPTION_NUMBER", "1");
			m.put("SET_OPTION_NUMBER", "2");
			
			return m;
	}

}
