package org.magic.api.pictures.impl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicEdition;
import org.magic.api.interfaces.MTGPicturesCache;
import org.magic.api.interfaces.abstracts.AbstractPicturesProvider;
import org.magic.services.MTGControler;
import org.magic.tools.InstallCert;
import org.magic.tools.URLTools;

public class MagicCardInfoPicturesProvider extends AbstractPicturesProvider {

	private static final String WEBSITE = "WEBSITE";
	private static final String LOAD_CERTIFICATE = "LOAD_CERTIFICATE";

	@Override
	public STATUT getStatut() {
		return STATUT.DEPRECATED;
	}


	public MagicCardInfoPicturesProvider() {
		super();

		if(getBoolean(LOAD_CERTIFICATE))
		{
			try {
				InstallCert.installCert("magiccards.info");
				setProperty(LOAD_CERTIFICATE, "false");
			} catch (Exception e1) {
				logger.error(e1);
			}
		}
	
	}

	@Override
	public BufferedImage getPicture(MagicCard mc, MagicEdition ed) throws IOException {

		if (MTGControler.getInstance().getEnabled(MTGPicturesCache.class).getPic(mc, ed) != null) {
			return resizeCard(MTGControler.getInstance().getEnabled(MTGPicturesCache.class).getPic(mc, ed), newW, newH);
		}

		if (ed == null)
			ed = mc.getCurrentSet();

		String infocode = ed.getMagicCardsInfoCode();

		if (infocode == null)
			infocode = mc.getCurrentSet().getId().toLowerCase();

		URL url;

		if (mc.getMciNumber() != null) {
			if (mc.getMciNumber().contains("/")) {
				String mcinumber = mc.getMciNumber().substring(mc.getMciNumber().lastIndexOf('/')).replaceAll(".html",
						"");
				url = new URL(
						getString(WEBSITE) + "/" + getString("LANG") + "/" + infocode + "/" + mcinumber + ".jpg");
			} else {
				url = new URL(getString(WEBSITE) + "/" + getString("LANG") + "/" + infocode + "/" + mc.getMciNumber()
						+ ".jpg");
			}
		} else {
			url = new URL(getString(WEBSITE) + "/" + getString("LANG") + "/" + infocode + "/"
					+ mc.getCurrentSet().getNumber().replaceAll("a", "").replaceAll("b", "") + ".jpg");
		}
		logger.debug("Get card pic from " + url);

		Image img = null;

		img = URLTools.extractImage(url).getScaledInstance(newW, newH, BufferedImage.SCALE_SMOOTH);
		BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null),BufferedImage.TYPE_INT_RGB);

		Graphics g = bufferedImage.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();

		MTGControler.getInstance().getEnabled(MTGPicturesCache.class).put(bufferedImage, mc, ed);

		return resizeCard(bufferedImage, newW, newH);
	}

	@Override
	public BufferedImage getSetLogo(String set, String rarity) throws IOException {
		URL url = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=" + set
				+ "&size=medium&rarity=" + rarity.substring(0, 1));
		return ImageIO.read(url);
	}

	@Override
	public String getName() {
		return "MagicCardInfo";
	}

	@Override
	public BufferedImage extractPicture(MagicCard mc) throws IOException {
		return getPicture(mc, null).getSubimage(15, 34, 184, 132);
	}

	@Override
	public void initDefault() {
		super.initDefault();
		setProperty(WEBSITE, "https://magiccards.info/scans/");
		setProperty("LANG", "en");
		
		setProperty(LOAD_CERTIFICATE, "true");
		
	}


}
