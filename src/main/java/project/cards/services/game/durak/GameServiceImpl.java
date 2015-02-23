package project.cards.services.game.durak;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import project.cards.objects.durak.DurakAction;
import project.cards.objects.durak.DurakYack;
import project.cards.services.game.DeckService;
import project.cards.services.game.GameService;
import project.cards.services.game.PlayerService;
import project.cards.services.game.YackService;

import java.util.*;

/**
 * Created by omerpr on 23/01/2015.
 */
public class GameServiceImpl extends GameService {

	private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);
	private static GameServiceImpl instance = null;

	private GameServiceImpl() {
	}

	public static GameServiceImpl getInstance() {
		if(null != instance) {
			return instance;
		}
		synchronized(GameServiceImpl.class) {
			if(null == instance) {
				instance = new GameServiceImpl();
			}
		}

		return instance;
	}

	@Override
	public PlayerService getPlayerService() {
		return PlayerServiceImpl.getInstance();
	}

	@Override
	public FlowServiceImpl getFlowService() {
		return FlowServiceImpl.getInstance();
	}

	@Override
	public DeckService getDeckService() {
		return DeckServiceImpl.getInstance();
	}

	@Override
	public YackService<DurakYack> getYackService() {
		return YackServiceImpl.getInstance();
	}

	@Override
	public void startGame(String gId) {
		super.startGame(gId);
	}

	@Override
	protected void completeHand(String gId, int index) {
		logger.info("completeHand start.");

		int initialCardsNumber = getInitialCardsNum();
		String pId = getPlayerPId(gId, index);

		while(!DeckServiceImpl.getInstance().isEmpty(gId) && PlayerServiceImpl.getInstance().getCardsSize(gId, pId) < initialCardsNumber) {
			PlayerServiceImpl.getInstance().addCardId(gId, pId, DeckServiceImpl.getInstance().deal(gId), false);
		}

		logger.info("completeHand end.");
	}

	@Override
	protected boolean isGameOver(String gId) {
		return getLosers(gId).size() == 1;
	}

	@Override
	protected boolean isPlayerStillInGame(String gId, String pId) {
		return !getDeckService().isEmpty(gId) || getPlayerService().getCardsSize(gId, pId) > 0;
	}

	public Set<String> getLosers(String gId) {
		Set<String> losers = new HashSet<>(2);
		for(String pId : getPlayersIds(gId)) {
			if(isPlayerStillInGame(gId, pId)) {
				losers.add(getPlayerService().getPlayerUserName(pId));
			}
		}
		return losers;
	}

	@Override
	protected void populateJsonGameEndedInfo(JsonObject gameInfo, String gId) {
		super.populateJsonGameEndedInfo(gameInfo, gId);
		gameInfo.putString("loser", getLosers(gId).iterator().next());
	}

	@Override
	public void handleAction(String gId, String pId, JsonObject jsonParams) {
		String actionType = jsonParams.getString("actionType");

		DurakAction durakAction = new DurakAction(pId, actionType);

		JsonObject attackCard;

		switch(actionType) {
			case DurakAction.Types.ATTACK:
				attackCard = jsonParams.getObject("attackCard");
				durakAction.setAttackCardId(DeckServiceImpl.getInstance().getCardId(attackCard));
				break;
			case DurakAction.Types.ANSWER:
				attackCard = jsonParams.getObject("attackCard");
				JsonObject answerCard = jsonParams.getObject("answerCard");
				durakAction.setAttackCardId(DeckServiceImpl.getInstance().getCardId(attackCard));
				durakAction.setAnswerCardId(DeckServiceImpl.getInstance().getCardId(answerCard));
				break;
			case DurakAction.Types.COLLECT:
			case DurakAction.Types.DONE_ATTACKING:
				break;
			default:
				logger.info("TODO: handle me.. no attack type specified.");
				return;
		}

		getFlowService().requestAction(gId, durakAction);
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
