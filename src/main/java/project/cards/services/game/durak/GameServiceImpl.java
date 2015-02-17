package project.cards.services.game.durak;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import project.cards.objects.durak.DurakYack;
import project.cards.services.game.DeckService;
import project.cards.services.game.GameService;
import project.cards.services.game.PlayerService;
import project.cards.services.game.YackService;

import java.util.List;

/**
 * Created by omerpr on 23/01/2015.
 */
public class GameServiceImpl extends GameService{

    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);
    private static GameServiceImpl instance = null;

    private GameServiceImpl() {

    }

    public static GameServiceImpl getInstance() {
        if(null != instance) {
            return instance;
        }
        synchronized (GameServiceImpl.class) {
            if(null == instance) {
                instance = new GameServiceImpl();
            }
        }

        return instance;
    }

	@Override
	protected void completeHand(String gId, int index) {
		int initialCardsNumber = getInitialCardsNum();
		String pId = getPlayerPId(gId, index);

		while(!DeckServiceImpl.getInstance().isEmpty(gId) && PlayerServiceImpl.getInstance().getCardsSize(gId, pId) < initialCardsNumber) {
			PlayerServiceImpl.getInstance().addCardId(gId, pId, DeckServiceImpl.getInstance().deal(gId), false);
		}
	}

    @Override
    public DeckService getDeckService() {
        return DeckServiceImpl.getInstance();
    }

    @Override
    public PlayerService getPlayerService() {
        return PlayerServiceImpl.getInstance();
    }


    @Override
    protected YackService<DurakYack> getYackService() {
        return YackServiceImpl.getInstance();
    }

    @Override
    public FlowServiceImpl getFlowService() {
        return FlowServiceImpl.getInstance();
    }

    @Override
    protected int getInitialCardsNum() {
        return 6;
    }

    @Override
    protected int getMaxPlayers() {
        return 4;
    }

    @Override
    protected int getMinimumPlayers() {
        return 2;
    }

}
