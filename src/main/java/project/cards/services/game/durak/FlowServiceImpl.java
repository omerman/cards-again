package project.cards.services.game.durak;

import com.hazelcast.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import project.cards.ApplicationServer;
import project.cards.objects.durak.DurakAction;
import project.cards.objects.durak.DurakFlow;
import project.cards.objects.impl.Card;
import project.cards.services.VertxService;
import project.cards.services.game.FlowService;

import java.util.ArrayList;

/**
 * Changing official rules :)
 *
 * TRANSFERRING RULE:
 *  Player is attacked by 1 or more cards(for the first time) with the same rank of course(since its the first attack)
 *  Player that has not yet responded, can decide to respond with a card of the same rank as he is attacked with and the flow will continue to next player(clockwise!)
 * example:
 *  player 1-> 6 of clubs
 *  defender -> 6 of diamonds( as an attack, not as an answer on the previous card)
 *
 * flow++(increasing the flow to next player to be attacked clockwise!)
 *
 * NOTE: technically, its possible to complete a cyclic flow if 4 players has a card with same rank and each one uses the "Transferring" rule.
 */

/**
 * Created by omerpr on 05/02/2015.
 */
public class FlowServiceImpl extends FlowService<DurakFlow, DurakAction> {


	private static final Logger logger = LoggerFactory.getLogger(FlowServiceImpl.class);
	private static FlowServiceImpl instance = null;

	public static FlowServiceImpl getInstance() {
		if(null != instance) {
			return instance;
		}
		synchronized(FlowServiceImpl.class) {
			if(null == instance) {
				instance = new FlowServiceImpl();
			}
		}

		return instance;
	}

	@Override
	public void createFlow(String gId) {
		DurakFlow flow = new DurakFlow(0);
		flow.setActionsTimerPeriod(6000);//TODO: customizable.
		flow.setDefenderTimerPeriod(30000);//TODO: customizable.
		flows.put(gId, flow);
	}

	public void setActionsTimer(String gId, Vertx vertx) {
		DurakFlow flow = getFlow(gId);
		vertx.cancelTimer(flow.getDefenderTimerID());
		flow.setActionsTimerID(vertx.setTimer(flow.getActionsTimerPeriod(), new Handler<Long>() {
			@Override
			public void handle(Long event) {
				//trigger clear yack.
				//set the defender as next attacker.
				//each player draws cards to 6.
			}
		}));
	}

	public void setDefenderTimer(String gId, Vertx vertx) {
		DurakFlow flow = getFlow(gId);
		vertx.cancelTimer(flow.getActionsTimerID());
		flow.setDefenderTimerID(vertx.setTimer(flow.getDefenderTimerPeriod(), new Handler<Long>() {
			@Override
			public void handle(Long event) {
				//trigger collect yack for defender.
				//set the defender as next attacker.
				//each player draws cards to 6.
			}
		}));
	}

