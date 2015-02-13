package project.cards.services.eventbus;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.sockjs.EventBusBridgeHook;
import project.cards.objects.durak.DurakAction;
import project.cards.objects.durak.DurakFlow;
import project.cards.services.eventbus.hook.EventBusBridgeHookImpl;
import project.cards.services.game.DeckService;
import project.cards.services.game.GameService;
import project.cards.services.game.PlayerService;
import project.cards.services.game.durak.DeckServiceImpl;
import project.cards.services.game.durak.FlowServiceImpl;
import project.cards.services.game.durak.YackServiceImpl;

import java.util.Set;

/**
 * Created by omerpr on 23/01/2015.
 */
public class EventBusService {

    protected EventBus eventBus;
    private static final Logger logger = LoggerFactory.getLogger(EventBusService.class);
    protected EventBusBridgeHook eventBusBridgeHook;

    protected final GameService gameService;
    protected final PlayerService playerService;

    public EventBusService(EventBus eventBus,GameService gameService) {
        this.eventBus = eventBus;

        registerListeners();
        setEventBusBridgeHook(new EventBusBridgeHookImpl(this));
        this.gameService = gameService;
        this.playerService = gameService.getPlayerService();
    }

    public void registerListeners() {

        this.eventBus.registerHandler("user.register",new Handler<Message<JsonObject>>() {

            @Override
            public void handle(Message<JsonObject> event) {

                String pId = event.body().getString("sessionID");
                String name = event.body().getString("username");
                logger.info("register for pId: "+pId);

                gameService.getPlayerService().setPlayerUserName(pId, name);
            }
        });

        this.eventBus.registerHandler("user.unregister",new Handler<Message<JsonObject>>() {

            @Override
            public void handle(Message<JsonObject> event) {

                String pId = event.body().getString("sessionID");
                logger.info("unregister for pId: "+pId);

                Set<String> gamesIds = gameService.getPlayerService().removePlayer(pId);

                if(null != gamesIds) {
                    for (String gId : gamesIds) {
                        gameService.removePlayer(gId, pId);
                    }
                }

                publishGamesList();
            }
        });

        this.eventBus.registerHandler("games.list.request", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                logger.info("games list request was made.");
                event.reply(new JsonObject().putArray("gamesList", gameService.getJsonAllGames()));
            }
        });

        this.eventBus.registerHandler("game.create.request", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                logger.info("game create request was made.");

                String gName = event.body().getString("name");
                String pId = event.body().getString("pId");

                String gId = gameService.createGame(gName);

                gameService.addPlayer(gId, pId);
                gameService.getPlayerService().createPlayer(gId, pId, true);

                publishGamesList();

                event.reply(new JsonObject().putString("gId",gId));

            }
        });

        this.eventBus.registerHandler("my.info.request", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                event.reply(new JsonObject().putString("pId", event.body().getString("pId")));
            }
        });

        this.eventBus.registerHandler("game.join.request", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                logger.info("game join request was made.");

                String gId = event.body().getString("gId");

                String pId = event.body().getString("pId");

                try {
                    gameService.joinGame(gId, pId);//throws if could not join.

                    gameService.getPlayerService().getOrCreatePlayer(gId, pId);//if joined successfully.. create the player in this game.

                    publishGamesList();
                    publishGameInfoUpdate(gId);

                    event.reply(new JsonObject().putBoolean("SUCCESS",true));
                }
                catch(Exception e) {
                    replyError(event, e);
                }


            }
        });

        eventBus.registerHandler("game.info.request", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                logger.info("game info request was made.");
                String gId = event.body().getString("gId");
                String pId = event.body().getString("pId");

                try {
                    JsonObject gameInfo = gameService.getJsonGameInfo(gId);

                    gameInfo.putNumber("requesterPosIdx", gameService.getPlayerPosIdx(gId, pId));

                    event.reply(new JsonObject().putObject("game", gameInfo));
                }
                catch(Exception e) {
                    replyError(event,e);
                }
            }
        });

        eventBus.registerHandler("game.myHand.request",new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                logger.info("myHand request was made.");
                String gId = event.body().getString("gId");
                String pId = event.body().getString("pId");

                replyMyHand(event, gId, pId);
            }
        });

        eventBus.registerHandler("game.readyOrNot.request",new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                String gId = event.body().getString("gId");

                if(!gameService.isGameStarted(gId)) {

                    String pId = event.body().getString("pId");
                    boolean isReady = event.body().getBoolean("isReady");
                    gameService.getPlayerService().setReady(gId, pId, isReady);

                    if(gameService.isReadyToStart(gId)) {
                        gameService.startGame(gId);
                        YackServiceImpl.getInstance().createYack(gId);
                        publishGameStarted(gId);
                        publishGameInfoUpdate(gId);
                    }
                }
                event.reply(new JsonObject());
            }
        });

        eventBus.registerHandler("game.action.request",new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                logger.info(event.body());
                String gId = event.body().getString("gId");
                String pId = event.body().getString("pId");
                String actionType = event.body().getString("actionType");

                DurakAction durakAction = new DurakAction(pId,actionType);

                JsonObject attackCard;

                switch(actionType) {
                    case DurakAction.Types.ATTACK:
                        attackCard = event.body().getObject("attackCard");
                        durakAction.setAttackCardId(DeckServiceImpl.getInstance().getCardId(attackCard));
                        break;
                    case DurakAction.Types.ANSWER:
                        attackCard = event.body().getObject("attackCard");
                        JsonObject answerCard = event.body().getObject("answerCard");
                        durakAction.setAttackCardId(DeckServiceImpl.getInstance().getCardId(attackCard));
                        durakAction.setAnswerCardId(DeckServiceImpl.getInstance().getCardId(answerCard));
                        break;
                    case DurakAction.Types.COLLECT:
                        break;
                    default:
                        logger.info("TODO: handle me.. no attack type specified.");
                }

                FlowServiceImpl.getInstance().requestAction(gId,durakAction);
                publishGameInfoUpdate(gId);
                replyMyHand(event,gId,pId);
            }
        });
    }

    private void replyMyHand(Message<JsonObject> event, String gId, String pId) {
        event.reply(new JsonObject().putArray("myHand", gameService.getPlayerService().getJsonPlayerCards(gId, pId)));
    }

    private void replyError(Message<JsonObject> event,Exception e) {
        event.reply(new JsonObject().putString("error",e.getLocalizedMessage()));
        logger.error(e);
        e.printStackTrace();
    }

    private void publishGameStarted(String gId) {
        eventBus.publish("game.started."+gId,new JsonObject());
    }

    private void setEventBusBridgeHook(EventBusBridgeHookImpl eventBusBridgeHook) {
        this.eventBusBridgeHook = eventBusBridgeHook;
    }

    public EventBusBridgeHook getEventBusBridgeHook() {
        return eventBusBridgeHook;
    }

    private void publishGameInfoUpdate(String gId) {
        eventBus.publish("game.info.update."+gId, gameService.getJsonGameInfo(gId));
    }

    public void publishGamesList() {
        eventBus.publish("games.list.update",new JsonObject().putArray("gamesList", gameService.getJsonAllGames()));
    }

}
