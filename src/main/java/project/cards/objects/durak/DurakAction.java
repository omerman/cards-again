package project.cards.objects.durak;

import project.cards.objects.Action;

import java.util.Map;

/**
 * Created by omerpr on 05/02/2015.
 */
public class DurakAction extends Action{

    public DurakAction(String initiatorPId, String type) {
        super(initiatorPId, type);
    }

    public void setAttackCardId(String attackCardId) {
        this.setActionCardId(ActionCards.ATTACK_CARD,attackCardId);
    }
    public String getAttackCardId() {
        return this.getActionCardId(ActionCards.ATTACK_CARD);
    }

    public void setAnswerCardId(String answerCardId) {
        this.setActionCardId(ActionCards.ANSWER_CARD,answerCardId);
    }
    public String getAnswerCardId() {
        return this.getActionCardId(ActionCards.ANSWER_CARD);
    }

    public class Types {
        public static final String ATTACK = "ATTACK";
        public static final String ANSWER = "ANSWER";
        public static final String COLLECT = "COLLECT";
    }

    public class ActionCards {
        public static final String ATTACK_CARD = "ATTACK_CARD";
        public static final String ANSWER_CARD = "ANSWER_CARD";
    }
}
