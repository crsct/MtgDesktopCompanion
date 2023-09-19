package org.magic.api.beans.messages;

import java.util.List;
import java.util.stream.Collectors;

import org.magic.api.beans.MagicCardStock;
import org.magic.api.beans.abstracts.AbstractMessage;

public class SearchAnswerMessage extends AbstractMessage {

	private static final long serialVersionUID = 1L;
	private SearchMessage searchQuery;
	private List<MagicCardStock> resultItems;

	
	public SearchMessage getSearchQuery() {
		return searchQuery;
	}
	
	public List<MagicCardStock> getResultItems() {
		return resultItems;
	}
	
	public SearchAnswerMessage(SearchMessage msg,List<MagicCardStock> ret) {
		setTypeMessage(MSG_TYPE.ANSWER);
		this.searchQuery = msg;
		this.resultItems = ret;
		setMessage("I have ! "+ ret.stream().map(mcs->mcs.getProduct().getName() + " " + mcs.getQte() + " " + mcs.getLanguage() + " " + mcs.getCondition()).collect(Collectors.joining(System.lineSeparator())));
	}

}
