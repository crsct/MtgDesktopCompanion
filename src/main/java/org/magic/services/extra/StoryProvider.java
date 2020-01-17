package org.magic.services.extra;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.magic.api.beans.MTGStory;
import org.magic.services.MTGLogger;
import org.magic.tools.ImageTools;
import org.magic.tools.URLTools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class StoryProvider {

	private Logger logger = MTGLogger.getLogger(this.getClass());
	private Locale local;
	private int offset = 0;
	private String baseURI = "https://magic.wizards.com";
	// &fromDate=&toDate=&word=

	public StoryProvider(Locale local) {
		this.local = local;
	}

	

	public int getOffset() {
		return offset;
	}

	public List<MTGStory> next() throws IOException {
		String url = baseURI + "/" + local.getLanguage() + "/section-articles-see-more-ajax?l=" + "en"+ "&sort=DESC&f=13961&offset=" + (offset++);
		List<MTGStory> list = new ArrayList<>();
		JsonElement el = URLTools.extractJson(url);
		JsonArray arr = el.getAsJsonObject().get("data").getAsJsonArray();

		for (int i = 0; i < arr.size(); i++) {
			JsonElement e = arr.get(i);
			String finale = StringEscapeUtils.unescapeJava(e.toString());
			Document d = Jsoup.parse(finale);
			
				MTGStory story = new MTGStory();
				story.setTitle(d.select("div.title h3").html());
				story.setAuthor(StringEscapeUtils.unescapeHtml3(d.select("span.author").html()));
				story.setDescription(StringEscapeUtils.unescapeHtml3(d.select("div.description").html()));
				story.setUrl(new URL(baseURI + d.select("a").first().attr("href")));
				story.setDate(d.select("span.date").text());
			try {
				String bgImage = d.select("div.image").attr("style");
				story.setIcon(loadPics(new URL(bgImage.substring(bgImage.indexOf("url(") + 5, bgImage.indexOf("');")))));
				
			} catch (Exception e2) {
				logger.error("Error loading story ", e2);
			}
			list.add(story);
		}

		return list;
	}

	private Image loadPics(URL url) {
		Image tmp;
		try {
			tmp = URLTools.extractImage(url).getScaledInstance(200, 110, Image.SCALE_SMOOTH);
			return tmp;
		} catch (IOException e) {
			logger.error("could not load" + url, e);
		}
		return null;

	}

}
