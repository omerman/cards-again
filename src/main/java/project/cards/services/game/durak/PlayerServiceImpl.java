package project.cards.services.game.durak;

import project.cards.services.game.DeckService;
import project.cards.services.game.PlayerService;

/**
 * Created by omerpr on 24/01/2015.
 */
public class PlayerServiceImpl extends PlayerService{

    private static PlayerServiceImpl instance = null;

    private PlayerServiceImpl() {

    }

    public static PlayerServiceImpl getInstance() {
        if(null != instance) {
            return instance;
        }
        synchronized (PlayerServiceImpl.class) {
            if(null == instance) {
                instance = new PlayerServiceImpl();
            }
        }

        return instance;
    }

    @Override
    public DeckService getDeckService() {
        return DeckServiceImpl.getInstance();
    }

}
