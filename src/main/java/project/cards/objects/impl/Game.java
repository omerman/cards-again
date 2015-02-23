package project.cards.objects.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by omerpr on 23/01/2015.
 */
public class Game {

	private String gId;
	private boolean isStarted;
	private List<String> playerIds;
	private String name;
	private boolean isEnded;

	public Game(String gId, String name) {

		setgId(gId);
		setName(name);
		setStarted(false);
		setEnded(false);
		setPlayerIds(new ArrayList<String>());

	}

	public void addPlayerId(String pId) {
		if(!playerIds.contains(pId)) {
			playerIds.add(pId);
		}
	}

	public void removePlayerId(String pId) {
		playerIds.remove(pId);
	}

	public void setPlayerIds(List<String> playerIds) {
		this.playerIds = playerIds;
	}

	public List<String> getPlayerIds() {
		return playerIds;
	}

	public String getgId() {
		return gId;
	}

	public void setgId(String gId) {
		this.gId = gId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	public void setEnded(boolean ended) {
		this.isEnded = ended;
	}

	public boolean isEnded() {
		return isEnded;
	}
}
