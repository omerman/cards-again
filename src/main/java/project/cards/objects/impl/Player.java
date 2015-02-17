package project.cards.objects.impl;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by omerpr on 23/01/2015.
 */
public class Player {

	private String pId;
	private String gId;
	private Set<String> cardIds;
	private boolean isReady;


	public Player(String gId, String pId) {
		setpId(pId);
		setgId(gId);
		setCardIds(new HashSet<String>());
		setReady(false);
	}

	public void addCardId(String cardId) {
		cardIds.add(cardId);
	}

	public void removeCardId(String cardId) {
		cardIds.remove(cardId);
	}

	public String getpId() {
		return pId;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

	public void setCardIds(Set<String> cardIds) {
		this.cardIds = cardIds;
	}

	public Set<String> getCardIds() {
		return cardIds;
	}

	public String getgId() {
		return gId;
	}

	public void setgId(String gId) {
		this.gId = gId;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
}