	@Override
	public boolean isValidAction(String gId, DurakAction durakAction) {
		DurakFlow f = getFlow(gId);
		String initiatorPId = durakAction.getInitiatorPId();


		if(PlayerServiceImpl.getInstance().isPlayerExists(gId, initiatorPId)) {//If the player initiated this action is part of the game.
			String targetPId = getCurrentDefenderPId(gId);
			if(null != targetPId) {

				String attackingCardId;
				switch(durakAction.getType()) {
					case DurakAction.Types.ATTACK:
						int currentTurnIndex = f.getCurrentPlayerTurnIndex();
						int initiatorPlayerPosIndex = GameServiceImpl.getInstance().getPlayerPosIdx(gId, initiatorPId);
						attackingCardId = durakAction.getAttackCardId();
						//first validate attacker owns the card he attacks with.
						if(!PlayerServiceImpl.getInstance().isHavingCardId(gId, initiatorPId, attackingCardId)) {
							return false;
						}
						/**
						 mandatory:
						 1)attack card is not empty.
						 2)initiator is starting the turn OR the attack card exists on the floor.

						 if the initiator is not the target(some on attacks the defender)
						 return floor is not full for the defender to handle.

						 otherwise(transfering rule)
						 return floor is not full for the NEXT defender to handle.
						 */
						boolean mandatoryRequirements =
								!StringUtil.isNullOrEmpty(attackingCardId)
										&&
										(
												initiatorPlayerPosIndex == currentTurnIndex
														||
														YackServiceImpl.getInstance().isRankExists(gId, Card.getById(attackingCardId).getRank())
										);
						if(mandatoryRequirements) {
							if(!initiatorPId.equals(targetPId)) {
								return !isMaxAttackExceeded(gId, getCurrentDefenderPId(gId));
							} else {
								return !isMaxAttackExceeded(gId, GameServiceImpl.getInstance().getNextPlayerPId(gId, initiatorPId));
							}
						}
					case DurakAction.Types.ANSWER:
						attackingCardId = durakAction.getAttackCardId();
						String answeringCardId = durakAction.getAnswerCardId();
						Card answeringCard = Card.getById(answeringCardId);
						Card attackingCard = Card.getById(attackingCardId);

						//first validate defender owns the card he defends with.
						if(!PlayerServiceImpl.getInstance().isHavingCardId(gId, initiatorPId, answeringCardId)) {
							return false;
						}

						/**
						 * The Answer action is valid when :
						 *
						 *      the initiator of the attack is targeted(check that the defender actually is the defender....).
						 *      and
						 *      defender is not already collecting the cards
						 *      and
						 *      attacking card is not empty
						 *      and
						 *      answering card is not empty.
						 *      and
						 *      card is not already answered on!
						 *      (if same suit, validate the answer is stronger.
						 *      otherwise, check if the player answered with a strong card.)
						 */

						return initiatorPId.equals(targetPId)
								&&
								!f.isDefenderCollecting()
								&&
								!StringUtil.isNullOrEmpty(attackingCardId)
								&&
								!StringUtil.isNullOrEmpty(answeringCardId)
								&&
								!YackServiceImpl.getInstance().isBackCardAnswered(gId, attackingCardId, true)
								&&
								(answeringCard.getSuit() == attackingCard.getSuit()
										?answeringCard.compareTo(attackingCard) > 0
										:DeckServiceImpl.getInstance().isStrongCard(gId, answeringCard));

					case DurakAction.Types.COLLECT:
						return initiatorPId.equals(targetPId)
								&&
								isCollectingPossible(gId);

					case DurakAction.Types.DONE_ATTACKING:
						return !initiatorPId.equals(targetPId)
								&&
								GameServiceImpl.getInstance().isPlayerStillInGame(gId, initiatorPId)
								&&
								isDoneAttackingPossible(gId);
					default:
						return false;
				}
			}
		}
		return false;
	}

	private boolean isCollectingPossible(String gId) {
		return YackServiceImpl.getInstance().getYackSize(gId) > 0
				&&
				YackServiceImpl.getInstance().getBackCardsSize(gId) > YackServiceImpl.getInstance().getFrontCardsSize(gId);
	}

	private Boolean isDoneAttackingPossible(String gId) {
		return getFlow(gId).isDefenderCollecting() || isDefenseDone(gId);
	}

	private boolean isMaxAttackExceeded(String gId, String targetPId) {
		int currentYackSize = YackServiceImpl.getInstance().getYackSize(gId);
		return YackServiceImpl.MAX_YACK_SIZE == currentYackSize
				||
				PlayerServiceImpl.getInstance().getCardsSize(gId, targetPId) <= YackServiceImpl.getInstance().getUnAnsweredCards(gId);
	}

