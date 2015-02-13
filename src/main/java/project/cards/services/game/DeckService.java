package project.cards.services.game;

import org.vertx.java.core.json.JsonObject;
import project.cards.objects.impl.Card;
import project.cards.objects.impl.Deck;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by omerpr on 30/01/2015.
 */
public abstract class DeckService {
    protected Map<String,Deck> decksByGame = new HashMap<>();
    protected Map<String,Integer> fakeSizesByGame = new HashMap<>();

    public String deal(String gId) {
        Deck d = getDeck(gId);

        if(null == d) {//TODO: throw DeckDoesntExist;
            return null;
        }

        Random rand = new Random();
        int randomInt = rand.nextInt(d.size());

        decreaseFakeSize(gId);
        return d.remove(randomInt);

    }

    protected void decreaseFakeSize(String gId) {

        int forFunSize = (getDeck(gId).size()+getFakeSize(gId))/2;

        switch (forFunSize) {
            case Deck.MAX_SIZE - 10:
                setFakeSize(gId, 25);
            case Deck.MAX_SIZE - 20:
                setFakeSize(gId, 20);
            case Deck.MAX_SIZE - 30:
                setFakeSize(gId, 12);
            default:
                fakeSizesByGame.put(gId,getFakeSize(gId)-1);
        }

    }

    protected Deck getDeck(String gId) {
        return decksByGame.get(gId);
    }

    public void createDeck(String gId) {
        Deck d = new Deck();
        decksByGame.put(gId,new Deck());
        fakeSizesByGame.put(gId,d.size());
    }

    protected void setFakeSize(String gId,int randomRange) {
        int realSize = getDeck(gId).size();
        Random rand = new Random();
        int randomInt = rand.nextInt(randomRange/2) + realSize/2 + randomRange/3;

        Integer prevFakeSize = fakeSizesByGame.get(gId);
        if(prevFakeSize <= randomInt) {//if prev fake size is smaller.. its kinda ugly.
            randomInt = prevFakeSize - 1;
        }
        fakeSizesByGame.put(gId,randomInt);
    }

    public JsonObject getJsonCard(String cardId) {
        Card c = Card.getById(cardId);
        return new JsonObject().putNumber("rank", c.getRank()).putNumber("suit", c.getSuit());
    }

    public String getCardId(JsonObject card) {
        return Card.getId(card.getInteger("suit"),card.getInteger("rank"));
    }

    public int getFakeSize(String gId) {
        return fakeSizesByGame.get(gId);
    }

}
