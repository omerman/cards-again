package project.cards.services.game.durak;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import project.cards.objects.Yack;
import project.cards.objects.durak.DurakYack;
import project.cards.objects.durak.DurakYackPair;
import project.cards.objects.impl.Card;
import project.cards.services.game.DeckService;
import project.cards.services.game.YackService;

import java.util.List;

/**
 * Created by omerpr on 06/02/2015.
 */
public class YackServiceImpl extends YackService<DurakYack>{

    public static int MAX_YACK_SIZE = 6;
    private static YackServiceImpl instance = null;

    public static YackServiceImpl getInstance() {
        if(null != instance) {
            return instance;
        }
        synchronized (YackServiceImpl.class) {
            if(null == instance) {
                instance = new YackServiceImpl();
            }
        }

        return instance;
    }

    public boolean isRankExists(String gId,int rank) {
        List<String> cardIdsList = getCards(gId);
        for(String cardId : cardIdsList) {
            if(Card.getById(cardId).getRank() == rank) {
                return true;
            }
        }
        return false;
    }

    public void addBackCardId(String gId, String backCardId) {
        DurakYack y = getYack(gId);
        y.addBackCardId(backCardId);
    }

    public void addFrontCardId(String gId, String backCardId, String frontCardId) {
        DurakYack y = getYack(gId);
        y.addFrontCardId(backCardId,frontCardId);
    }

    @Override
    public void createYack(String gId) {
        this.yacksByGame.put(gId,new DurakYack());
    }

    @Override
    public JsonArray getJsonYack(String gId) {
        DurakYack y = getYack(gId);
        JsonArray jsonYack = new JsonArray();
        JsonObject jsonYackPair;
        for(DurakYackPair yackPair : y.getYack() ) {
            jsonYackPair = new JsonObject();

            jsonYackPair.putObject("backCard", DeckServiceImpl.getInstance().getJsonCard(yackPair.getBackCardId()));
            if(null != yackPair.getFrontCardId()) {
                jsonYackPair.putObject("frontCard",DeckServiceImpl.getInstance().getJsonCard(yackPair.getFrontCardId()));
            }
            jsonYack.add(jsonYackPair);
        }

        return jsonYack;
    }

	public int getBackCardsSize(String gId) {
		List<DurakYackPair> yackPairs = getYack(gId).getYack();
		return yackPairs.size();
	}

    public int getFrontCardsSize(String gId) {
        List<DurakYackPair> yackPairs = getYack(gId).getYack();
        int counterFrontCards = 0;
        for(DurakYackPair yackPair : yackPairs) {
            if(null != yackPair.getFrontCardId()) {
                counterFrontCards++;
            }
        }
        return counterFrontCards;
    }


}
