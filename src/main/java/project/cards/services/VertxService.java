package project.cards.services;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;

/**
 * Created by omerpr on 28/02/2015.
 */
public class VertxService {
	private static VertxService instance = new VertxService();
	private Vertx vertx;

	public static Vertx getVertx() {
		return instance.vertx;
	}

	private VertxService() {
		vertx = VertxFactory.newVertx();
	}
}
