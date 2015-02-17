package project.cards.services.game;

import org.vertx.java.core.json.JsonObject;
import project.cards.objects.Action;
import project.cards.objects.Flow;
import project.cards.objects.durak.DurakFlow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by omerpr on 05/02/2015.
 */
public abstract class FlowService<F extends Flow, A extends Action> {
	protected Map<String, F> flows = new HashMap<>();

	public abstract void createFlow(String gId);

	public abstract boolean isValidAction(String gId, A a);

	protected abstract void doAction(String gId, A a);

	protected abstract void initializeFlow(String gId);

	protected void increaseFlow(String gId, int nextPlayerIndex) {
		getFlow(gId).setCurrentPlayerTurnIndex(nextPlayerIndex);
	}

	protected F getFlow(String gId) {
		return flows.get(gId);
	}

	public void requestAction(String gId, A a) {
		Flow<A> f = getFlow(gId);

		if(null != f) {
			if(isValidAction(gId, a)) {
				doAction(gId, a);
			} else {
				throw new RuntimeException("This action is not allowed.");
			}
		} else {
			throw new RuntimeException("Flow does not exist.");
		}
	}


	public JsonObject getJsonFlow(String gId) {
		JsonObject jsonFlow = new JsonObject();
		Flow flow = getFlow(gId);
		jsonFlow.putNumber("turnPosIndex", flow.getCurrentPlayerTurnIndex());
		return jsonFlow;
	}
}
