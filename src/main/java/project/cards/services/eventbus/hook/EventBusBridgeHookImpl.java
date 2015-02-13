package project.cards.services.eventbus.hook;

import com.hazelcast.util.StringUtil;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.sockjs.EventBusBridgeHook;
import org.vertx.java.core.sockjs.SockJSSocket;
import project.cards.services.eventbus.EventBusService;

/**
 * Created by omerpr on 23/01/2015.
 */
public class EventBusBridgeHookImpl implements EventBusBridgeHook {

    private static final Logger logger = LoggerFactory.getLogger(EventBusBridgeHookImpl.class);
    private EventBusService eventBusService;

    public EventBusBridgeHookImpl(EventBusService eventBusService) {
        this.eventBusService = eventBusService;
    }

    @Override
    public boolean handleSocketCreated(SockJSSocket sock) {

        logger.info("add client "+sock.remoteAddress());
        return true;
    }

    @Override
    public void handleSocketClosed(SockJSSocket sock) {
        logger.info("remove client "+sock.remoteAddress());
    }

    @Override
    public boolean handleSendOrPub(SockJSSocket sock, boolean send, JsonObject msg, String address) {
        logger.info("handleSendOrPub for: "+sock.remoteAddress()+" on address: "+address + " message: "+msg);

        String sessionID = msg.getString("sessionID");
        if(!StringUtil.isNullOrEmpty(sessionID)) {
            if(null == msg.getObject("body")) {
                msg.putObject("body",new JsonObject());
            }
            msg.getObject("body").putString("pId",sessionID);
        }

        return true;
    }


    @Override
    public boolean handlePreRegister(SockJSSocket sock, String address) {
        logger.info("handlePreRegister for: "+sock.remoteAddress()+" on address: "+address);
        return true;
    }

    @Override
    public void handlePostRegister(SockJSSocket sock, String address) {

    }

    @Override
    public boolean handleUnregister(SockJSSocket sock, String address) {
        return true;
    }

    @Override
    public boolean handleAuthorise(JsonObject message, String sessionID, Handler<AsyncResult<Boolean>> handler) {
        logger.info("handleAuthorise :) "+message);
        return false;
    }
}