	@Override
	protected void doAction(final String gId, DurakAction durakAction) {

		DurakFlow flow = getFlow(gId);
		switch(durakAction.getType()) {
			case DurakAction.Types.ATTACK:
				String attackingCardId = durakAction.getAttackCardId();
				PlayerServiceImpl.getInstance().removeCardId(gId, durakAction.getInitiatorPId(), attackingCardId, true);
				YackServiceImpl.getInstance().addBackCardId(gId, attackingCardId);

				flow.setCurrentPlayerTurnIndex(-1);

				//Transferring rule
				if(getCurrentDefenderPId(gId).equals(durakAction.getInitiatorPId())) {
					increaseFlowAfterTransfering(gId);
				} else if(flow.isDefenderCollecting() && isMaxAttackExceeded(gId, getCurrentDefenderPId(gId))) {//defender requested collection and no room for further more cards.
					endTurnAfterCollection(gId, 3000);
				} else {//just incase, remove the player from done attacking incase he is in there.
					flow.getDoneAttackingUsers().remove(PlayerServiceImpl.getInstance().getPlayerUserName(durakAction.getInitiatorPId()));
				}
				break;
			case DurakAction.Types.ANSWER:
				String answeringCardId = durakAction.getAnswerCardId();
				PlayerServiceImpl.getInstance().removeCardId(gId, durakAction.getInitiatorPId(), answeringCardId, true);
				YackServiceImpl.getInstance().addFrontCardId(gId, durakAction.getAttackCardId(), answeringCardId);

				if(isDefenseDone(gId) && isMaxAttackExceeded(gId, getCurrentDefenderPId(gId))) {//defender answered all cards on floor, and no room for further more cards.
					endTurnWithoutCollection(gId, 3000);
				}
				break;
			case DurakAction.Types.COLLECT:
				if(isMaxAttackExceeded(gId, getCurrentDefenderPId(gId))) {//defender requested collection and no room for further more cards.
					endTurnAfterCollection(gId);
				} else {
					flow.resetDoneAttackingUsers();
					flow.setDefenderCollecting(true);
				}
				break;
			case DurakAction.Types.DONE_ATTACKING:
				toggleDoneAttackingUser(flow, durakAction.getInitiatorPId());
				if(GameServiceImpl.getInstance().getPlayersInGame(gId).size() == flow.getDoneAttackingUsers().size() + 1) {//all players are done attacking.
					if(flow.isDefenderCollecting()) {
						endTurnAfterCollection(gId);
					} else {
						endTurnWithoutCollection(gId);
					}
				}
				break;
		}
		//If game is over after this attack..
		if(GameServiceImpl.getInstance().isGameOver(gId)) {
			stopFlow(gId);
		}
	}

	@Override
	protected void stopFlow(final String gId) {
		resetFlow(gId);
		increaseFlow(gId, -1, -1);//set the flow impossible to be increased..
		VertxService.getVertx().setTimer(1500, new Handler<Long>() {
			@Override
			public void handle(Long event) {
				GameServiceImpl.getInstance().endGame(gId);
				ApplicationServer.getEventBusService().publishGameInfoUpdate(gId);//end the game.
			}
		});
	}


	private void toggleDoneAttackingUser(DurakFlow flow, String pId) {
		String userName = PlayerServiceImpl.getInstance().getPlayerUserName(pId);
		flow.toggleDoneAttackingUser(userName);
	}

	private boolean isDefenseDone(String gId) {
		return YackServiceImpl.getInstance().getBackCardsSize(gId) > 0
				&& YackServiceImpl.getInstance().getUnAnsweredCards(gId) == 0;
	}

	private void endTurnAfterCollection(final String gId, long delay) {
		VertxService.getVertx().setTimer(delay, new Handler<Long>() {
			@Override
			public void handle(Long event) {
				endTurnAfterCollection(gId);
				ApplicationServer.getEventBusService().publishGameInfoUpdate(gId);
			}
		});
	}

	private void endTurnAfterCollection(String gId) {
		DurakFlow flow = getFlow(gId);

		//collect the yack(floor)
		PlayerServiceImpl.getInstance().addCardsIds(gId,
				GameServiceImpl.getInstance().getPlayerPId(gId, flow.getCurrentDefenderIndex()),
				YackServiceImpl.getInstance().collectCards(gId), false);

		completeHands(gId);


		//if game attacked p2, and p2 collected, now p3 should attack p4.
		int nextPlayerIndex = GameServiceImpl.getInstance().getNextPlayerPos(gId, flow.getCurrentDefenderIndex());

		increaseFlow(gId, nextPlayerIndex, GameServiceImpl.getInstance().getNextPlayerPos(gId, nextPlayerIndex));

		initNextFlow(gId);
	}

