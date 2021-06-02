package Tests;

import Shared.*;
import com.google.gson.Gson;

public class GsonTest {

	private Gson gson;
	private KMessage msg;
	private String jsonString;

	public GsonTest() {
		try {
			gson = new Gson();
			
			// Serialization
			msg = new KMessage();
			msg.sender = "bedo";
			msg.receiver = "baro";
			msg.message = "selam";
			msg.type = CommandType.GroupMessage;

			jsonString = gson.toJson(msg);
			System.out.println(jsonString);

			// Deserialization
			KMessage obj2 = gson.fromJson(jsonString, KMessage.class);
			jsonString = gson.toJson(obj2);
			System.out.println(jsonString);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
