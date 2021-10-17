package org.magic.api.beans;

import org.magic.api.beans.enums.EnumItems;
import org.magic.api.beans.shop.Product;

public class MTGSealedProduct extends Product{

	private static final long serialVersionUID = 1L;

	public enum EXTRA { SET, DRAFT, COLLECTOR,THEME,GIFT,VIP}
	
	private EnumItems type;
	private String lang;
	private int num;
	private EXTRA extra;
	
	
	public EXTRA getExtra() {
		return extra;
	}
	public void setExtra(EXTRA extra) {
		this.extra = extra;
	}
	
	
	public EnumItems getType() {
		return type;
	}
	public void setType(EnumItems type) {
		this.type = type;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}

	@Override
	public String toString() {
		return getLang()+"-" + (getExtra()!=null?getExtra()+"-":"")+ getNum();
	}
	
	
}
