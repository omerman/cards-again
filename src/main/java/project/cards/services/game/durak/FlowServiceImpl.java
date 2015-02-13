package project.cards.services.game.durak;

import com.hazelcast.util.StringUtil;
import org.vertx.java.core.json.JsonObject;
import project.cards.objects.Flow;
import project.cards.objects.durak.DurakAction;
import project.cards.objects.durak.DurakFlow;
import project.cards.objects.impl.Card;
import project.cards.services.game.FlowService;
import project.cards.services.game.PlayerService;

/**
 * Created by omerpr on 05/02/2015.
 */
public class FlowServiceImpl extends FlowService<DurakFlow, DurakAction> {

    private static FlowServiceImpl instance = null;

    public static FlowServiceImpl getInstance() {
        if (null != instance) {
            return instance;
        }
        synchronized (FlowServiceImpl.class) {
            if (null == instance) {
                instance = new FlowServiceImpl();
            }
        }

        return instance;
    }

    @Override
    public void createFlow(String gId) {
        DurakFlow flow = new DurakFlow(0);
        flows.put(gId, flow);
    }

    @Override
    public boolean isValidAction(String gId, DurakAction durakAction) {
        Flow<DurakAction> f = getFlow(gId);
        String initiatorPId = durakAction.getInitiatorPId();


        if (PlayerServiceImpl.getInstance().isPlayerInGame(gId, initiatorPId)) {//If the player initiated this action is part of the game.
            int initiatorPlayerPosIndex = GameServiceImpl.getInstance().getPlayerPosIdx(gId, initiatorPId);
            int currentTurnIndex = f.getCurrentPlayerTurnIndex();
            String targetPId = getCurrentDefenderPId(gId);
            String attackingCardId = durakAction.getAttackCardId();
            switch (durakAction.getType()) {
                case DurakAction.Types.ATTACK:

                    int currentYackSize = YackServiceImpl.getInstance().getYackSize(gId);

                    /**
                     * The Attack action is valid when :
                     *
                     *  its the attacker's turn :) (which means no cards are on the floor yet)
                     *  OR
                     *      everyone can put cards AND the attacker is not the target.
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
                                    (!initiatorPId.equals(targetPId) || YackServiceImpl.getInstance().getFrontCardsSize(gId) < 2)
                                    &&
                                    !StringUtil.isNullOrEmpty(attackingCardId)
                                    &&
                                    YackServiceImpl.getInstance().isRankExists(gId, Card.getById(attackingCardId).getRank())
                                    &&
                                    !isMaxAttackExceeded(gId, targetPId, currentYackSize));
                case DurakAction.Types.ANSWER:

                    String answeringCardId = durakAction.getAnswerCardId();
                    Card answeringCard = Card.getById(answeringCardId);
                    Card attackingCard = Card.getById(attackingCardId);


                    /**
                     * The Answer action is valid when :
                     *
                     *      the initiator of the attack is targeted.
                     *      and
                     *      attacking card is not empty
                     *      and
                     *      answering card is not empty.
                     *      and
                     *      (rank of answering card is greater than attacking card
                     *      and
                     *      suit of answering card is same as suit of attacking card
                     *      or
                     *      the answering card is strong card and the attacking card isn't.)

                     */
                    return initiatorPId.equals(targetPId)
                            &&
                            !StringUtil.isNullOrEmpty(attackingCardId)
                            &&
                            !StringUtil.isNullOrEmpty(answeringCardId)
                            &&
                            (answeringCard.compareTo(attackingCard) > 0
                                    &&
                                    answeringCard.getSuit() == attackingCard.getSuit()
                                    ||
                                    (answeringCard.getSuit() != attackingCard.getSuit() && DeckServiceImpl.getInstance().isStrongCard(gId, answeringCard)));

                case DurakAction.Types.COLLECT:
                    return initiatorPId.equals(targetPId)
                            &&
                            YackServiceImpl.getInstance().getYackSize(gId) > 0
                            &&
                            isAttackOver(gId);
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private boolean isAttackOver(String gId) {
        return true;
    }

    private boolean isMaxAttackExceeded(String gId, String targetPId, int currentYackSize) {
        return YackServiceImpl.MAX_YACK_SIZE <= currentYackSize
                ||
                PlayerServiceImpl.getInstance().getCardsSize(gId, targetPId) <= currentYackSize;
    }

    @Override
    protected void increaseFlow(String gId) {
        DurakFlow flow = getFlow(gId);
        flow.setCurrentDefenderIndex((flow.getCurrentDefenderIndex() + 1) % GameServiceImpl.getInstance().getPlayersIds(gId).size());
    }

    @Override
    protected void doAction(String gId, DurakAction durakAction) {

        DurakFlow flow = getFlow(gId);
        switch (durakAction.getType()) {
            case DurakAction.Types.ATTACK:
                String attackingCardId = durakAction.getAttackCardId();
                PlayerServiceImpl.getInstance().removeCardId(gId, durakAction.getInitiatorPId(), attackingCardId, true);
                YackServiceImpl.getInstance().addBackCardId(gId, attackingCardId);
                flow.setCurrentPlayerTurnIndex(-1);
                if (getCurrentDefenderPId(gId).equals(durakAction.getInitiatorPId())) {
                    increaseFlow(gId);
                }
                break;
            case DurakAction.Types.ANSWER:
                String answeringCardId = durakAction.getAnswerCardId();
                PlayerServiceImpl.getInstance().removeCardId(gId, durakAction.getInitiatorPId(), answeringCardId, true);
                YackServiceImpl.getInstance().addFrontCardId(gId, durakAction.getAttackCardId(), answeringCardId);
                if (isMaxAttackExceeded(gId, getCurrentDefenderPId(gId), YackServiceImpl.getInstance().getYackSize(gId))) {
                    endTurn(gId);
                }
        }
    }

    @Override
    protected void endTurn(String gId) {
        increaseFlow(gId);
    }

    @Override
    public JsonObject getJsonFlow(String gId) {

        JsonObject jsonFlow = super.getJsonFlow(gId);
        DurakFlow flow = getFlow(gId);
        jsonFlow.putNumber("defenderPosIndex", flow.getCurrentDefenderIndex());

        return jsonFlow;
    }


    public String getCurrentDefenderPId(String gId) {
        return GameServiceImpl.getInstance().getPlayerPId(gId, getFlow(gId).getCurrentDefenderIndex());
    }
}