	private void endTurnWithoutCollection(final String gId, long delay) {
		VertxService.getVertx().setTimer(delay, new Handler<Long>() {
			@Override
			public void handle(Long event) {
				endTurnWithoutCollection(gId);
				ApplicationServer.getEventBusService().publishGameInfoUpdate(gId);
			}
		});
	}

	private void endTurnWithoutCollection(String gId) {
		DurakFlow flow = getFlow(gId);

		//clear the yack(floor)
		YackServiceImpl.getInstance().clearCards(gId);

		completeHands(gId);

		int nextAttackerIndex = flow.getCurrentDefenderIndex();
		if(!GameServiceImpl.getInstance().isPlayerStillInGame(gId, nextAttackerIndex)) {
			nextAttackerIndex = GameServiceImpl.getInstance().getNextPlayerPos(gId, nextAttackerIndex);
		}

		//if game attacked p2, and p2 finished answering, p2 should attack p3.
		increaseFlow(gId, nextAttackerIndex, GameServiceImpl.getInstance().getNextPlayerPos(gId, nextAttackerIndex));
		initNextFlow(gId);
	}

	private void increaseFlow(String gId, int nextPlayerIndex, int nextDefenderIndex) {
		super.increaseFlow(gId, nextPlayerIndex);
		getFlow(gId).setCurrentDefenderIndex(nextDefenderIndex);
	}

	private void increaseFlowAfterTransfering(String gId) {

		DurakFlow flow = getFlow(gId);
		int currentDefenderIndex = flow.getCurrentDefenderIndex();
		//if game attacked p2,and p2 used transferring rule.
		increaseFlow(gId, flow.getCurrentPlayerTurnIndex(), GameServiceImpl.getInstance().getNextPlayerPos(gId, currentDefenderIndex));

	}

	@Override
	protected void resetFlow(String gId) {
		DurakFlow flow = getFlow(gId);

		flow.resetDoneAttackingUsers();
		flow.setDefenderCollecting(false);
	}

	@Override
	protected void initNextFlow(String gId) {
		resetFlow(gId);
	}

	/**
	 * @param gId first the initiator of the attack draws cards.
	 *            <p/>
	 *            last the defender draws cards.
	 */
	public void completeHands(String gId) {

		DurakFlow flow = getFlow(gId);

		int turnInitiatorIndex = flow.getTurnInitiatorIndex();
		int defenderIndex = flow.getCurrentDefenderIndex();

		GameServiceImpl.getInstance().completeHand(gId, turnInitiatorIndex);

		int turnIndex = GameServiceImpl.getInstance().getNextPlayerPos(gId, turnInitiatorIndex);
		if(turnInitiatorIndex != turnIndex) {
			int nextTurnIndex = turnIndex;
			do {
				if(nextTurnIndex != defenderIndex) {
					GameServiceImpl.getInstance().completeHand(gId, nextTurnIndex);
				}
				nextTurnIndex = GameServiceImpl.getInstance().getNextPlayerPos(gId, nextTurnIndex);
			}
			while(nextTurnIndex != turnIndex);
		}

		GameServiceImpl.getInstance().completeHand(gId, defenderIndex);
	}

	@Override
	public JsonObject getJsonFlow(String gId) {

		JsonObject jsonFlow = super.getJsonFlow(gId);
		DurakFlow flow = getFlow(gId);
		boolean isCollectingPossible = isCollectingPossible(gId);
		jsonFlow.putNumber("defenderPosIndex", flow.getCurrentDefenderIndex())
				.putBoolean("isCollectingPossible", isCollectingPossible)
				.putBoolean("isDoneAttackingPossible", isDoneAttackingPossible(gId))
				.putBoolean("isDefenderCollecting", flow.isDefenderCollecting())
				.putArray("doneAttackingUsers", new JsonArray(new ArrayList<>(flow.getDoneAttackingUsers())));

		return jsonFlow;
	}


	public String getCurrentDefenderPId(String gId) {
		return GameServiceImpl.getInstance().getPlayerPId(gId, getFlow(gId).getCurrentDefenderIndex());
	}
}
