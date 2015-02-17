package project.cards.objects.durak;

import project.cards.objects.Flow;

import java.util.*;

/**
 * Created by omerpr on 05/02/2015.
 */
public class DurakFlow implements Flow<DurakAction> {


	private int turnInitiatorIndex;
	private int currentPlayerTurnIndex;
	private int currentDefenderIndex;

	private long actionsTimerPeriod;
	private long actionsTimerID;

	private long defenderTimerPeriod;
	private long defenderTimerID;

	private Set<String> doneAttackingUsers;
	private boolean isDefenderCollecting;


	public DurakFlow(int currentPlayerTurnIndex) {
		setTurnInitiatorIndex(currentPlayerTurnIndex);
		setCurrentPlayerTurnIndex(currentPlayerTurnIndex);
		setCurrentDefenderIndex(currentPlayerTurnIndex + 1);
		setDefenderCollecting(false);
		resetDoneAttackingUsers();
	}

	@Override
	public int getCurrentPlayerTurnIndex() {
		return currentPlayerTurnIndex;
	}

	@Override
	public void setCurrentPlayerTurnIndex(int currentPlayerTurnIndex) {
		this.currentPlayerTurnIndex = currentPlayerTurnIndex;
	}

	public void setCurrentDefenderIndex(int currentDefenderIndex) {
		this.currentDefenderIndex = currentDefenderIndex;
	}

	public int getCurrentDefenderIndex() {
		return currentDefenderIndex;
	}

	public long getActionsTimerPeriod() {
		return actionsTimerPeriod;
	}

	public void setActionsTimerPeriod(long actionsTimerPeriod) {
		this.actionsTimerPeriod = actionsTimerPeriod;
	}

	public long getActionsTimerID() {
		return actionsTimerID;
	}

	public void setActionsTimerID(long actionsTimerID) {
		this.actionsTimerID = actionsTimerID;
	}

	public long getDefenderTimerPeriod() {
		return defenderTimerPeriod;
	}

	public void setDefenderTimerPeriod(long defenderTimerPeriod) {
		this.defenderTimerPeriod = defenderTimerPeriod;
	}

	public long getDefenderTimerID() {
		return defenderTimerID;
	}

	public void setDefenderTimerID(long defenderTimerID) {
		this.defenderTimerID = defenderTimerID;
	}

	public int getTurnInitiatorIndex() {
		return turnInitiatorIndex;
	}

	public void setTurnInitiatorIndex(int turnInitiatorIndex) {
		this.turnInitiatorIndex = turnInitiatorIndex;
	}

	public Set<String> getDoneAttackingUsers() {
		return doneAttackingUsers;
	}

	public void resetDoneAttackingUsers() {
		this.doneAttackingUsers = new HashSet<>();
	}

	public void toggleDoneAttackingUser(String userName) {
		if(this.doneAttackingUsers.contains(userName)) {
			this.doneAttackingUsers.remove(userName);
		} else {
			this.doneAttackingUsers.add(userName);
		}
	}

	public boolean isDefenderCollecting() {
		return isDefenderCollecting;
	}

	public void setDefenderCollecting(boolean isDefenderCollecting) {
		this.isDefenderCollecting = isDefenderCollecting;
	}
}
