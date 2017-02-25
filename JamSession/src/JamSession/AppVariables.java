package JamSession;

import java.util.ArrayList;
import java.util.HashMap;

import models.User;

public class AppVariables {
	//hashmap to keep tabs on what channels users are currently in
		public static HashMap<String,ArrayList<User>> usersByChannel = new HashMap<String,ArrayList<User>>();
		public static HashMap<String,ArrayList<User>> activeUsersByChannel = new HashMap<String,ArrayList<User>>();
		public static DatabaseConnection db;
		public static int privateChatCounter;
}
