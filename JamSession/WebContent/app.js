(function () {
	'use strict';

	angular.module('jamSession', [])
	.controller('jamSessionController', jamSessionController);

	jamSessionController.$inject = ['$scope','$http'];
	function jamSessionController($scope,$http) {
		/*welcomeScreen is true when we display login or signup panel and false after user is logged into site*/
		// Save data to sessionStorage
		if ((sessionStorage.getItem("welcomeScreen"))==null)
			sessionStorage.setItem("welcomeScreen",false);
		$scope.welcomeScreen=sessionStorage.getItem("welcomeScreen");
		console.log($scope.welcomeScreen);

		if ((sessionStorage.getItem("createChannelScreen"))==null)
			sessionStorage.setItem("createChannelScreen",false);
		$scope.createChannelScreen = sessionStorage.getItem("createChannelScreen");
		console.log("create channel is "+$scope.createChannelScreen);

		if ((sessionStorage.getItem("showThreads"))==null)
			sessionStorage.setItem("showThreads",true);
		$scope.showThreads = sessionStorage.getItem("showThreads");
		console.log($scope.showThreads);

		if ((sessionStorage.getItem("loading"))==null)
			sessionStorage.setItem("loading",false);
		$scope.loading = (sessionStorage.getItem("loading"));
		console.log($scope.loading);

		if ((sessionStorage.getItem("showSideBar"))==null)
			sessionStorage.setItem("showSideBar",true);
		$scope.showSideBar = sessionStorage.getItem("showSideBar");
		console.log($scope.showSideBar);

		if ((sessionStorage.getItem("ErrorExists"))==null)
			sessionStorage.setItem("ErrorExists",false);
		$scope.ErrorExists = sessionStorage.getItem("ErrorExists");
		console.log($scope.ErrorExists);

		if ((sessionStorage.getItem("showChannels"))==null)
			sessionStorage.setItem("showChannels",false);
		$scope.showChannels = sessionStorage.getItem("showChannels");
		console.log($scope.showChannels);

		if ((sessionStorage.getItem("ErrorMsg"))==null)
			sessionStorage.setItem("ErrorMsg",JSON.stringify(""));
		$scope.ErrorMsg = sessionStorage.getItem("ErrorMsg");
		console.log($scope.ErrorMsg);

		var array=[];
		if ((sessionStorage.getItem("ThreadsToShow"))==null)
			sessionStorage.setItem("ThreadsToShow",JSON.stringify(array));
		$scope.ThreadsToShow=JSON.parse(sessionStorage.getItem("ThreadsToShow"));
		console.log($scope.ThreadsToShow);

		if ((sessionStorage.getItem("UserPublicChannels"))==null)
			sessionStorage.setItem("UserPublicChannels",JSON.stringify(array));
		$scope.UserPublicChannels=JSON.parse(sessionStorage.getItem("UserPublicChannels"));
		console.log($scope.UserPublicChannels);

		if ((sessionStorage.getItem("UserPrivateChannels"))==null)
			sessionStorage.setItem("UserPrivateChannels",JSON.stringify(array));
		$scope.UserPrivateChannels=JSON.parse(sessionStorage.getItem("UserPrivateChannels"));
		console.log($scope.UserPrivateChannels);

		if ((sessionStorage.getItem("ActiveChannel"))==null)
			sessionStorage.setItem("ActiveChannel",JSON.stringify(""));
		$scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));

		if ((sessionStorage.getItem("lastThreadDate"))==null)
			sessionStorage.setItem("lastThreadDate",JSON.stringify(Date.now()));
		$scope.lastThreadDate = JSON.parse(sessionStorage.getItem("lastThreadDate"));

		if ((sessionStorage.getItem("firstThreadDate"))==null)
			sessionStorage.setItem("firstThreadDate",JSON.stringify(Date.now()));
		$scope.firstThreadDate = JSON.parse(sessionStorage.getItem("firstThreadDate"));

		/*-1 is the value for a message that is not a reply in the variable replyParentId*/
		if ((sessionStorage.getItem("replyParentId"))==null)
			sessionStorage.setItem("replyParentId",JSON.stringify(-1));
		$scope.replyParentId =JSON.parse(sessionStorage.getItem("replyParentId"));

		if ((sessionStorage.getItem("replyIndication"))==null)
			sessionStorage.setItem("replyIndication",false);
		$scope.replyIndication = sessionStorage.getItem("replyIndication");

		if ((sessionStorage.getItem("showSearchResults"))==null)
			sessionStorage.setItem("showSearchResults",false);
		$scope.showSearchResults = sessionStorage.getItem("showSearchResults");
		console.log("showSearchResults is "+$scope.showSearchResults);

		if ((sessionStorage.getItem("numberOfNewMessages"))==null)
			sessionStorage.setItem("numberOfNewMessages",JSON.stringify(0));
		$scope.numberOfNewMessages = JSON.parse(sessionStorage.getItem("numberOfNewMessages"));

		if ((sessionStorage.getItem("numberOfMentions"))==null)
			sessionStorage.setItem("numberOfMentions",JSON.stringify(0));
		$scope.numberOfMentions = JSON.parse(sessionStorage.getItem("numberOfMentions"));

		if ((sessionStorage.getItem("replyTo"))==null)
			sessionStorage.setItem("replyTo",JSON.stringify(""));
		$scope.replyTo=JSON.parse(sessionStorage.getItem("replyTo"));

		if ((sessionStorage.getItem("channel_description"))==null)
			sessionStorage.setItem("channel_description",JSON.stringify(""));
		$scope.channel_description=JSON.parse(sessionStorage.getItem("channel_description"));

		if ((sessionStorage.getItem("channel_name"))==null)
			sessionStorage.setItem("channel_name",JSON.stringify(""));
		$scope.channel_name=JSON.parse(sessionStorage.getItem("channel_name"));

		if ((sessionStorage.getItem("searchPublicChannels"))==null)
			sessionStorage.setItem("searchPublicChannels",JSON.stringify(array));
		$scope.searchPublicChannels=JSON.parse(sessionStorage.getItem("searchPublicChannels"));

		/*tab is the active tab on login signup panel*/
		$scope.tab = "signup";

		/*scope variables to bind and retrieve data from index.html*/
		if ((sessionStorage.getItem("userName"))==null)
			sessionStorage.setItem("userName",JSON.stringify(""));
		$scope.userName=JSON.parse(sessionStorage.getItem("userName"));

		if ((sessionStorage.getItem("password"))==null)
			sessionStorage.setItem("password",JSON.stringify(""));
		$scope.password=JSON.parse(sessionStorage.getItem("password"));

		if ((sessionStorage.getItem("userNickname"))==null)
			sessionStorage.setItem("userNickname",JSON.stringify(""));
		$scope.userNickname=JSON.parse(sessionStorage.getItem("userNickname"));

		if ((sessionStorage.getItem("userDescription"))==null)
			sessionStorage.setItem("userDescription",JSON.stringify(""));
		$scope.userDescription=JSON.parse(sessionStorage.getItem("userDescription"));

		if ((sessionStorage.getItem("photoURL"))==null)
			sessionStorage.setItem("photoURL",JSON.stringify(""));
		$scope.photoURL=JSON.parse(sessionStorage.getItem("photoURL"));

		if ((sessionStorage.getItem("lastLogged"))==null)
			sessionStorage.setItem("lastLogged",JSON.stringify(new Date()));
		$scope.lastLogged =JSON.parse(sessionStorage.getItem("lastLogged"));

		if ((sessionStorage.getItem("lastlastlogged"))==null)
			sessionStorage.setItem("lastlastlogged",JSON.stringify(new Date()));
		$scope.lastlastlogged = JSON.parse(sessionStorage.getItem("lastlastlogged"));

		/*method to change from login to sign up and vice versa on welcome screen*/
		$scope.selectTab = function(setTab) {
			$scope.tab = setTab;
		};

		/*method to change view from login to sign up and vice versa on welcome screen*/
		$scope.isSelected = function(checkTab){
			return $scope.tab === checkTab;
		};



		/*setting up websocket on client to send messages*/
		var wsUri = "ws://"+window.location.host+"/JamSession/"+$scope.userNickname;
		$scope.websocket = new WebSocket(wsUri);
		$scope.websocket.onopen = function(evt){
			console.log("connected to server");
		};
		//what happens when we recieve a message
		$scope.websocket.onmessage = function(evt){
			notify(evt.data);
		};
		$scope.websocket.onerror = function(evt){
			console.log('ERROR: '+evt.data);
		};
		$scope.websocket.onclose = function(evt){
			websocket = null;
			console.log("disconnected from server");
		};

		/*posting a message to screen - need to write this*/
		function notify(message) {
			/*if message is a request to open private chat*/
			if (message.hasOwnProperty("participanta")){
				/*get channels properties from server - because this is the recieving participant the channel already exists*/
				var credentials = {
						usera : message.participanta,
						userb : message.participantb
				}
				if (($scope.userNickname == participanta)||($scope.userNickname == participantb)){
					$http({
						method: 'POST',
						url: '/GetPrivateChatServlet',
						data: credentials
					}).then(
							function(data){
								if (data != null){
									/*entering private channel to channels list and updating  it's mentions and notifications*/
									$scope.UserPrivateChannels.push(data);
									($scope.UserPrivateChannels[$scope.UserPrivateChannels.length-1]).setAttribute(mentions,0);
									($scope.UserPrivateChannels[$scope.UserPrivateChannels.length-1]).setAttribute(notifications,1);
									sessionStorage.setItem("UserPrivateChannels",JSON.stringify($scope.UserPrivateChannels));
									$scope.UserPrivateChannels=JSON.parse(sessionStorage.getItem("UserPrivateChannels"));
								}
							});
				}}
			else{
				/*for users that are on the channel that contains the new message, the page should refresh according to the thread updated*/
				if ($scope.ActiveChannel == message.channel){
					sessionStorage.setItem("loading",true);
					$scope.loading = (sessionStorage.getItem("loading"));
					/*getting 10 newest threads to show*/
					getNewestThreads(ActiveChannel);
					sessionStorage.setItem("loading",false);
					$scope.loading = (sessionStorage.getItem("loading"));
				}
				else{
					/*for users that are subscribed to the channel that contains the new message but are not there there should be a notification*/
					var isSubscribed = checkSubscription(message.channel);
					if (isSubscribed == "public")
						updateNotificationsPublic(message.channel);
					if (isSubscribed == "private")
						updateNotificationsPrivate(message.channel);
					/*if user is subscribed to channel and is mentioned in the new message posted he should get a notification*/
					if (message.content.includes("@"+$scope.userNickname)){
						if (isSubscribed == "public")
							updateMentionsPublic(message.channel);
						if (isSubscribed == "private")
							updateMentionsPrivate(message.channel);
					}
				}
			}
		}

		/*method activated when user tries to login to site*/
		$scope.userLogin = function(){
			var userCredentials = {
					userName : $scope.userName,
					password : $scope.password
			};
			$http({
				method: 'POST',
				url: '/LoginServlet',
				data: userCredentials
			}).then(
					function(data){
						if (data != null){
							sessionStorage.setItem("userName",JSON.stringify(data.userName));
							$scope.userName=JSON.parse(sessionStorage.getItem("userName"));
							sessionStorage.setItem("password",JSON.stringify(data.password));
							$scope.password=JSON.parse(sessionStorage.getItem("password"));
							sessionStorage.setItem("userNickname",JSON.stringify(data.userNickname));
							$scope.userNickname=JSON.parse(sessionStorage.getItem("userNickname"));
							sessionStorage.setItem("userDescription",JSON.stringify(data.userDescription));
							$scope.userDescription=JSON.parse(sessionStorage.getItem("userDescription"));

							sessionStorage.setItem("photoURL",JSON.stringify(data.photoURL));
							$scope.photoURL=JSON.parse(sessionStorage.getItem("photoURL"));
							$scope.islogged = data.islogged;
							sessionStorage.setItem("lastlastlogged",JSON.stringify(new Date(data.lastlogged)));
							$scope.lastlastlogged = JSON.parse(sessionStorage.getItem("lastlastlogged"));
							sessionStorage.setItem("lastLogged",JSON.stringify(Date.now()));
							$scope.lastLogged =JSON.parse(sessionStorage.getItem("lastLogged"));
							sessionStorage.setItem("welcomeScreen",false);
							$scope.welcomeScreen=sessionStorage.getItem("welcomeScreen");
							getUserPublicChannels($scope.user.userNickname);
							getUserPrivateChannels($scope.user.userNickname);
							sessionStorage.setItem("showChannels",true);
							$scope.showChannels = sessionStorage.getItem("showChannels");
						}
					});
		};

		/*method activated when user tries to signup to site*/
		$scope.userSignUp = function(){
			var newUserDetails = {
					userName : $scope.userName,
					password : $scope.password,
					userNickname : $scope.userNickname,
					userDescription : $scope.userDescription,
					userPhotoURL : $scope.photoURL
			};
			$http({
				method: 'POST',
				url: '/SignupServlet',
				data: newUserDetails
			}).then(
					function(data){
						if (data != null){
							if ((data.userName == "Error username taken")||(data.userName == "Error nickname taken")){
								sessionStorage.setItem("ErrorExists",true);
								$scope.ErrorExists = sessionStorage.getItem("ErrorExists");
								sessionStorage.setItem("ErrorMsg",JSON.stringify(data.username));
								$scope.ErrorMsg = sessionStorage.getItem("ErrorMsg");
							}
							else{
								sessionStorage.setItem("userName",JSON.stringify(data.userName));
								$scope.userName=JSON.parse(sessionStorage.getItem("userName"));
								sessionStorage.setItem("password",JSON.stringify(data.password));
								$scope.password=JSON.parse(sessionStorage.getItem("password"));
								sessionStorage.setItem("userNickname",JSON.stringify(data.userNickname));
								$scope.userNickname=JSON.parse(sessionStorage.getItem("userNickname"));
								sessionStorage.setItem("userDescription",JSON.stringify(data.userDescription));
								$scope.userDescription=JSON.parse(sessionStorage.getItem("userDescription"));

								sessionStorage.setItem("photoURL",JSON.stringify(data.photoURL));
								$scope.photoURL=JSON.parse(sessionStorage.getItem("photoURL"));
								$scope.islogged = data.islogged;
								sessionStorage.setItem("lastLogged",JSON.stringify(new Date(data.lastLogged)));
								$scope.lastLogged =JSON.parse(sessionStorage.getItem("lastLogged"));
								sessionStorage.setItem("lastlastlogged",JSON.stringify(new Date(data.lastlogged)));
								$scope.lastlastlogged = JSON.parse(sessionStorage.getItem("lastlastlogged"));
								sessionStorage.setItem("welcomeScreen",false);
								$scope.welcomeScreen=sessionStorage.getItem("welcomeScreen");
								getUserPublicChannels($scope.user.userNickname);
								getUserPrivateChannels($scope.user.userNickname);
								sessionStorage.setItem("showChannels",true);
								$scope.showChannels = sessionStorage.getItem("showChannels");
							}
						}
					});
		};

		/*function that is called when a user chooses to logout*/
		$scope.logout = function(){
			var UserDetails = {
					userName : $scope.userNickname,
					lastActiveChannel : user.ActiveChannel
			};
			$http({
				method: 'POST',
				url: '/LogoutServlet',
				data: UserDetails
			}).then(
					function(data){
						if (data == "success"){
							sessionStorage.setItem("userName",JSON.stringify(""));
							$scope.userName=JSON.parse(sessionStorage.getItem("userName"));
							sessionStorage.setItem("password",JSON.stringify(""));
							$scope.password=JSON.parse(sessionStorage.getItem("password"));
							sessionStorage.setItem("userNickname",JSON.stringify(""));
							$scope.userNickname=JSON.parse(sessionStorage.getItem("userNickname"));
							sessionStorage.setItem("userDescription",JSON.stringify(""));
							$scope.userDescription=JSON.parse(sessionStorage.getItem("userDescription"));

							sessionStorage.setItem("photoURL",JSON.stringify(""));
							$scope.photoURL=JSON.parse(sessionStorage.getItem("photoURL"));
							$scope.islogged = false;
							sessionStorage.setItem("lastLogged",JSON.stringify(new Date()));
							$scope.lastLogged =JSON.parse(sessionStorage.getItem("lastLogged"));
							sessionStorage.setItem("lastlastlogged",JSON.stringify(new Date(data.lastlogged)));
							$scope.lastlastlogged = JSON.parse(sessionStorage.getItem("lastlastlogged"));
							sessionStorage.setItem("welcomeScreen",true);
							$scope.welcomeScreen=sessionStorage.getItem("welcomeScreen");
							sessionStorage.setItem("showChannels",false);
							$scope.showChannels = sessionStorage.getItem("showChannels");
						}
					});
		};

		/*function that gets user's public channels on login*/
		$scope.getUserPublicChannels = function(nickname){
			$http({
				method: 'GET',
				url: '/FindSubscriptionServlet',
			}).then(
					function(data){
						for (x in data){
							$scope.UserPublicChannels.push(data.x);
						}

						for(i=0;i<$scope.UserPublicChannels.length;i++){
							($scope.UserPublicChannels[i]).setAttribute(mentions,0);
							($scope.UserPublicChannels[i]).setAttribute(notifications,0);
							($scope.UserPublicChannels[i]).notifications = updateNotificationsOnLoadPublic($scope.UserPublicChannels[i]);
							($scope.UserPublicChannels[i]).mentions = updateMentionsOnLoadPublic($scope.UserPublicChannels[i]);

							sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPublicChannels));

						}
					});
		};

		/*function that gets user's private channels on login*/
		$scope.getUserPrivateChannels = function(nickname){
			$http({
				method: 'GET',
				url: '/FindPrivateChannelsServlet',
			}).then(
					function(data){
						for (x in data){
							$scope.UserPrivateChannels.push(data.x);
						}
						for(i=0;i<$scope.UserPublicChannels.length;i++){
							($scope.UserPrivateChannels[i]).setAttribute(mentions,0);
							($scope.UserPrivateChannels[i]).setAttribute(notifications,0);
							($scope.UserPrivateChannels[i]).notifications = updateNotificationsOnLoadPrivate(($scope.UserPrivateChannels[i]));
							($scope.UserPrivateChannels[i]).mentions = updateMentionsOnLoadPrivate(($scope.UserPrivateChannels[i]));
						}
						sessionStorage.setItem("UserPrivateChannels",JSON.stringify($scope.UserPrivateChannels));

					});
		};

		``  /*function that removes a specific public channel from a user's channels*/
		$scope.publicChannelRemove = function(channelName){
			var subscription = {
					username : $scope.userName,
					channel : channelName,
					type : "public"
			}
			$http({
				method: 'POST',
				url: '/UnsubscribeServlet',
				data: subscription
			}).then(
					function(data){
						if (data == "success"){
							/*removing the channel from users channel list on screen*/
							var index = $scope.UserPublicChannels.indexOf(subscription);
							if (index > -1){
								$scope.UserPublicChannels.splice(index, 1);
								sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPublicChannels));
							}
						}
					});
		};

		/*function that removes a specific private channel from a user's channels*/
		$scope.privateChannelRemove = function(participanta,participantb){
			var subscription = {
					first : participanta,
					second : participantb,
					user: $scope.userNickname
			}
			$http({
				method: 'POST',
				url: '/RemovePrivateChannelServlet',
				data: subscription
			}).then(
					function(data){
						if (data != "fail"){
							/*removing the channel from users channel list on screen*/
							var index = $scope.UserPrivateChannels.indexOf(data);
							if (index > -1){
								$scope.UserPublicChannels.splice(index, 1);
								sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPublicChannels));

							}
						}
					});
		};

		/*function that set displayed channel to selected channel*/
		$scope.setChannel = function(channelName){
			sessionStorage.setItem("showSearchResults",false);
			$scope.showSearchResults = sessionStorage.getItem("showSearchResults");
			var arr =[];
			$http({
				method: 'GET',
				url: '/GetThreadsServlet/channelName/'+channelName
			}).then(
					function(data){
						if (data != null){
							/*extracting threads from database according to channel*/
							sessionStorage.setItem("ThreadsToShow",JSON.stringify(data));
							$scope.ThreadsToShow=JSON.parse(sessionStorage.getItem("ThreadsToShow"));
							/*resetting mentions and notifications indicators for channel*/
							for(i=0;i<$scope.UserPublicChannels.length;i++){
								if ($scope.UserPublicChannels[i].channel == channelToGet.name){
									$scope.UserPublicChannels[i].mentions = 0;
									$scope.UserPublicChannels[i].notifications = 0;
								}
							}
							sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPublicChannels));
							for(i=0;i<$scope.UserPrivateChannels.length;i++){
								if ($scope.UserPrivateChannels[i].name == channelToGet.name){
									$scope.UserPrivateChannels[i].mentions = 0;
									$scope.UserPrivateChannels[i].notifications = 0;
								}
							}
							sessionStorage.setItem("UserPrivateChannels",JSON.stringify($scope.UserPrivateChannels));
							sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPublicChannels));
							/*setting replies for all extracted threads to hidden*/
							for (i=0;i<ThreadsToShow.length;i++){
								(ThreadsToShow[i])["showReplies"]=false;
							}
							sessionStorage.setItem("ThreadsToShow",JSON.stringify($scope.ThreadsToShow));

							/*setting view to show threads and to mark active channel*/
							sessionStorage.setItem("showThreads",true);
							$scope.showThreads = sessionStorage.getItem("showThreads");
							sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
							$scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
						}
						else return arr;
					});
		};


		$scope.openPrivateChannel = function(nickname){
			var credentials = {
					usera : $scope.userNickname,
					userb : nickname
			}
			$http({
				method: 'POST',
				url: '/GetPrivateChatServlet',
				data: credentials
			}).then(
					function(data){
						if (data != null){
							setChannel(data.name);
						}
						else{
							var channelDetails = {
									name : "chat",
									creator : $scope.userName,
									created : Date.now(),
									participanta : $scope.userNickname,
									participantb : nickname
							};
							$http({
								method: 'POST',
								url: '/CreatePrivateChatServlet',
								data: channelDetails
							}).then(
									function(data){
										if (data != null){
											$scope.UserPrivateChannels.push(data);
											setChannel(data.name);
											$scope.websocket.send(channelDetails);
											sessionStorage.setItem("UserPrivateChannels",JSON.stringify($scope.UserPrivateChannels));
										}});
						}
					});
		};


		/*function that fetches the 10 newest threads from databse*/
		$scope.getNewestThreads = function(channelName){
			var arr =[];
			$http({
				method: 'GET',
				url: '/GetNewestThreadsServlet/channelName/'+channelName
			}).then(
					function(data){
						if (data != null){
							/*removing the channel from users channel list on screen*/
							sessionStorage.setItem("ThreadsToShow",JSON.stringify(data));
							$scope.ThreadsToShow=JSON.parse(sessionStorage.getItem("ThreadsToShow"));
							for (i=0;i<$scope.ThreadsToShow.length;i++)
								(ThreadsToShow[i])["showReplies"]=false;
							sessionStorage.setItem("ThreadsToShow",JSON.stringify($scope.ThreadsToShow));
							sessionStorage.setItem("showThreads",true);
							$scope.showThreads = sessionStorage.getItem("showThreads");
							sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
							$scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
							/*updating lastThreadDate for the scrollupdown functions*/
							sessionStorage.setItem("lastThreadDate",JSON.stringify($scope.ThreadsToShow[length-1].lastUpdate));
							$scope.lastThreadDate = JSON.parse(sessionStorage.getItem("lastThreadDate"));//newest
							sessionStorage.setItem("firstThreadDate",JSON.stringify(($scope.ThreadsToShow[0]).lastUpdate));
							$scope.firstThreadDate = JSON.parse(sessionStorage.getItem("firstThreadDate"));//oldest
						}
						// else return arr;
					});
		};

		/*function that fetches next 10 threads from databse*/
		$scope.getNextTenThreadsUp = function(channelName){
			var channelToGet = {
					name : channelName,
					date : $scope.firstThreadDate,
					username : $scope.userName
			};
			var arr =[];
			$http({
				method: 'POST',
				url: '/GetNextTenThreadsUpServlet',
				data: channelToGet
			}).then(
					function(data){
						if (data != null){
							/*removing the channel from users channel list on screen*/
							var newThreads = data;
							for (i=0;i<newThreads.length;i++){
								(newThreads[i])["showReplies"]=false;
							}
							sessionStorage.setItem("showThreads",true);
							$scope.showThreads = sessionStorage.getItem("showThreads");
							sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
							$scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
							/*actually adding the newly acquired threads to content*/
							for (i=0;i<newThreads.length;i++){
								/*add elemet to beginning of thread array*/
								$scope.ThreadsToShow.unshift(newThreads[i]);
								/*remove elemet from end of thread array*/
								$scope.ThreadsToShow.pop();
								sessionStorage.setItem("ThreadsToShow",JSON.stringify($scope.ThreadsToShow));
							}

							/*updating lastThreadDate for the scrollupdown functions*/
							sessionStorage.setItem("lastThreadDate",JSON.stringify($scope.ThreadsToShow[length-1].lastUpdate));
							$scope.lastThreadDate = JSON.parse(sessionStorage.getItem("lastThreadDate"));//newest
							sessionStorage.setItem("firstThreadDate",JSON.stringify(($scope.ThreadsToShow[0]).lastUpdate));
							$scope.firstThreadDate = JSON.parse(sessionStorage.getItem("firstThreadDate"));//oldest
						}
						else return arr;
					});
		};


		/*function that fetches next 10 threads from databse*/
		$scope.getNextTenThreadsDown = function(channelName){
			var channelToGet = {
					name : channelName,
					date : $scope.lastThreadDate,
					username : $scope.userName
			};
			var arr =[];
			$http({
				method: 'POST',
				url: '/GetNextTenThreadsDownServlet',
				data: channelToGet
			}).then(
					function(data){
						if (data != null){
							/*removing the channel from users channel list on screen*/
							var newThreads = data;
							for (i=0;i<newThreads.length;i++){
								(newThreads[i])["showReplies"]=false;
							}
							sessionStorage.setItem("showThreads",true);
							$scope.showThreads = sessionStorage.getItem("showThreads");
							sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
							$scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
							/*actually adding the newly acquired threads to content*/
							for (i=0;i<newThreads.length;i++){
								/*add elemet to end of thread array*/
								$scope.ThreadsToShow.push(newThreads[i]);
								/*remove elemet from beginning of thread array*/
								$scope.ThreadsToShow.shift();
							};
							sessionStorage.setItem("ThreadsToShow",JSON.stringify($scope.ThreadsToShow));
							/*updating lastThreadDate for the scrollupdown functions*/
							sessionStorage.setItem("lastThreadDate",JSON.stringify($scope.ThreadsToShow[length-1].lastUpdate));
							$scope.lastThreadDate = JSON.parse(sessionStorage.getItem("lastThreadDate"));//newest
							sessionStorage.setItem("firstThreadDate",JSON.stringify(($scope.ThreadsToShow[0]).lastUpdate));
							$scope.firstThreadDate = JSON.parse(sessionStorage.getItem("firstThreadDate"));//oldest
						}
						else return arr;
					});
		};

		/*function that fetches next thread from databse on scrollup*/
		$scope.getNextThreadUp = function(channelName){
			var channelToGet = {
					name : channelName,
					date : $scope.firstThreadDate,
					username : $scope.userName
			};
			var arr =[];
			$http({
				method: 'POST',
				url: '/GetNextThreadUpServlet',
				data: channelToGet
			}).then(
					function(data){
						if (data != null){
							var newThread = data;
							newThread["showReplies"]=false;
							sessionStorage.setItem("showThreads",true);
							$scope.showThreads = sessionStorage.getItem("showThreads");
							sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
							$scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
							/*add elemet to beginning of thread array*/
							$scope.ThreadsToShow.unshift(newThread);
							/*remove elemet from end of thread array*/
							$scope.ThreadsToShow.pop();

							sessionStorage.setItem("ThreadsToShow",JSON.stringify($scope.ThreadsToShow));
							/*updating lastThreadDate for the scrollupdown functions*/
							sessionStorage.setItem("lastThreadDate",JSON.stringify($scope.ThreadsToShow[length-1].lastUpdate));
							$scope.lastThreadDate = JSON.parse(sessionStorage.getItem("lastThreadDate"));//newest
							sessionStorage.setItem("firstThreadDate",JSON.stringify(($scope.ThreadsToShow[0]).lastUpdate));
							$scope.firstThreadDate = JSON.parse(sessionStorage.getItem("firstThreadDate"));//oldest
						}
						else return arr;
					});
		};


		/*function that fetches next thread from databse on scroll down*/
		$scope.getNextThreadDown = function(channelName){
			var channelToGet = {
					name : channelName,
					date : $scope.lastThreadDate,
					username : $scope.userName
			};
			var arr =[];
			$http({
				method: 'POST',
				url: '/GetNextThreadDownServlet',
				data: channelToGet
			}).then(
					function(data){
						if (data != null){
							var newThread = data;
							newThread["showReplies"]=false;
							sessionStorage.setItem("showThreads",true);
							$scope.showThreads = sessionStorage.getItem("showThreads");
							sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
							$scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
							/*add elemet to end of thread array*/
							$scope.ThreadsToShow.push(newThread);
							/*remove elemet from beginning of thread array*/
							$scope.ThreadsToShow.shift();
							sessionStorage.setItem("ThreadsToShow",JSON.stringify($scope.ThreadsToShow));
							/*updating lastThreadDate for the scrollupdown functions*/
							sessionStorage.setItem("lastThreadDate",JSON.stringify($scope.ThreadsToShow[length-1].lastUpdate));
							$scope.lastThreadDate = JSON.parse(sessionStorage.getItem("lastThreadDate"));//newest
							sessionStorage.setItem("firstThreadDate",JSON.stringify(($scope.ThreadsToShow[0]).lastUpdate));
							$scope.firstThreadDate = JSON.parse(sessionStorage.getItem("firstThreadDate"));//oldest
						}
						else return arr;
					});
		};

		/*scrolling part - when scrolling bring more threads*/
		var win = $(window);
		var lastY = win.scrollTop();
		win.on('scroll',function(){
			var currY = win.scrollTop();
			y = (currY > lastY)? 'down' : ((currY===lastY)? 'none':'up');
			if (y=='down')
				getNextThreadDown($scope.ActiveChannel);
			else if (y=='up')
				getNextThreadUp($scope.ActiveChannel);
			lastY = currY;
		});


		/*fucntion that checks whether the current checked channel from channels list is active*/
		$scope.isActiveChannel = function(channelName){
			return $scope.ActiveChannel == channelName;
		};

		/*function that gets a thread id and returns an array of replies to it*/
		$scope.getReplies = function(thread_id){
			var arr =[];
			$http({
				method: 'GET',
				url: '/GetRepliesServlet/threadID/'+thread_id
			}).then(
					function(data){
						if (data != null){
							/*returning array of replies*/
							for (i=0;i<data.length;i++){
								(data[i])["showReplies"]=false;
							}
							return data;
						}
						else return arr;
					});
		};

		/*function to update flags for when wanting to post a reply to a thread*/
		$scope.addReply = function(thread_id,thread_author){
			/*double click will cancel replying*/
			if ($scope.replyParentId == thread_id){
				sessionStorage.setItem("replyIndication",false);
				$scope.replyIndication = sessionStorage.getItem("replyIndication");
				sessionStorage.setItem("replyParentId",JSON.stringify(-1));
				$scope.replyParentId =JSON.parse(sessionStorage.getItem("replyParentId"));
				$scope.message_input.replace("@thread_author","");
			}
			else {
				/*you can only reply to a single message*/
				if ($scope.replyParentId == -1){
					sessionStorage.setItem("replyParentId",JSON.stringify(thread_id));
					$scope.replyParentId =JSON.parse(sessionStorage.getItem("replyParentId"));
					sessionStorage.setItem("replyIndication",true);
					$scope.replyIndication = sessionStorage.getItem("replyIndication");
					sessionStorage.setItem("replyTo",JSON.stringify(thread_author));
					$scope.replyTo=JSON.parse(sessionStorage.getItem("replyTo"));
					$scope.message_input="@thread_author"+$scope.message_input;
				}
			}
		};

		/*function to handle the case that the user sends a message or a thread*/
		$scope.messageSubmit = function(){
			/*first, handling the case when a new thread is being posted*/
			if ($scope.replyIndication == false){
				var message = {
						author : $scope.userNickname,
						channel : $scope.ActiveChannel,
						content : $scope.message_input,
						isThread : true,
						isReplyTo : -1,
						/*threadID will be updated before entering to database on server side*/
						threadID : -1,
						lastUpdate: Date.now(),
						date: Date.now()
				};
				/*adding new thread to database*/
				$http({
					method: 'POST',
					url: '/PostThreadServlet',
					data: message
				}).then(
						function(data){
							if (data == "success"){
								//sending the message in websocket
								$scope.websocket.send(message);
								//reseting the chat typing field
								$scope.message_input="";
							}
						});
			}
			/*now handling the case where the posted message is a reply*/
			else{
				var message = {
						author : $scope.userNickname,
						channel : $scope.ActiveChannel,
						content : $scope.message_input,
						isThread : false,
						isReplyTo : $scope.replyParentId,
						/*threadID will be updated before entering to database on server side*/
						threadID : -1,
						lastUpdate: Date.now(),
						date: Date.now()
				};
				/*adding new message to database*/
				$http({
					method: 'POST',
					url: '/PostReplyServlet',
					data: message
				}).then(
						function(data){
							if (data == "success"){
								//sending the message in websocket
								$scope.websocket.send(message);
								//reseting the chat typing field
								$scope.message_input="";
							}
						});
			}
		};

		/*function that recieves username and channelname and returns true if user is subscribed to channel or false otherwise*/
		$scope.checkSubscription = function(channelName){
			for (i=0;i<$scope.UserPublicChannels.length;i++){
				if (($scope.UserPublicChannels[i]).channel == channelName)
					return "public";
			}
			for (i=0;i<$scope.UserPrivateChannels.lenght;i++){
				if (($scope.UserPrivateChannels[i]).name == channelName)
					return "private";
			}
			return "none";
		};
		/*function that updates notifications count for a specific public channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateNotificationsPublic = function(channelName){
			for (i=0;i<$scope.UserPublicChannels.length;i++){
				if (($scope.UserPublicChannels[i]).channel == channelName){
					($scope.UserPublicChannels[i]).notifications++;
					sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPublicChannels));
				}
			}
		};
		/*function that updates notifications count for a specific private channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateNotificationsPrivate = function(channelName){
			for (i=0;i<$scope.UserPrivateChannels.length;i++){
				if (($scope.UserPrivateChannels[i]).name == channelName){
					($scope.UserPrivateChannels[i]).notifications++;
					sessionStorage.setItem("UserPrivateChannels",JSON.stringify($scope.UserPrivateChannels));}
			}
		};

		/*function that updates mentions count for a specific public channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateMentionsPublic = function(channelName){
			for (i=0;i<$scope.UserPublicChannels.length;i++){
				if (($scope.UserPublicChannels[i]).channel == channelName){
					($scope.UserPublicChannels[i]).mentions++;
					sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPublicChannels));
				}
			}
		};

		/*function that updates mentions count for a specific private channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateMentionsPrivate = function(channelName){
			for (i=0;i<$scope.UserPrivateChannels.length;i++){
				if (($scope.UserPrivateChannels[i]).name == channelName){
					($scope.UserPrivateChannels[i]).mentions++;
					sessionStorage.setItem("UserPrivateChannels",JSON.stringify($scope.UserPrivateChannels));
				}
			}
		};

		/*function that updates notifications count for a specific public channel according to a new message for subscribed member that
		has just signed in to app*/
		$scope.updateNotificationsOnLoadPublic = function(subscription){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : $scope.userNickname,
					previousLog : $scope.user.lastlastlogged,
					channel : subscription.channel
			}
			/*adding new message to database*/
			$http({
				method: 'POST',
				url: '/GetNotificationsServlet',
				data: userDetails
			}).then(
					function(data){
						if (data != 0)
							return data;
					});
		};

		/*function that updates mentions count for a specific public channel according to a new message for subscribed member that
		has just signed in to app*/
		$scope.updateMentionsOnLoadPublic = function(subscription){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : $scope.userNickname,
					previousLog : $scope.user.lastlastlogged,
					channel : subscription.channel
			}
			/*adding new message to database*/
			$http({
				method: 'POST',
				url: '/GetMentionsServlet',
				data: userDetails
			}).then(
					function(data){
						if (data != 0)
							return data;
					});
		};

		/*function that updates notifications count for a specific private channel according to a new message for subscribed member that
		has just signed in to app*/
		$scope.updateNotificationsOnLoadPrivate = function(channel){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : $scope.userNickname,
					previousLog : $scope.user.lastlastlogged,
					channel : channel.name
			}
			/*adding new message to database*/
			$http({
				method: 'POST',
				url: '/GetNotificationsServlet',
				data: userDetails
			}).then(
					function(data){
						if (data != 0)
							return data;
					});
		};

		/*function that updates mentions count for a specific private channel according to a new message for subscribed member that
		has just signed in to app*/
		$scope.updateMentionsOnLoadPrivate = function(channel){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : $scope.userNickname,
					previousLog : $scope.user.lastlastlogged,
					channel : channel.name
			}
			/*adding new message to database*/
			$http({
				method: 'POST',
				url: '/GetMentionsServlet',
				data: userDetails
			}).then(
					function(data){
						if (data != 0)
							return data;
					});
		};

		/*function that sets the view of create public channel pannel*/
		$scope.createPublicChannel = function(){
			sessionStorage.setItem("welcomeScreen",true);
			$scope.welcomeScreen=sessionStorage.getItem("welcomeScreen");
			sessionStorage.setItem("createChannelScreen",true);
			$scope.createChannelScreen = sessionStorage.getItem("createChannelScreen");
		};

		/*function that actually creates the public channel upon clicking on create button on create public channel pannel*/
		$scope.PublicChannelCreate = function(){
			var newChannelDetails = {
					type : "Public",
					name : $scope.channel_name,
					creator : $scope.userName,
					description : $scope.channel_description,
					created : Date.now()
			};
			/*adding new message to database*/
			$http({
				method: 'POST',
				url: '/CreateChannelServlet',
				data: newChannelDetails
			}).then(
					function(data){
						if (data != "fail"){
							var subscription = data;
							$scope.UserPublicChannels.push(subscription);
							sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPublicChannels));
						}
					});
			sessionStorage.setItem("welcomeScreen",false);
			$scope.welcomeScreen=sessionStorage.getItem("welcomeScreen");
			sessionStorage.setItem("createChannelScreen",false);
			$scope.createChannelScreen = sessionStorage.getItem("createChannelScreen");
		};

		$scope.searchChannels = function(){
			var searchInfo = {
					parameter : "name",
					value : $scope.channelSearchText
			}
			if ($("#radioNick").prop("checked")){
				/*search by nickname*/
				searchInfo.parameter = "nick";
			}
			$http({
				method: 'POST',
				url: '/SearchPublicChannelsServlet',
				data: searchInfo
			}).then(
					function(data){
						if (data != null){
							var channels = data;
							//empty search results array first*/
							sessionStorage.setItem("searchPublicChannels",JSON.stringify(array));
							$scope.searchPublicChannels=JSON.parse(sessionStorage.getItem("searchPublicChannels"));
							for (i=0;i<channels.length;i++)
								$scope.searchPublicChannels.push(channels[i]);
							sessionStorage.setItem("searchPublicChannels",JSON.stringify($scope.searchPublicChannels));
							$scope.searchPublicChannels=JSON.parse(sessionStorage.getItem("searchPublicChannels"));
							sessionStorage.setItem("showSearchResults",true);
							$scope.showSearchResults = sessionStorage.getItem("showSearchResults");
						}
					});
		};

		$scope.channelSubscribe = function(channelName){
			sessionStorage.setItem("showSearchResults",false);
			$scope.showSearchResults = sessionStorage.getItem("showSearchResults");
			var subscriptionInfo = {
					channel : channelName,
					user : $scope.userName
			}
			$http({
				method: 'POST',
				url: '/PublicChannelSubscribeServlet',
				data: subscriptionInfo
			}).then(
					function(data){
						if (data != "fail"){
							var channel = data;
							$scope.searchPublicChannels.push(channel);
							$scope.searchPublicChannels[$scope.searchPublicChannels.length-1].setAttribute(mentions,0);
							$scope.searchPublicChannels[$scope.searchPublicChannels.length-1].setAttribute(notifications,0);
							sessionStorage.setItem("searchPublicChannels",JSON.stringify($scope.searchPublicChannels));
							$scope.searchPublicChannels=JSON.parse(sessionStorage.getItem("searchPublicChannels"));

						}
					});
		};

	};
})();
