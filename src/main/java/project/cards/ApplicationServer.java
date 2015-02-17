package project.cards;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;
import project.cards.auth.AuthManager;
import project.cards.services.eventbus.EventBusService;
import project.cards.services.game.durak.GameServiceImpl;
import project.cards.services.game.durak.PlayerServiceImpl;

public class ApplicationServer extends Verticle {

	private static final String webFolder = System.getProperty("user.dir") + "/src/main/webapp";
	private EventBusService eventBusService;

	public void start() {

		container.deployModule("io.vertx~mod-mongo-persistor~2.1.1", container.config().getObject("mongo-persistor"));
		container.deployVerticle("project.cards.auth.AuthManager");

		HttpServer server = initiateServer();

		initSockJSServer(server);

		server.listen(8080);
		container.logger().info("Webserver started, listening on port: 8080");


	}

	private SockJSServer initSockJSServer(HttpServer server) {
		SockJSServer sockJSServer = vertx.createSockJSServer(server);
		initSockJSServerBridge(sockJSServer);

		return sockJSServer;
	}

	private void initSockJSServerBridge(SockJSServer sockJSServer) {


		JsonObject config = new JsonObject().putString("prefix", "/eventbus");

		JsonArray inBoundPermitted = new JsonArray()
				.add(new JsonObject().putString("address", "vertx.basicauthmanager.login"))
				.add(new JsonObject().putString("address", "games.list.request"))
				.add(new JsonObject().putString("address", "my.info.request").putBoolean("requires_auth", true))
				.add(new JsonObject().putString("address", "game.join.request").putBoolean("requires_auth", true))
				.add(new JsonObject().putString("address", "game.create.request").putBoolean("requires_auth", true))
				.add(new JsonObject().putString("address", "game.info.request"))
				.add(new JsonObject().putString("address", "game.myHand.request").putBoolean("requires_auth", true))
				.add(new JsonObject().putString("address", "game.action.request").putBoolean("requires_auth", true))
				.add(new JsonObject().putString("address", "game.readyOrNot.request").putBoolean("requires_auth", true));

		JsonArray outBoundPermitted = new JsonArray()
				.add(new JsonObject().putString("address", "games.list.update"))
				.add(new JsonObject().putString("address_re", "game.info.update\\..+"))
				.add(new JsonObject().putString("address_re", "game.started\\..+"));

		this.eventBusService = new EventBusService(vertx.eventBus(), GameServiceImpl.getInstance());

		sockJSServer.setHook(this.eventBusService.getEventBusBridgeHook());

		sockJSServer.bridge(config, inBoundPermitted, outBoundPermitted);

	}

	private HttpServer initiateServer() {
		RouteMatcher matcher = initiateMatcher();
		return vertx.createHttpServer().requestHandler(matcher);
	}

	private RouteMatcher initiateMatcher() {
		RouteMatcher matcher = new RouteMatcher();
		matcher.noMatch(getNoMatchRoute());
		return matcher;
	}

	private Handler<HttpServerRequest> getNoMatchRoute() {
		return new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest req) {
				String path = req.path();
				if(path.equals("/")) {
					path = "/index.html";
				}
				if(true) {
					req.response().sendFile(webFolder + path);
				}
			}
		};
	}
}
