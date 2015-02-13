package project.cards.objects;

/**
 * Created by omerpr on 05/02/2015.
 */
public interface Flow<A extends Action> {

    int getCurrentPlayerTurnIndex();
    void setCurrentPlayerTurnIndex(int currentPlayerTurnIndex);

}
