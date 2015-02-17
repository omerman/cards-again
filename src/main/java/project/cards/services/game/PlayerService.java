package project.cards.services.game;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import project.cards.objects.impl.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by omerpr on 24/01/2015.
 */
public abstract class PlayerService {

    protected Map<String,Map<String,Player>> players = new HashMap<>();
    protected Map<String,String> playerNames = new HashMap<>();

    public abstract DeckService getDeckService();

    public Player getOrCreatePlayer(String gId,String pId) {
        Player p = getPlayer(gId,pId,false);
        if(null == p) {
            p = new Player(gId,pId);
            Map<String,Player> playerProfiles = new HashMap<>();
            playerProfiles.put(gId,p);
            players.put(pId,playerProfiles);
        }
        return p;
    }

    public Player createPlayer(String gId,String pId,boolean thrower) {

        Player p = getPlayer(gId, pId, false);
        if (null != p) {
            if(thrower) {//TODO: throw player already exists!!!
                return null;
            }
            Map<String,Player> playerProfiles = getPlayerProfiles(pId,false);//cannot be null if p was originally not null.
            playerProfiles.remove(gId);
        }

        return getOrCreatePlayer(gId,pId);//doesnt exist for sure.
    }

    public Set<String> removePlayer(String pId) {
        Map<String,Player> playerProfiles = getPlayerProfiles(pId,false);

        playerNames.remove(pId);
        players.remove(pId);
        if(null != playerProfiles) {
            return playerProfiles.keySet();
        }

        return null;
    }

    public Player getPlayer(String gId,String pId,boolean thrower) {
        Map<String,Player> playerProfiles = getPlayerProfiles(pId,thrower);
        Player p = null;
        if(null != playerProfiles) {
            p = playerProfiles.get(gId);
            if(null == p && thrower) {//TODO: throw PlayerDoesntExist.
                return null;
            }
        }
        return p;
    }

    public Map<String,Player> getPlayerProfiles(String pId,boolean thrower) {
        Map<String,Player> playerProfiles = players.get(pId);
        if(thrower && null == playerProfiles) {//TODO: throw PlayerDoesntExist.
            return null;
        }
        return playerProfiles;
    }

    public JsonArray getJsonPlayerGames(String pId) {
        Map<String,Player> playerProfiles = players.get(pId);

        JsonArray jsonPlayerGames = new JsonArray();
        if(null != playerProfiles) {

            for (String gId: playerProfiles.keySet()) {
                jsonPlayerGames.addString(gId);
            }
        }

        return jsonPlayerGames;
    }

    public JsonArray getJsonPlayerCards(String gId,String pId) {
        Player p = getPlayer(gId,pId,true);
        JsonArray playerCards = new JsonArray();
        for(String cId : p.getCardIds()) {
            playerCards.add(getDeckService().getJsonCard(cId));
        }

        return playerCards;
    }

    public JsonObject getJsonPlayer(String gId,String pId) {
        Player p = getPlayer(gId,pId,true);
        return new JsonObject()
                .putString("userName",getPlayerUserName(pId))
                .putNumber("cardsNum", p.getCardIds().size());
    }

    public void setReady(String gId, String pId, boolean isReady) {
        Player p = getPlayer(gId,pId,true);
        p.setReady(isReady);
    }

    public boolean isPlayerInGame(String gId, String pId) {
        return null != getPlayer(gId,pId,false);
    }

    public boolean isHavingCardId(String gId,String pId,String cId) {
        return getPlayer(gId,pId,true).getCardIds().contains(cId);
    }

    public void setPlayerUserName(String pId, String userName) {
        playerNames.put(pId,userName);
    }

    public String getPlayerUserName(String pId) {
        return playerNames.get(pId);
    }

    public boolean containsCard(String gId,String pId, String cardId) {
        return getPlayer(gId,pId,true).getCardIds().contains(cardId);
    }

    public boolean removeCardId(String gId, String pId, String cardId, boolean thrower) {
        Player p = getPlayer(gId,pId,true);

        boolean cardExists = p.getCardIds().remove(cardId);
        if(!cardExists && thrower) {
            throw new RuntimeException("Card with id " + cardId + " doesn't exist.");
        }
        return cardExists;
    }

    public boolean addCardId(String gId, String pId, String cardId, boolean thrower) {
        Player p = getPlayer(gId,pId,true);

        boolean cardDoesntExist = p.getCardIds().add(cardId);
        if(!cardDoesntExist && thrower) {
            throw new RuntimeException("Card with id " + cardId + " already exists.");
        }

        return cardDoesntExist;
    }

    public int getCardsSize(String gId, String pId) {
        return getPlayer(gId,pId,true).getCardIds().size();
    }

	public void addCardsIds(String gId, String pId, List<String> cardsIds, boolean thrower) {
		for(String cardId : cardsIds) {
			addCardId(gId, pId, cardId, thrower);
		}
	}
}
