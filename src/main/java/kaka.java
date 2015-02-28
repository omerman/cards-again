import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

/**
 * Created by omerpr on 23/01/2015.
 */
public class kaka {
	public static void main(String[] ar) {
		OmerPRizner o = new OmerPRizner();

		System.out.print(new JsonObject(Json.encode(o)));
	}
}
