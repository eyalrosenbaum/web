package models;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

public class MsgDecoder implements Decoder.Text<Message> {

	@Override
	public void destroy() {
		// do nothing
		
	}

	@Override
	public void init(EndpointConfig config) {
		// do nothing
		
	}

	@Override
	public Message decode(String msg) throws DecodeException {
		Gson gson = new Gson();
		Message message = gson.fromJson(msg, Message.class);
		return message;
	}

	@Override
	public boolean willDecode(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
