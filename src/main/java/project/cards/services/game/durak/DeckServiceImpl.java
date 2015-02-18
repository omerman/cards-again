package project.cards.services.game.durak;

import org.vertx.java.core.json.JsonObject;
import project.cards.objects.impl.Card;
import project.cards.objects.impl.Deck;
import project.cards.services.game.DeckService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by omerpr on 24/01/2015.
 */
public class DeckServiceImpl extends DeckService {

	private static DeckServiceImpl instance = null;
	private Map<String, Integer> gamesToStrongCardSuits;


	private DeckServiceImpl() {
		gamesToStrongCardSuits = new HashMap<>();
	}

	@Override
	public void createDeck(String gId) {
		super.createDeck(gId);
		String strongCardId = deal(gId);
		getDeck(gId).add(strongCardId);
		gamesToStrongCardSuits.put(gId, Card.getById(strongCardId).getSuit());
	}

	@Override
	public String deal(String gId) {
		System.out.println("DECK SIZE BEFORE " + getFakeSize(gId));
		String returnnn = super.deal(gId);
		System.out.println("DECK SIZE AFTER " + getFakeSize(gId));
		return returnnn;
	}

	public static DeckServiceImpl getInstance() {
		if(null != instance) {
			return instance;
		}
		synchronized(DeckServiceImpl.class) {
			if(null == instance) {
				instance = new DeckServiceImpl();
			}
		}

		return instance;
	}

	public JsonObject getJsonStrongCard(String gId) {
		Deck d = getDeck(gId);
		return this.getJsonCard(d.get(d.size() - 1));
	}

	public boolean isStrongCard(String gId, Card card) {
		return gamesToStrongCardSuits.get(gId) == card.getSuit();
	}

	@Override
	public JsonObject getJsonDeck(String gId) {
		return new JsonObject().putNumber("size", getFakeSize(gId))
				.putObject("strongCard", getJsonStrongCard(gId));
	}

}
