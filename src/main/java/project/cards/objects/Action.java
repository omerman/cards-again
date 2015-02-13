package project.cards.objects;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by omerpr on 05/02/2015.
 */
public abstract class Action {

    private String initiatorPId;
    private String type;

    public Action(String initiatorPId,String type) {
        setInitiatorPId(initiatorPId);;
        setType(type);
    }

    private Map<String,String> actionCards = new HashMap<>();

    public String getInitiatorPId() {
        return initiatorPId;
    }

    public void setInitiatorPId(String initiatorPId) {
        this.initiatorPId = initiatorPId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    protected void setActionCardId(String key,String cardId) {
        this.actionCards.put(key,cardId);
    }

    protected String getActionCardId(String key) {
        return this.actionCards.get(key);
    }
}
