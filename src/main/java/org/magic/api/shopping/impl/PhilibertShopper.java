package org.magic.api.shopping.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.magic.api.beans.SealedStock;
import org.magic.api.beans.enums.TransactionPayementProvider;
import org.magic.api.beans.shop.Transaction;
import org.magic.api.beans.technical.RetrievableTransaction;
import org.magic.api.interfaces.abstracts.AbstractMagicShopper;
import org.magic.services.AccountsManager;
import org.magic.services.MTGControler;
import org.magic.services.network.RequestBuilder;
import org.magic.services.network.RequestBuilder.METHOD;
import org.magic.services.network.URLTools;
import org.magic.services.tools.UITools;

public class PhilibertShopper extends AbstractMagicShopper {

	private static final String BASE_URL="https://www.philibertnet.com/";
	private Document s;
	
		
	@Override
	public List<RetrievableTransaction> listOrders() throws IOException {
			init();
			var  ret = new ArrayList<RetrievableTransaction>();
			for(Element tr : s.getElementById("order-list").select("tbody tr"))
			{
				String id = tr.select("td a.color-myaccount").text().trim();
				var rt = new RetrievableTransaction();
					 rt.setSourceId(id);
					 rt.setSource(getName());
					 rt.setUrl(BASE_URL+"/en/index.php?controller=order-detail&id_order="+id+"&ajax=true");
					 rt.setDateTransaction(UITools.parseDate(tr.select("td.history_date").text(), "dd/MM/yyyy"));
					 rt.setTotalValue(UITools.parseDouble(tr.select("td.history_price").attr("data-value")));
					 rt.setComments(tr.select("td.history_method").text());
					 
					 
					 ret.add(rt);
			}
			
			return ret;
	}

	@Override
	public Transaction getTransaction(RetrievableTransaction rt) throws IOException {
		var t = buildTransaction(rt);
			 t.setCurrency(Currency.getInstance("EUR"));
			 t.setPaymentProvider(rt.getComments().contains("PayPal")?TransactionPayementProvider.PAYPAL:TransactionPayementProvider.VISA);
			 t.setMessage("");
		
			 
		var orderPage = RequestBuilder.build().url(rt.getUrl())
				   .setClient(client)
				   .method(METHOD.GET)
				   .addHeader("x-requested-with","XMLHttpRequest")
				   .toHtml();
		
		
		var stepsTable = orderPage.select("table.detail_step_by_step");
		
		for(var tr : stepsTable)
		{
			if(tr.select("TD").get(2).text().equals("Shipped"))
				t.setDateSend(UITools.parseDate(tr.select("td").get(1).text(),"dd/MM/yyyy"));
			
			if(tr.select("TD").get(2).text().equals("Order cashed"))
				t.setDatePayment(UITools.parseDate(tr.select("td").get(1).text(),"dd/MM/yyyy"));
			
			
		}
		
		
		
		
		for(Element tr : orderPage.select("table").get(1).select("tbody>tr.item"))
		{
			int index = tr.selectFirst("td:has(input)")!=null?0:1; //check if first column is checkbox or not
			
			var stock = new SealedStock();
				 stock.setComment(tr.select("td").get(2-index).text());
				 stock.setPrice(UITools.parseDouble(tr.select("td").get(4-index).text()));
				 stock.getTiersAppIds().put(getName(), tr.select("td").get(1-index).text());
				 stock.setQte(Integer.parseInt(tr.select("td").get(3-index).text()));
			
			t.getItems().add(stock);
		}
		
		
		return t;
	}

	
	
	private void init()
	{
		
		if(client==null) {
			
			client = URLTools.newClient();
			try {
				s = RequestBuilder.build()
						  .method(METHOD.POST)
						  .url(BASE_URL+"/en/authentication")
						  .setClient(client)
						  .addContent("email", getAuthenticator().getLogin())
						  .addContent("passwd",getAuthenticator().getPassword())
						  .addContent("back","history")
						  .addContent("SubmitLogin","")
						  .addHeader(":path", "/en/authentication")
						  .addHeader(":authority", "www.philibertnet.com")
						  .addHeader(":method", METHOD.POST.name())
						  .addHeader(URLTools.CONTENT_TYPE, "application/x-www-form-urlencoded")
						  .toHtml();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	
	@Override
	public List<String> listAuthenticationAttributes() {
		return AccountsManager.generateLoginPasswordsKeys();
	}


	@Override
	public String getName() {
		return "Philibert";
	}

}
