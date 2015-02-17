package project.cards.services.game.durak;

import com.hazelcast.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import project.cards.objects.Flow;
import project.cards.objects.durak.DurakAction;
import project.cards.objects.durak.DurakFlow;
import project.cards.objects.impl.Card;
import project.cards.services.game.FlowService;
import project.cards.services.game.PlayerService;

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


		if(PlayerServiceImpl.getInstance().isPlayerInGame(gId, initiatorPId)) {//If the player initiated this action is part of the game.
			int initiatorPlayerPosIndex = GameServiceImpl.getInstance().getPlayerPosIdx(gId, initiatorPId);
			int currentTurnIndex = f.getCurrentPlayerTurnIndex();
			String targetPId = getCurrentDefenderPId(gId);
			String attackingCardId = durakAction.getAttackCardId();
			switch(durakAction.getType()) {
				case DurakAction.Types.ATTACK:

					/**
					 * The Attack action is valid when :
					 *
					 *  its the attacker's turn :) (which means no cards are on the floor yet)
					 *  OR
					 *      everyone can put cards AND the attacker is not the target( OR he is the target but he is allowed to keep the flow going..(Transferring rule!) ).
					 *      and
					 *      attacking card is not empty.
					 *      and
					 *      attacking card's rank is on the floor(yack)
					 *      and
					 *      the floor(yack) size did not reach limit
					 *      and
					 *      the attacked player has more cards than the floor

					 */
					return initiatorPlayerPosIndex == currentTurnIndex
							||
							(-1 == currentTurnIndex
									&&
									(!initiatorPId.equals(targetPId) || (YackServiceImpl.getInstance().getFrontCardsSize(gId) == 0 && !f.isDefenderCollecting()))
									&&
									!StringUtil.isNullOrEmpty(attackingCardId)
									&&
									YackServiceImpl.getInstance().isRankExists(gId, Card.getById(attackingCardId).getRank())
									&&
									!isMaxAttackExceeded(gId));
				case DurakAction.Types.ANSWER:

					String answeringCardId = durakAction.getAnswerCardId();
					Card answeringCard = Card.getById(answeringCardId);
					Card attackingCard = Card.getById(attackingCardId);

					/**
					 * The Answer action is valid when :
					 *
					 *      part 1 (mandatoryCond):
					 *      the initiator of the attack is targeted.
					 *      and
					 *      attacking card is not empty
					 *      and
					 *      answering card is not empty.
					 */

					boolean mandatoryCond = initiatorPId.equals(targetPId)
							&&
							!StringUtil.isNullOrEmpty(attackingCardId)
							&&
							!StringUtil.isNullOrEmpty(answeringCardId);

					if(mandatoryCond) {
						/**
						 * part 2
						 * if same suit, validate the answer is stronger.
						 * otherwise, check if the player answered with a strong card.
						 */
						boolean isStrongerAnswer = answeringCard.compareTo(attackingCard) > 0;
						boolean isAnsweredWithStrongCard = DeckServiceImpl.getInstance().isStrongCard(gId, answeringCard);
						if(answeringCard.getSuit() == attackingCard.getSuit()) {
							return isStrongerAnswer;
						}
						return isAnsweredWithStrongCard;
					}
					return false;

				case DurakAction.Types.COLLECT:
					return initiatorPId.equals(targetPId)
							&&
							isCollectingPossible(gId);

				case DurakAction.Types.DONE_ATTACKING:
					logger.info("HI! AND WTF?");
					return !initiatorPId.equals(targetPId)
							&&
							isDoneAttackingPossible(gId);
				default:
					return false;
			}
		} else {
			return false;
		}
	}

	private boolean isCollectingPossible(String gId) {
		return YackServiceImpl.getInstance().getYackSize(gId) > 0
				&&
				YackServiceImpl.getInstance().getBackCardsSize(gId) > YackServiceImpl.getInstance().getFrontCardsSize(gId);
	}

	private Boolean isDoneAttackingPossible(String gId) {
		return getFlow(gId).isDefenderCollecting() || isDefenseDone(gId);
	}

	private boolean isAttackOver(String gId) {
		return true;
	}

	private boolean isMaxAttackExceeded(String gId) {
		String targetPId = getCurrentDefenderPId(gId);
		int currentYackSize = YackServiceImpl.getInstance().getYackSize(gId);
		return YackServiceImpl.MAX_YACK_SIZE == currentYackSize
				||
				PlayerServiceImpl.getInstance().getCardsSize(gId, targetPId) == currentYackSize;
	}

	@Override
	protected void doAction(String gId, DurakAction durakAction) {

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
				} else if(flow.isDefenderCollecting() && isMaxAttackExceeded(gId)) {//defender requested collection and no room for further more cards.
					endTurnAfterCollection(gId);
				} else {//just incase, remove the player from done attacking incase he is in there.
					flow.getDoneAttackingUsers().remove(PlayerServiceImpl.getInstance().getPlayerUserName(durakAction.getInitiatorPId()));
				}
				break;
			case DurakAction.Types.ANSWER:
				String answeringCardId = durakAction.getAnswerCardId();
				PlayerServiceImpl.getInstance().removeCardId(gId, durakAction.getInitiatorPId(), answeringCardId, true);
				YackServiceImpl.getInstance().addFrontCardId(gId, durakAction.getAttackCardId(), answeringCardId);

				if(isDefenseDone(gId) && isMaxAttackExceeded(gId)) {//defender answered all cards on floor, and no room for further more cards.
					endTurnWithoutCollection(gId);
				}
				break;
			case DurakAction.Types.COLLECT:
				if(isMaxAttackExceeded(gId)) {//defender requested collection and no room for further more cards.
					endTurnAfterCollection(gId);
				} else {
					flow.resetDoneAttackingUsers();
					flow.setDefenderCollecting(true);
				}
				break;
			case DurakAction.Types.DONE_ATTACKING:
				toggleDoneAttackingUser(flow, PlayerServiceImpl.getInstance().getPlayerUserName(durakAction.getInitiatorPId()));
				if(GameServiceImpl.getInstance().getPlayersIds(gId).size() == flow.getDoneAttackingUsers().size() + 1) {//all players are done attacking.
					if(flow.isDefenderCollecting()) {
						endTurnAfterCollection(gId);
					} else {
						endTurnWithoutCollection(gId);
					}
				}
				break;
		}
	}

	private void toggleDoneAttackingUser(DurakFlow flow, String pId) {
		String userName = PlayerServiceImpl.getInstance().getPlayerUserName(pId);
		flow.toggleDoneAttackingUser(userName);
	}

	private boolean isDefenseDone(String gId) {
		return YackServiceImpl.getInstance().getBackCardsSize(gId) > 0
				&& YackServiceImpl.getInstance().getFrontCardsSize(gId) == YackServiceImpl.getInstance().getBackCardsSize(gId);
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

		initializeFlow(gId);
	}

	private void endTurnWithoutCollection(String gId) {
		DurakFlow flow = getFlow(gId);

		//clear the yack(floor)
		YackServiceImpl.getInstance().clearCards(gId);

		completeHands(gId);


		int currentDefenderIndex = flow.getCurrentDefenderIndex();

		//if game attacked p2, and p2 finished answering, p2 should attack p3.
		increaseFlow(gId, currentDefenderIndex, GameServiceImpl.getInstance().getNextPlayerPos(gId, currentDefenderIndex));
		initializeFlow(gId);
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
	protected void initializeFlow(String gId) {
		DurakFlow flow = getFlow(gId);

		flow.resetDoneAttackingUsers();
		flow.setDefenderCollecting(false);
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
		while(turnIndex != turnInitiatorIndex) {
			if(turnIndex != defenderIndex) {
				GameServiceImpl.getInstance().completeHand(gId, turnIndex);
			}
			turnIndex = GameServiceImpl.getInstance().getNextPlayerPos(gId, turnIndex);
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
