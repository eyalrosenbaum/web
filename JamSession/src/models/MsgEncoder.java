package models;

import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

public class MsgEncoder implements Encoder {

	public String encode(Message msg){
		Gson gson = new Gson();
		String message = gson.toJson(msg);
		return message;
	}
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(EndpointConfig arg0) {
		// TODO Auto-generated method stub

	}

}
