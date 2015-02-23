package project.cards.services.game;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import project.cards.objects.impl.Game;
import project.cards.objects.impl.Player;
import project.cards.services.game.durak.PlayerServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by omerpr on 23/01/2015.
 */
public abstract class GameService{

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static Integer sequence = 1;

    protected Map<String,Game> games = new HashMap<>();

	public abstract DeckService getDeckService();

	public abstract PlayerService getPlayerService();

	public abstract YackService<?> getYackService();

    protected abstract int getInitialCardsNum();
    protected abstract int getMaxPlayers();
    protected abstract int getMinimumPlayers();

	protected abstract void completeHand(String gId, int index);

	protected abstract boolean isGameOver(String gId);

	protected abstract boolean isPlayerStillInGame(String gId, String pId);

	public boolean isPlayerStillInGame(String gId, int index) {
		return isPlayerStillInGame(gId, getPlayerPId(gId, index));
	}

    public void addPlayer(String gId,String pId) {

        Game g = getGame(gId,true);

        g.addPlayerId(pId);
    }

    public void joinGame(String gId,String pId) {

	    if(getPlayerService().isPlayerExists(gId, pId)) {//player already in the game, do nothing.
		    return;
        }

        if(!isGameFull(gId)) {
            if(!isGameStarted(gId)) {
                addPlayer(gId, pId);
            }
            else {
                throw new RuntimeException("Game has already started.");
            }
        }
        else {
            throw new RuntimeException("Game is full.");
        }
    }

    public String createGame(String name) {
        String gId = "Game_"+(sequence++);
        games.put(gId,new Game(gId,name));
        return gId;
    }

    public void startGame(String gId) {

        Game g = getGame(gId,true);

        getDeckService().createDeck(gId);
	    getYackService().createYack(gId);
	    getFlowService().createFlow(gId);

        Player p;
        for(String pId : g.getPlayerIds()) {
            for(int i = 0;i<getInitialCardsNum();i++) {
                p = getPlayerService().getPlayer(gId, pId, true);

                p.addCardId(getDeckService().deal(gId));
            }
        }

	    g.setEnded(false);
	    g.setStarted(true);
    }

	public void endGame(String gId) {
		Game g = getGame(gId, true);
		g.setStarted(false);
		g.setEnded(true);

		for(String pId : g.getPlayerIds()) {
			PlayerServiceImpl.getInstance().resetPlayer(gId, pId);
		}

		getFlowService().removeFlow(gId);
		getYackService().removeYack(gId);
	}

    public List<String> getPlayersIds(String gId) {
        return getGame(gId,true).getPlayerIds();
    }

    public void removePlayer(String gId,String pId) {
        Game g = getGame(gId,false);
        if(null != g) {
            g.removePlayerId(pId);
            if(isGameEmpty(gId) ||g.getPlayerIds().size() <= getMinimumPlayers()) {
                removeGame(gId);
            }
        }
    }

    private void removeGame(String gId) {
        games.remove(gId);
    }

    public Game getGame(String gId,boolean thrower) {
        Game g = games.get(gId);
        if(thrower && null == g) {//TODO: throw GameDoesntExist.
            throw new RuntimeException("Game does not exist.");
        }
        return g;
    }

    public boolean isGameFull(String gId) {
        return getMaxPlayers() == getGame(gId,true).getPlayerIds().size();
    }

    public Boolean isGameStarted(String gId) {
        Game g = getGame(gId,true);
        return g.isStarted();
    }

	public Boolean isGameEnded(String gId) {
		Game g = getGame(gId, true);
		return g.isEnded();
	}

    public boolean isReadyToStart(String gId) {
        Game g = getGame(gId,true);
        if(g.getPlayerIds().size() <getMinimumPlayers()) {//if not enough players.
            return false;
        }

        Player p;
        for(String pId : g.getPlayerIds()) {
            p = getPlayerService().getPlayer(gId, pId, true);
            if(!p.isReady()) {//if at least one player is not ready..
                return false;
            }
        }

        //more than 1 player in the game & all players are ready.
        return true;
    }

	public int getNextPlayerPos(String gId, int playerPos) {
		int playerSize = getPlayersIds(gId).size();
		int nextPlayerPos = (playerPos + 1) % playerSize;
		while(!isPlayerStillInGame(gId, getPlayerPId(gId, nextPlayerPos))) {
			if(nextPlayerPos == playerPos) {//If we have completed a cycle.
				return playerPos;
			}
			nextPlayerPos = (nextPlayerPos + 1) % playerSize;
		}
		return nextPlayerPos;
	}

	public int getNextPlayerPos(String gId, String pId) {
		return getNextPlayerPos(gId, getPlayerPosIdx(gId, pId));
	}

    public int getPlayerPosIdx(String gId, String pId) {
        if(null == pId) return -1;

        Game g = getGame(gId,true);
        List<String> playerIds = g.getPlayerIds();
        for(int i = 0;i<playerIds.size();i++) {
            if(pId.equals(playerIds.get(i))) {
                return i;
            }
        }
        return -1;
    }

	public JsonArray getJsonAllGames() {//TODO: cach.
		JsonArray allGames = new JsonArray();
		String gId;
		Game g;
		for(Map.Entry<String, Game> e : games.entrySet()) {
			gId = e.getKey();
			g = games.get(gId);
			allGames.add(
					new JsonObject()
							.putString("name", g.getName())
							.putString("gId", gId)
							.putArray("players", new JsonArray(g.getPlayerIds()))
			);
		}
		return allGames;
	}

	public JsonArray getJsonGamePlayers(String gId) {
		JsonArray gamePlayers = new JsonArray();
		Game g = getGame(gId, true);

		List<String> playerIds = g.getPlayerIds();
		for(String pId : playerIds) {
			gamePlayers.add(getPlayerService().getJsonPlayer(gId, pId));
		}

		return gamePlayers;
	}

    public JsonObject getJsonGameInfo(String gId) {
        boolean isGameStarted = isGameStarted(gId);
	    boolean isGameEnded = isGameEnded(gId);
	    JsonObject gameInfo = new JsonObject()
                .putBoolean("isStarted", isGameStarted)
		        .putBoolean("isEnded", isGameEnded)
			    .putArray("players", getJsonGamePlayers(gId));

        if(isGameStarted) {
	        populateJsonGameStartedInfo(gameInfo, gId);
        }
	    if(isGameEnded) {
		    populateJsonGameEndedInfo(gameInfo, gId);
	    }

        return gameInfo;
    }

	protected void populateJsonGameStartedInfo(JsonObject gameInfo, String gId) {
		//Override me for more.. :)
		gameInfo
				.putObject("deckInfo", getDeckService().getJsonDeck(gId))
				.putObject("flowInfo", getFlowService().getJsonFlow(gId))
				.putArray("yackInfo", getYackService().getJsonYack(gId));
	}

	protected void populateJsonGameEndedInfo(JsonObject gameInfo, String gId) {
		//Override me for more.. :)
	}



    public boolean isGameEmpty(String gId) {
        return getGame(gId,true).getPlayerIds().size() == 0;
    }

    public String getPlayerPId(String gId, int index) {
        if(index < 0) return null;

        Game g = getGame(gId,true);
        return g.getPlayerIds().get(index);
    }

	public abstract FlowService<?, ?> getFlowService();

	public abstract void handleAction(String gId, String pId, JsonObject jsonParams);

}
