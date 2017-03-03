// (function () {
// 	'use strict';


angular.module('jamSession', [])
.value('GlobalWelcomeScreen',true)
.value('GlobaluserName',"")
.value('GlobaluserNickname',"")
.value('GlobaluserDescription',"")
.value('Globalpassword',"")
.value('GlobalphotoURL',"")
.value('Globallastloged',new Date())
.value('Globallastlastloged',new Date())
.factory('session',session)
.controller('jamSessionController', jamSessionController);

	session.$inject = ['$http','$q','$rootScope'];
	function session($http,$q,$rootScope){
		var defer = $q.defer();
		$http.get('http://localhost:8080/Proj/GetSessionDetailsServlet').then(function(result){
				if ((typeof result.data!='string')||(result.data.trim() != 'fail')){
					$rootScope.GlobalWelcomeScreen = false;
					$rootScope.GlobalshowChannels = true;
					$rootScope.GlobaluserName = result.data.userName;
					$rootScope.GlobaluserNickname = result.data.userNickname;
					$rootScope.GlobaluserDescription = result.data.userDescription;
					$rootScope.Globalpassword = result.data.password;
					$rootScope.GlobalphotoURL = result.data.photoURL;
					$rootScope.Globallastlogged = new Date(result.data.lastlogged);
					console.log("lastlog at beginninng is "+$rootScope.Globallastlogged);
					$rootScope.Globallastlastlogged = new Date(result.data.lastlastlogged);
					console.log("lastlastlog at beginninng is "+$rootScope.Globallastlastlogged);
				}
				else{
					$rootScope.GlobalWelcomeScreen = true;
					$rootScope.GlobalshowChannels = false;
					$rootScope.GlobaluserName = "";
					$rootScope.GlobaluserNickname = "";
					$rootScope.GlobaluserDescription = "";
					$rootScope.Globalpassword = "";
					$rootScope.GlobalphotoURL= "";
					$rootScope.Globallastlogged = new Date();
					$rootScope.Globallastlastlogged = new Date();
				}
				console.log(result.data);
				console.log("welcomeScreen is "+$rootScope.GlobalWelcomeScreen);
				defer.resolve('done');
			});
		return defer.promise;
		};


	jamSessionController.$inject = ['$http','$scope','$location','$window','$rootScope','session'/*,'jamSessionService'*/];
	function jamSessionController($http,$scope,$location,$window,$rootScope,session/*,jamSessionService*/) {
		var jam = this;
		session.then(function(){
		jam.welcomeScreen = $rootScope.GlobalWelcomeScreen;
		console.log("welcomeScreen is "+jam.welcomeScreen);
		jam.userName=$rootScope.GlobaluserName;
		console.log("userName is "+jam.userName);
		jam.password=$rootScope.Globalpassword;
		console.log("password is "+jam.password);
		jam.userNickname=$rootScope.GlobaluserNickname;
		console.log("usernickname is "+jam.userNickname);
		jam.userDescription=$rootScope.GlobaluserDescription;
		jam.photoURL=$rootScope.GlobalphotoURL;
		jam.lastLogged = $rootScope.Globallastlogged;
		jam.lastlastlogged =$rootScope.Globallastlastlogged;
		jam.showChannels = $rootScope.GlobalshowChannels;
		/*function that gets user's public channels on login*/
		jam.getUserPublicChannels = function(nickname){
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/FindSubscriptionServlet',
			}).then(
					function(response){
						if (response.data!=undefined){
						jam.UserPublicChannels = response.data;

						console.log("FindSubscriptionServlet called, UserPublicChannels is "+jam.UserPublicChannels);
						/*setting the attributes mentions and notifications to each of the user's channel, and afterwards updating them*/
						for(var i=0;i<jam.UserPublicChannels.length;i++){
							(jam.UserPublicChannels[i]).mentions = 0;
							(jam.UserPublicChannels[i]).notifications = 0;
							(jam.UserPublicChannels[i]).notifications = jam.updateNotificationsOnLoadPublic(jam.UserPublicChannels[i]);
							(jam.UserPublicChannels[i]).mentions = jam.updateMentionsOnLoadPublic(jam.UserPublicChannels[i]);
							console.log("jam.UserPublicChannels["+i+"] is "+jam.UserPublicChannels[i]);
						}

					}

					});
		};

		/*function that gets user's private channels on login*/
		jam.getUserPrivateChannels = function(nickname){
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/FindPrivateChannelsServlet',
			}).then(
					function(response){
						console.log("FindPrivateChannelsServlet called, response is "+response.data);
						if (response.data!=undefined){
								jam.UserPrivateChannels = response.data;
							for(var i=0;i<jam.UserPrivateChannels.length;i++){
								console.log("jam.UserPrivateChannels["+i+"] is "+jam.UserPrivateChannels[i]);
								/*setting the attributes mentions and notifications to each of the user's channel, and afterwards updating them*/
								jam.UserPrivateChannels[i].mentions = 0;
								jam.UserPrivateChannels[i].notifications = 0;
								jam.UserPrivateChannels[i].notifications = jam.updateNotificationsOnLoadPrivate((jam.UserPrivateChannels[i]));
								jam.UserPrivateChannels[i].mentions = jam.updateMentionsOnLoadPrivate((jam.UserPrivateChannels[i]));

							}

						}

					});
		};

		if (jam.showChannels){
			jam.getUserPublicChannels(jam.userNickname);
			jam.getUserPrivateChannels(jam.userNickname);
		};
		$http.get("http://localhost:8080/Proj/Channels").then(
		function(response){
		});

		$http.get("http://localhost:8080/Proj/Messages").then(
		function(response){
		});
		$http.get("http://localhost:8080/Proj/Subscriptions").then(
		function(response){
		});
		$http.get("http://localhost:8080/Proj/Users").then(
		function(response){
		});
		/*welcomeScreen is true when we display login or signup panel and false after user is logged into site*/
		jam.showSideBar = true;
		jam.ErrorExists = false;
		jam.createChannelScreen = false;
		jam.showThreads = false;
		jam.showSearchResults = false;
		jam.ErrorMsg = "";
		jam.ActiveChannel ="";
		/*-1 is the value for a message that is not a reply in the variable replyParentId*/
		jam.replyParentId =-1;
		jam.replyIndication = false;
		jam.showSearchResults = false;
		jam.replyTo="";
		jam.channel_description="";
		jam.channel_name="";
		/*tab is the active tab on login signup panel*/
		jam.tab = "signup";
		/*for checking purposes*/
		console.log("username is "+jam.userName);
		console.log("password is "+jam.password);
		console.log("userNickname is "+jam.userNickname);
		console.log("userDescription is "+jam.userDescription);
		console.log("photoURL is "+jam.photoURL);
		console.log("lastLogged is "+jam.lastLogged);
		console.log("lastlastlogged is "+jam.lastlastlogged);
		console.log("welcomSecreen is "+jam.welcomeScreen);

		/*method to change from login to sign up and vice versa on welcome screen*/
		jam.selectTab = function(setTab) {
			jam.tab = setTab;
		};

		/*method to change view from login to sign up and vice versa on welcome screen*/
		jam.isSelected = function(checkTab){
			return jam.tab === checkTab;
		};


	function connect() {
		/*setting up websocket on client to send messages*/
		var wsUri = "ws://"+window.location.host+window.location.pathname+"chat/"+jam.userNickname;
		console.log("wsUri is "+wsUri);
		jam.websocket = new WebSocket(wsUri);

		jam.websocket.onopen = function(evt){
			console.log("connected to server");
		};
		//what happens when we recieve a message
		jam.websocket.onmessage = function(evt){
			notify(evt.data);
		};
		jam.websocket.onerror = function(evt){
			console.log('ERROR: '+evt.data);
		};
		jam.websocket.onclose = function(evt){
			jam.websocket = null;
			console.log("disconnected from server");
		};
	}
		/*posting a message to screen - need to write this*/
		function notify(message) {
			console.log("starting notify function");
			console.log("message is "+message);
			message = JSON.parse(message);
			/*if message is a request to open private chat*/
			if (message.hasOwnProperty("participanta")){
				/*get channels properties from server - because this is the recieving participant the channel already exists*/
				var credentials = {
						usera : message.participanta,
						userb : message.participantb
				}
				if ((jam.userNickname == message.participanta)||(jam.userNickname == message.participantb)){
							if (jam.userNickname == message.participantb){
							credentials.userb = credentials.usera;
							credentials.usera = jam.userNickname;
					}
					$http({
						method: 'POST',
						url: 'http://localhost:8080/Proj/GetPrivateChatServlet',
						data: credentials
					}).then(
							function(response){
								console.log("GetPrivateChatServlet called, response is "+response.data);
								if (response.data != null){
									/*entering private channel to channels list and updating  it's mentions and notifications*/
									jam.UserPrivateChannels.push(response.data);
									(jam.UserPrivateChannels[jam.UserPrivateChannels.length-1]).setAttribute(mentions,0);
									(jam.UserPrivateChannels[jam.UserPrivateChannels.length-1]).setAttribute(notifications,1);
								}
							});
				}}
			else{
				console.log("message is "+message);
				console.log("active channe is "+jam.ActiveChannel);
				/*for users that are on the channel that contains the new message, the page should refresh according to the thread updated*/
				if (jam.ActiveChannel == message.channel){
					/*getting 10 newest threads to show*/
					jam.getNewestThreads(jam.ActiveChannel);
				}
				else{
					console.log("message is "+message);
					/*for users that are subscribed to the channel that contains the new message but are not there there should be a notification*/
					var isSubscribed = jam.checkSubscription(message.channel);
					if (isSubscribed == "public")
						jam.updateNotificationsPublic(message.channel);
					if (isSubscribed == "private")
						jam.updateNotificationsPrivate(message.channel);
					/*if user is subscribed to channel and is mentioned in the new message posted he should get a notification*/
					console.log("message content is "+message.content);
					if (message.content.includes("@"+jam.userNickname)){
						if (isSubscribed == "public")
							jam.updateMentionsPublic(message.channel);
						if (isSubscribed == "private")
							jam.updateMentionsPrivate(message.channel);
					}
				}
			}
		};




		/*method activated when user tries to login to site*/
		jam.userLogin = function(){
			var userCredentials = {
					userName : jam.userName,
					password : jam.password
			};
			$http.post(//{
				/*method: 'POST',
				url: */"http://localhost:8080/Proj/LoginServlet",
				/*data: */JSON.stringify(userCredentials)
			/*}*/).then(
					function(response){
						if ((typeof response.data === 'string')&&(response.data.trim() == "fail")){
							jam.ErrorExists = true;
							jam.ErrorMsg = "wrong username or password";
						}
						else{
							console.log("LoginServlet called, response is "+response.data.userName);
							if (response.data.userName != undefined){
							jam.userName=response.data.userName;
							console.log("userName is "+ jam.userName);
							jam.password=response.data.password;
							console.log("password is "+ jam.password);
							jam.userNickname=response.data.userNickname;
							console.log("userNickname is "+ jam.userNickname);
							jam.userDescription=response.data.userDescription;
							console.log("userDescription is "+ jam.userDescription);
							jam.photoURL=response.data.photoURL;
							console.log("photoURL is "+ jam.photoURL);
							jam.islogged = response.data.islogged;
							jam.lastlastlogged = new Date(response.data.lastlogged)
							jam.lastLogged = Date.now()
							jam.welcomeScreen=false;
							jam.getUserPublicChannels(jam.userNickname);
							jam.getUserPrivateChannels(jam.userNickname);
							jam.showChannels = true;
							jam.ErrorExists = false;
						}
					}
					});
		};

		/*method activated when user tries to signup to site*/
		jam.userSignUp = function(){
			var newUserDetails = {
					userName : jam.userName,
					password : jam.password,
					userNickname : jam.userNickname,
					userDescription : jam.userDescription,
					userPhotoURL : jam.photoURL
			};
			$http.post(//{
				/*method: 'POST',
				url: */"http://localhost:8080/Proj/SignupServlet",
				/*data: */JSON.stringify(newUserDetails)
		/*	}*/).then(
					function(response){
						console.log("SignupServlet called, response is "+response.data);
						console.log("username is "+response.data.userName);
						if((response.data.userName == "Error username taken")||(response.data.userName == "Error nickname taken")){
							console.log("there was an error");
								jam.ErrorExists = true;
							jam.ErrorMsg = response.data.userName;
							}else{
								jam.userName = response.data.userName;
								jam.password=response.data.password;
								jam.userNickname=response.data.userNickname;
								jam.userDescription=response.data.userDescription;
								jam.photoURL=response.data.photoURL;
								jam.islogged = response.data.islogged;
								jam.lastLogged =new Date(response.data.lastLogged)
								jam.lastlastlogged = new Date(response.data.lastLogged)
								jam.welcomeScreen=false;
								jam.showChannels = true;
								jam.ErrorExists = false;

							}

					});
		};

		/*function that is called when a user chooses to logout*/
		jam.logout = function(){
			var UserDetails = {
					userName : jam.userNickname,
					lastActiveChannel : jam.ActiveChannel
			};
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/LogoutServlet',
				data: JSON.stringify(UserDetails)
			}).then(
					function(response){
						if ((typeof response.data === 'string') && (response.data.trim() == "success")){

							jam.userName="";
							console.log("userName is "+jam.userName);
							jam.password="";
							jam.userNickname="";
							jam.userDescription="";
							jam.photoURL="";
							jam.islogged = false;
							jam.lastLogged =new Date();
							jam.lastlastlogged = new Date();
							jam.welcomeScreen=true;
							jam.showChannels = false;
							jam.showThreads = false;
							jam.createChannelScreen = false;
							jam.showSearchResults = false;
						}
					});
		};



		  /*function that removes a specific public channel from a user's channels*/
		jam.publicChannelRemove = function(channelName){
			var subscription = {
					username : jam.userName,
					channel : channelName,
					type : "public"
			};
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/UnsubscribeServlet',
				data: JSON.stringify(subscription)
			}).then(
					function(response){
						console.log("UnsubscribeServlet called, response is "+response.data);
						if (response.data == "success"){
							/*removing the channel from users channel list on screen*/
							var index = -2;
							for (var i=0;i<jam.UserPublicChannels.length;i++)
								if (jam.UserPublicChannels[i].channel == subscription.channel)
									index = i;
							if (index > -1){
								jam.UserPublicChannels.splice(index, 1);
							}
						}
					});
		};

		/*function that removes a specific private channel from a user's channels*/
		jam.privateChannelRemove = function(participanta,participantb){
			var subscription = {
					first : participanta,
					second : participantb,
					user: jam.userNickname
			};
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/RemovePrivateChannelServlet',
				data: JSON.stringify(subscription)
			}).then(
					function(response){
						console.log("RemovePrivateChannelServlet called, response is "+response.data);
						if (response.data != "fail"){
							/*removing the channel from users channel list on screen*/
							var index = -2;
							for (var i=0;i<jam.UserPrivateChannels.length;i++)
								if (jam.UserPrivateChannels[i].channel == response.data.name)
									index = i;
							if (index > -1){
								jam.UserPrivateChannels.splice(index, 1);

							}
						}
					});
		};

		/*function that set displayed channel to selected channel*/
		jam.setChannel = function(channelName){
		jam.showSearchResults = false;
		jam.createChannelScreen = false;
			if ((jam.ActiveChannel != undefined) && (jam.ActiveChannel !=""))
				jam.removeUserFromChannelList(jam.ActiveChannel);
			jam.ActiveChannel = channelName;
			var arr =[];
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/GetThreadsServlet/channelName/'+channelName
			}).then(
					function(data){
						connect();
						console.log("GetThreadsServlet called, response is "+data.data);
						if (data.data != undefined){
							/*extracting threads from database according to channel*/
							jam.ThreadsToShow=data.data;
							console.log("ThreadsToShow are "+jam.ThreadsToShow);
							/*resetting mentions and notifications indicators for channel*/
							for(var i=0;i<jam.UserPublicChannels.length;i++){
								if (jam.UserPublicChannels[i].channel == channelName){
									jam.UserPublicChannels[i].mentions = 0;
									jam.UserPublicChannels[i].notifications = 0;
								}
							}
							/*resetting mentions and notifications indicators for channel*/
							for(var i=0;i<jam.UserPrivateChannels.length;i++){
								if (jam.UserPrivateChannels[i].name == channelName){
									jam.UserPrivateChannels[i].mentions = 0;
									jam.UserPrivateChannels[i].notifications = 0;
								}
							}
							/*setting replies for all extracted threads to hidden*/
							for (var i=0;i<jam.ThreadsToShow.length;i++){
								(jam.ThreadsToShow[i])["showReplies"]=false;
								jam.ThreadsToShow[i].replies=[];
							}

							/*setting view to show threads and to mark active channel*/
							console.log("threads to show length is "+jam.ThreadsToShow.length);
							console.log("threads to show length is "+jam.ThreadsToShow[0]);
							if (jam.ThreadsToShow.length > 0){
								jam.lastThreadDate = jam.ThreadsToShow[length-1];
								jam.firstThreadDate = jam.ThreadsToShow[0];
							}
						jam.showThreads = true;
						console.log("showThreads are "+jam.showThreads);
						jam.ActiveChannel = channelName;
						console.log("ActiveChannel are "+jam.ActiveChannel);

						}
						// else return arr;
					});
		};

		// jam.$watch(function(){
		// 	console.log("Digest Loop Fired!");
		// });

		jam.removeUserFromChannelList = function(channelName){
			var data={
				name : channelName
			};
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/RemoveUserFromChannelListServlet',
				data: JSON.stringify(data)
			}).then(
				function(response){
					console.log("data sent from removeUserFromChannelList is "+response.data);
				});
		};

		jam.openPrivateChannel = function(nickname){
			var credentials = {
					usera : jam.userNickname,
					userb : nickname
			};
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetPrivateChatServlet',
				data: JSON.stringify(credentials)
			}).then(
					function(response){
						console.log("data received from openPrivateChannel is "+response.data);
						if (!((typeof response.data=='string')&&(response.data.trim() == 'fail'))){
							jam.setChannel(response.data.channelName);
						}
						else{
							var channelDetails = {
									name : "chat",
									creator : jam.userName,
									created : Date.now(),
									participanta : jam.userNickname,
									participantb : nickname
							};
							$http({
								method: 'POST',
								url: 'http://localhost:8080/Proj/CreatePrivateChatServlet',
								data: JSON.stringify(channelDetails)
							}).then(
									function(response){
										console.log("response from CreatePrivateChatServlet is "+response.data);
										if (response.data != null){
											jam.UserPrivateChannels.push(response.data);
											setChannel(response.data.channelName);
											jam.websocket.send(channelDetails);
										}});
						}
					});
		};


		/*function that fetches the 10 newest threads from databse*/
		jam.getNewestThreads = function(channelName){
			var arr =[];
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/GetNewestThreadsServlet/channelName/'+channelName
			}).then(
					function(response){
						console.log("GetNewestThreadsServlet called, response is "+response.data);
						if (response.data != undefined){
							console.log("response.data was not undefined")
							/*removing the channel from users channel list on screen*/
							jam.ThreadsToShow=response.data;
							for (var i=0;i<jam.ThreadsToShow.length;i++){
								(jam.ThreadsToShowThreadsToShow[i])["showReplies"]=false;
								jam.ThreadsToShow[i].replies=[];
							}
							jam.showThreads =true;
							jam.ActiveChannel = channelName;
							/*updating lastThreadDate for the scrollupdown functions*/
							if (jam.ThreadsToShow.length > 0){
								jam.lastThreadDate = jam.ThreadsToShow[length-1].lastUpdate;//newest
								console.log("lastThreadDate is "+jam.lastThreadDate);
								jam.firstThreadDate = jam.ThreadsToShow[0].lastUpdate;//oldest
								console.log("firstThreadDate is "+jam.firstThreadDate);
							}
						}
						// else return arr;
					});
		};

		/*function that fetches next 10 threads from databse*/
		jam.getNextTenThreadsUp = function(channelName){
			var channelToGet = {
					name : channelName,
					date : jam.firstThreadDate,
					username : jam.userName
			};
			var arr =[];
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetNextTenThreadsUpServlet',
				data: JSON.stringify(channelToGet)
			}).then(
					function(data){
						console.log("GetNextTenThreadsUpServlet called, response is "+response.data);
						if (response.data != null){
							/*removing the channel from users channel list on screen*/
							var newThreads = response.data;
							for (var i=0;i<newThreads.length;i++){
								(newThreads[i])["showReplies"]=false;
							}
						jam.showThreads = true;
							jam.ActiveChannel = channelName;
							/*actually adding the newly acquired threads to content*/
							for (var i=0;i<newThreads.length;i++){
								/*add elemet to beginning of thread array*/
								jam.ThreadsToShow.unshift(newThreads[i]);
								/*remove elemet from end of thread array*/
								jam.ThreadsToShow.pop();
							}

							/*updating lastThreadDate for the scrollupdown functions*/
							jam.lastThreadDate = jam.ThreadsToShow[length-1].lastUpdate;//newest
							jam.firstThreadDate = jam.ThreadsToShow[0].lastUpdate;//oldest
						}
						else return arr;
					});
		};


		/*function that fetches next 10 threads from databse*/
		jam.getNextTenThreadsDown = function(channelName){
			var channelToGet = {
					name : channelName,
					date : jam.lastThreadDate,
					username : jam.userName
			};
			var arr =[];
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetNextTenThreadsDownServlet',
				data: JSON.stringify(channelToGet)
			}).then(
					function(response){
						console.log("GetNextTenThreadsDownServlet called, response is "+response.data);
						if (response.data != null){
							/*removing the channel from users channel list on screen*/
							var newThreads = response.data;
							for (var i=0;i<newThreads.length;i++){
								(newThreads[i])["showReplies"]=false;
							}
						jam.showThreads = true;
							jam.ActiveChannel = channelName;
							/*actually adding the newly acquired threads to content*/
							for (var i=0;i<newThreads.length;i++){
								/*add elemet to end of thread array*/
								jam.ThreadsToShow.push(newThreads[i]);
								/*remove elemet from beginning of thread array*/
								jam.ThreadsToShow.shift();
							};
							/*updating lastThreadDate for the scrollupdown functions*/
							jam.lastThreadDate = jam.ThreadsToShow[length-1].lastUpdate;//newest
							jam.firstThreadDate = (jam.ThreadsToShow[0]).lastUpdate;//oldest
						}
						else return arr;
					});
		};

		/*function that fetches next thread from databse on scrollup*/
		jam.getNextThreadUp = function(channelName){
			var channelToGet = {
					name : channelName,
					date : jam.firstThreadDate,
					username : jam.userName
			};
			var arr =[];
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetNextThreadUpServlet',
				data: JSON.stringify(channelToGet)
			}).then(
					function(response){
						console.log("GetNextThreadUpServlet called, response is "+response.data);
						if (response.data != null){
							var newThread = response.data;
							newThread["showReplies"]=false;
						// 	sessionStorage.setItem("showThreads",JSON.stringify(true));
						// jam.showThreads = JSON.parse(sessionStorage.getItem("showThreads"));
							// sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
							// jam.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
							/*add elemet to beginning of thread array*/
							jam.ThreadsToShow.unshift(newThread);
							/*remove elemet from end of thread array*/
							jam.ThreadsToShow.pop();

							/*updating lastThreadDate for the scrollupdown functions*/
							jam.lastThreadDate = jam.ThreadsToShow[length-1].lastUpdate;//newest
							jam.firstThreadDate = jam.ThreadsToShow[0].lastUpdate;//oldest
						}
						//else return arr;
					});
		};


		/*function that fetches next thread from databse on scroll down*/
		jam.getNextThreadDown = function(channelName){
			var channelToGet = {
					name : channelName,
					date : jam.lastThreadDate,
					username : jam.userName
			};
			var arr =[];
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetNextThreadDownServlet',
				data: JSON.stringify(channelToGet)
			}).then(
					function(response){
						console.log("getNextThreadUp result "+response.data);
						if (response.data != null){
							var newThread = response.data;
							newThread["showReplies"]=false;
						// 	sessionStorage.setItem("showThreads",JSON.stringify(true));
						// jam.showThreads = JSON.parse(sessionStorage.getItem("showThreads"));
						// 	sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
						// 	jam.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
							/*add elemet to end of thread array*/
							jam.ThreadsToShow.push(newThread);
							/*remove elemet from beginning of thread array*/
							jam.ThreadsToShow.shift();
							/*updating lastThreadDate for the scrollupdown functions*/
							jam.lastThreadDate = jam.ThreadsToShow[length-1].lastUpdate;//newest
							jam.firstThreadDate = jam.ThreadsToShow[0].lastUpdate;//oldest
						}
						//else return arr;
					});
		};

		/*scrolling part - when scrolling bring more threads*/
		var win = $(window);
		jam.lastY = win.scrollTop();
		win.on('scroll',function(){
			var currY = win.scrollTop();
			var y = ((currY > jam.lastY)? 'down' : ((currY===jam.lastY)? 'none':'up'));
			console.log("showThreads is checked for scrolling and it is "+jam.showThreads);
			if((jam.showThreads)&&(jam.ThreadsToShow.length > 9)){
				if (y=='down')
					jam.getNextThreadDown(jam.ActiveChannel);
				else if (y=='up')
					jam.getNextThreadUp(jam.ActiveChannel);
				jam.lastY = currY;
			}
		});

		/*fucntion that checks whether the current checked channel from channels list is active*/
		jam.isActiveChannel = function(channelName){
			return jam.ActiveChannel == channelName;
		};

		jam.fetchReplies = function(thread){
			console.log("fetch replies called");
			console.log(JSON.stringify(thread));
			thread.showReplies = !thread.showReplies;
			if (thread.showReplies)
				jam.getReplies(thread);
		};
		/*function that gets a thread id and returns an array of replies to it*/
		jam.getReplies = function(thread){
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/GetRepliesServlet/threadID/'+thread.id
			}).then(
					function(response){
						console.log("data from getReplies is "+response.data);
						if (!((typeof response.data === 'string') && (response.data.trim() == "fail"))){
							/*returning array of replies*/
							for (var i=0;i<response.data.length;i++){
								(response.data[i])["showReplies"]=false;
								response.data[i].replies = [];
							}
							thread.replies = response.data;
						}
					});
		};

		/*function to update flags for when wanting to post a reply to a thread*/
		jam.addReply = function(thread_id,thread_author){
			/*double click will cancel replying*/
			if (jam.replyParentId == thread_id){
					jam.replyIndication = false;
					console.log("replyIndication is "+jam.replyIndication);
					jam.replyParentId =-1;
					console.log("replyParentId is "+jam.replyParentId);
					jam.replyTo = "";
					console.log("replyTo is "+jam.replyTo);
					jam.message_input=jam.message_input.replace("@"+thread_author+":","");
					console.log("message input is "+jam.message_input);
			}
			else {
				/*you can only reply to a single message*/
				if (jam.replyParentId == -1){
					jam.replyParentId =thread_id;
					console.log("replyParentId is "+jam.replyParentId);
					jam.replyIndication = true;
					console.log("replyIndication is "+jam.replyIndication);
					jam.replyTo= "@"+thread_author+":";
					console.log("replyTo is "+jam.replyTo);
					if (jam.message_input != undefined)
						jam.message_input=jam.replyTo+jam.message_input;
					else jam.message_input=jam.replyTo;
					console.log("message input is "+jam.message_input);
				}
				else{
					jam.replyParentId =thread_id;
					console.log("replyParentId is "+jam.replyParentId);
					jam.replyIndication = true;
					console.log("replyIndication is "+jam.replyIndication);
					jam.replyTo= "@"+thread_author+":";
					console.log("replyTo is "+jam.replyTo);
					if (jam.message_input != undefined)
						jam.message_input=jam.replyTo+jam.message_input;
					else jam.message_input=jam.replyTo;
					console.log("message input is "+jam.message_input);
				}
			}
		};

		/*function to handle the case that the user sends a message or a thread*/
		jam.messageSubmit = function(){
			/*first, handling the case when a new thread is being posted*/
			if (jam.replyIndication == false){
				var message = {
						author : jam.userNickname,
						channel : jam.ActiveChannel,
						content : jam.message_input,
						isThread : true,
						isReplyTo : -1,
						/*threadID will be updated before entering to database on server side*/
						threadID : -1,
						lastUpdate: Date.now(),
						date: Date.now()
				};
				console.log("you are about to send a message to "+jam.ActiveChannel);
				/*adding new thread to database*/
				$http({
					method: 'POST',
					url: 'http://localhost:8080/Proj/PostThreadServlet',
					data: JSON.stringify(message)
				}).then(
						function(response){
						if (!((typeof response.data === 'string') && (response.data.trim() == "fail"))){
							console.log("PostThreadServlet called, response is "+response.data);
								//sending the message in websocket
								jam.websocket.send(JSON.stringify(message));
								//reseting the chat typing field
								jam.message_input="";
								if (jam.ThreadsToShow.length>9)
									jam.ThreadsToShow.unshift();
								jam.ThreadsToShow.push(response.data);
								jam.showThreads = true;

							}
						});
			}else{
		/*now handling the case where the posted message is a reply*/
				var message = {
						author : jam.userNickname,
						channel : jam.ActiveChannel,
						content : jam.message_input,
						isThread : false,
						isReplyTo : jam.replyParentId,
						/*threadID will be updated before entering to database on server side*/
						threadID : -1,
						lastUpdate: Date.now(),
						date: Date.now()
				};
				/*adding new message to database*/
				$http({
					method: 'POST',
					url: 'http://localhost:8080/Proj/PostReplyServlet',
					data: JSON.stringify(message)
				}).then(
						function(response){
							console.log("PostReplyServlet called, response is "+response.data);
							if (!((typeof response.data === 'string') && (response.data.trim() == "fail"))){
								//sending the message in websocket
								jam.websocket.send(JSON.stringify(message));
								//reseting the chat typing field
								jam.message_input="";
								for (var i=0;i<jam.ThreadsToShow.length;i++)
									if (jam.ThreadsToShow[i].id==jam.replyParentId)
										jam.ThreadsToShow[i].numberOfReplies++;
							}
						});
			}
		};

		/*function that recieves username and channelname and returns true if user is subscribed to channel or false otherwise*/
		jam.checkSubscription = function(channelName){
			for (var i=0;i<jam.UserPublicChannels.length;i++){
				if ((jam.UserPublicChannels[i]).channel == channelName)
					return "public";
			}
			for (var i=0;i<jam.UserPrivateChannels.lenght;i++){
				if ((jam.UserPrivateChannels[i]).name == channelName)
					return "private";
			}
			return "none";
		};
		/*function that updates notifications count for a specific public channel according to a new message for subscribed member that
		is not currently in the channel*/
		jam.updateNotificationsPublic = function(channelName){
			for (var i=0;i<jam.UserPublicChannels.length;i++){
				if ((jam.UserPublicChannels[i]).channel == channelName){
					(jam.UserPublicChannels[i]).notifications++;
				}
			}
		};
		/*function that updates notifications count for a specific private channel according to a new message for subscribed member that
		is not currently in the channel*/
		jam.updateNotificationsPrivate = function(channelName){
			for (var i=0;i<jam.UserPrivateChannels.length;i++)
				if ((jam.UserPrivateChannels[i]).name == channelName)
					(jam.UserPrivateChannels[i]).notifications++;

		};

		/*function that updates mentions count for a specific public channel according to a new message for subscribed member that
		is not currently in the channel*/
		jam.updateMentionsPublic = function(channelName){
			for (var i=0;i<jam.UserPublicChannels.length;i++)
				if ((jam.UserPublicChannels[i]).channel == channelName)
					(jam.UserPublicChannels[i]).mentions++;
		};

		/*function that updates mentions count for a specific private channel according to a new message for subscribed member that
		is not currently in the channel*/
		jam.updateMentionsPrivate = function(channelName){
			for (var i=0;i<jam.UserPrivateChannels.length;i++)
				if ((jam.UserPrivateChannels[i]).name == channelName)
					(jam.UserPrivateChannels[i]).mentions++;
		};

		/*function that updates notifications count for a specific public channel according to a new message for subscribed member that
		has just signed in to app*/
		jam.updateNotificationsOnLoadPublic = function(subscription){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : jam.userNickname,
					previousLog : jam.lastlastlogged,
					channel : subscription.channel
			}

			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetNotificationsServlet',
				data: JSON.stringify(userDetails)
			}).then(
					function(response){
						console.log("data recieved from GetNotificationsServlet is "+response.data);
						if (response.data != 0)
						/*only if the number of notifications is different than 0 we return it*/
							return response.data;
					});
		};

		/*function that updates mentions count for a specific public channel according to a new message for subscribed member that
		has just signed in to app*/
		jam.updateMentionsOnLoadPublic = function(subscription){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : jam.userNickname,
					previousLog : jam.lastlastlogged,
					channel : subscription.channel
			}
			/*adding new message to database*/
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetMentionsServlet',
				data: JSON.stringify(userDetails)
			}).then(
					function(response){
						console.log("data recieved from GetMentionsServlet is "+response.data);
						if (response.data != 0)
						/*only if the number of mentions is different than 0 we return it*/
							return response.data;
					});
		};

		/*function that updates notifications count for a specific private channel according to a new message for subscribed member that
		has just signed in to app*/
		jam.updateNotificationsOnLoadPrivate = function(channel){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : jam.userNickname,
					previousLog : jam.lastlastlogged,
					channel : channel.name
			}
			/*adding new message to database*/
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetNotificationsServlet',
				data: JSON.stringify(userDetails)
			}).then(
					function(response){
						console.log("data recieved from GetNotificationsServlet is "+response.data);
						if (response.data != 0)
						/*only if the number of mentions is different than 0 we return it*/
							return response.data;
					});
		};

		/*function that updates mentions count for a specific private channel according to a new message for subscribed member that
		has just signed in to app*/
		jam.updateMentionsOnLoadPrivate = function(channel){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : jam.userNickname,
					previousLog : jam.lastlastlogged,
					channel : channel.name
			}
			/*adding new message to database*/
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetMentionsServlet',
				data: JSON.stringify(userDetails)
			}).then(
					function(response){
						console.log("data recieved from GetMentionsServlet is "+response.data);
						/*only if the number of mentions is different than 0 we return it*/
						if (response.data != 0)
							return response.data;
					});
		};

		/*function that sets the view of create public channel pannel*/
		jam.createPublicChannel = function(){
				console.log("we are here at createPublicChannel");
				jam.welcomeScreen=false;
				console.log(jam.welcomeScreen);
				jam.createChannelScreen = true;
				jam.showSearchResults = false;
				console.log(jam.createChannelScreen);
				jam.showThreads = false;
		};

		/*function that actually creates the public channel upon clicking on create button on create public channel pannel*/
		jam.publicChannelCreate = function(){
			var newChannelDetails = {
					channelName : jam.channel_name,
					channelType : null,
					channelCreator : jam.userName,
					//channelCreationTime : Date.now(),
					channelDescription :jam.channel_description
			};

			console.log(JSON.stringify(newChannelDetails));
			/*adding new message to database*/
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/CreatePublicChannelServlet',
				data: JSON.stringify(newChannelDetails)
			}).then(
					function(response){
						console.log("CreateChannelServlet called, response is "+response.data);
						if ((typeof response.data === 'string') && (response.data.trim() != "fail")){
							var subscription = data;
							jam.UserPublicChannels.push(subscription);
						}
					});
				jam.welcomeScreen=false;
			jam.createChannelScreen = false;
		};

		jam.searchChannels = function(){
			jam.showThreads = false;
			jam.createChannelScreen = false;
			var searchInfo = {
					parameter : "name",
					value : jam.channelSearchText
			}
			if ($("#radioNick").prop("checked")){
				/*search by nickname*/
				searchInfo.parameter = "nick";
			}
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/SearchPublicChannelsServlet',
				data: JSON.stringify(searchInfo)
			}).then(
					function(response){
						console.log("SearchPublicChannelsServlet called, response is "+response.data);
						if (response.data != undefined){
							var channels = response.data;
							//empty search results array first*/
							jam.searchPublicChannels=[];
							for (var i=0;i<channels.length;i++)
								jam.searchPublicChannels.push(channels[i]);
						jam.showSearchResults = true;
						console.log("channels are "+jam.searchPublicChannels);
						console.log("showSearchResults is "+jam.showSearchResults);
						}
					});
		};

		jam.channelSubscribe = function(channelName){
		jam.showSearchResults = false;
			var subscriptionInfo = {
					channel : channelName,
					user : jam.userNickname
			}
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/PublicChannelSubscribeServlet',
				data: JSON.stringify(subscriptionInfo)
			}).then(
					function(response){
						console.log("PublicChannelSubscribeServlet called, response is "+response.data);
						if ((typeof response.data === 'string') && (response.data.trim() != "fail")){
							var channel = response.data;
							jam.searchPublicChannels.push(channel);
							jam.searchPublicChannels[jam.searchPublicChannels.length-1].mentions = 0;
							jam.searchPublicChannels[jam.searchPublicChannels.length-1].notifications = 0;

						}
					});
		};

		//  jam.init();
		});
		};

// })();
