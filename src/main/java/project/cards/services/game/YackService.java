package project.cards.services.game;

import org.vertx.java.core.json.JsonArray;
import project.cards.objects.Yack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by omerpr on 30/01/2015.
 */
public abstract class YackService<Y extends Yack> {
	protected Map<String, Y> yacksByGame = new HashMap<>();

	public abstract void createYack(String gId);

	public Y getYack(String gId) {
		return yacksByGame.get(gId);
	}

	public List<String> getCards(String gId) {
		Yack y = getYack(gId);
		List<String> cards = y.toList();
		return cards;
	}

	public List<String> collectCards(String gId) {
		try {
			return getCards(gId);
		} finally {
			clearCards(gId);
		}
	}

	public void clearCards(String gId) {
		getYack(gId).clear();
	}

	public int getYackSize(String gId) {
		return getYack(gId).size();
	}

	public abstract JsonArray getJsonYack(String gId);
}
