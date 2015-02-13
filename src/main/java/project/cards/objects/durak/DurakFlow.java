package project.cards.objects.durak;

import project.cards.objects.Flow;

/**
 * Created by omerpr on 05/02/2015.
 */
public class DurakFlow implements Flow<DurakAction> {

    private int currentPlayerTurnIndex;
    private int currentDefenderIndex;

    public DurakFlow(int currentPlayerTurnIndex) {
        setCurrentPlayerTurnIndex(currentPlayerTurnIndex);
        setCurrentDefenderIndex(currentPlayerTurnIndex + 1);
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

}
