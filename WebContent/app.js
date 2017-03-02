(function () {
	'use strict';

	angular.module('jamSession', [])
	.controller('jamSessionController', jamSessionController);

	jamSessionController.$inject = ['$scope','$http','$location'];
	function jamSessionController($scope,$http,$location) {

		$http.get("http://localhost:8080/Proj/Channels").then(
		function(response){
			//console.log(JSON.parse(response.data));
		});

		$http.get("http://localhost:8080/Proj/Messages").then(
		function(response){
			//console.log(JSON.parse(response.data));
		});
		$http.get("http://localhost:8080/Proj/Subscriptions").then(
		function(response){
		//	console.log(JSON.parse(response.data));
		});
		$http.get("http://localhost:8080/Proj/Users").then(
		function(response){
			//console.log(JSON.parse(response.data));
		});
		/*welcomeScreen is true when we display login or signup panel and false after user is logged into site*/
		// Save data to sessionStorage;
		$scope.welcomeScreen=true;
		$scope.createChannelScreen = false;
		$scope.showThreads = false;
		$scope.loading = false;
		$scope.showSideBar = true;
		$scope.ErrorExists = false;
		$scope.showChannels = false;
		$scope.ErrorMsg = "";
		// $scope.ThreadsToShow=[];
		// $scope.UserPublicChannels=[];
		// $scope.UserPrivateChannels=[];
		$scope.ActiveChannel ="";
		// $scope.lastThreadDate = Date.now();
		// $scope.firstThreadDate = Date.now();

		/*-1 is the value for a message that is not a reply in the variable replyParentId*/
		$scope.replyParentId =-1;
		$scope.replyIndication = false;
		$scope.showSearchResults = false;
		$scope.numberOfNewMessages = 0;
		$scope.numberOfMentions = 0;
		$scope.replyTo="";
		$scope.channel_description="";
		$scope.channel_name="";
		// $scope.searchPublicChannels=[];

		/*tab is the active tab on login signup panel*/
		$scope.tab = "signup";
		$(document).ready(function(){
			getSessionDetails();
		});

		function getSessionDetails() {
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/GetSessionDetailsServlet',
			}).then(
					function(response){
						console.log("initiated getsessiondetails sequence");
						if(!((typeof response.data === 'string')&&(response.data.trim() == "fail"))){
							/*not a new session, session user details are already known*/
							$scope.welcomeScreen = false;
							$scope.userName=response.data.userName;
							$scope.password=response.data.password;
							$scope.userNickname=response.data.userNickname;
							$scope.userDescription=response.data.userDescription;
							$scope.photoURL=response.data.photoURL;
							$scope.lastLogged =new Date(response.data.lastLogged);
							$scope.lastlastlogged = new Date(response.data.lastlastlogged);

					}
					else{
							/*scope variables to bind and retrieve data from index.html*/
							/*new session - details of user will be taken on signup or login*/
							$scope.userName="";
							$scope.password="";
							$scope.userNickname="";
							$scope.userDescription="";
							$scope.photoURL="";
							$scope.lastLogged =new Date();
							$scope.lastlastlogged = new Date();
							$scope.welcomeScreen = true;
					}
				});
		}


		console.log("username is "+$scope.userName);
		console.log("password is "+$scope.password);
		console.log("userNickname is "+$scope.userNickname);
		console.log("userDescription is "+$scope.userDescription);
		console.log("photoURL is "+$scope.photoURL);
		console.log("lastLogged is "+$scope.lastLogged);
		console.log("lastlastlogged is "+$scope.lastlastlogged);

		/*method to change from login to sign up and vice versa on welcome screen*/
		$scope.selectTab = function(setTab) {
			$scope.tab = setTab;
		};

		/*method to change view from login to sign up and vice versa on welcome screen*/
		$scope.isSelected = function(checkTab){
			return $scope.tab === checkTab;
		};


	function connect() {
		/*setting up websocket on client to send messages*/
		var wsUri = "ws://"+window.location.host+window.location.pathname+"chat/"+$scope.userNickname;
		console.log("wsUri is "+wsUri);
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
			$scope.websocket = null;
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
				if (($scope.userNickname == message.participanta)||($scope.userNickname == message.participantb)){
							if ($scope.userNickname == message.participantb){
							credentials.userb = credentials.usera;
							credentials.usera = $scope.userNickname;
					}
					$http({
						method: 'POST',
						url: 'http://localhost:8080/Proj/GetPrivateChatServlet',
						data: credentials
					}).then(
							function(response){
								console.log("/GetPrivateChatServlet called, response is "+response.data);
								if (response.data != null){
									/*entering private channel to channels list and updating  it's mentions and notifications*/
									$scope.UserPrivateChannels.push(response.data);
									($scope.UserPrivateChannels[$scope.UserPrivateChannels.length-1]).setAttribute(mentions,0);
									($scope.UserPrivateChannels[$scope.UserPrivateChannels.length-1]).setAttribute(notifications,1);
								}
							});
				}}
			else{
				console.log("message is "+message);
				console.log("active channe is "+$scope.ActiveChannel);
				/*for users that are on the channel that contains the new message, the page should refresh according to the thread updated*/
				if ($scope.ActiveChannel == message.channel){
					/*getting 10 newest threads to show*/
					$scope.getNewestThreads($scope.ActiveChannel);
				}
				else{
					console.log("message is "+message);
					/*for users that are subscribed to the channel that contains the new message but are not there there should be a notification*/
					var isSubscribed = $scope.checkSubscription(message.channel);
					if (isSubscribed == "public")
						$scope.updateNotificationsPublic(message.channel);
					if (isSubscribed == "private")
						$scope.updateNotificationsPrivate(message.channel);
					/*if user is subscribed to channel and is mentioned in the new message posted he should get a notification*/
					console.log("message content is "+message.content);
					if (message.content.includes("@"+$scope.userNickname)){
						if (isSubscribed == "public")
							$scope.updateMentionsPublic(message.channel);
						if (isSubscribed == "private")
							$scope.updateMentionsPrivate(message.channel);
					}
				}
			}
		};


		/*function that gets user's public channels on login*/
		$scope.getUserPublicChannels = function(nickname){
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/FindSubscriptionServlet',
			}).then(
					function(response){
						if (response.data!=undefined){
						$scope.UserPublicChannels = response.data;

						console.log("FindSubscriptionServlet called, UserPublicChannels is "+$scope.UserPublicChannels);
						/*setting the attributes mentions and notifications to each of the user's channel, and afterwards updating them*/
						for(var i=0;i<$scope.UserPublicChannels.length;i++){
							($scope.UserPublicChannels[i]).mentions = 0;
							($scope.UserPublicChannels[i]).notifications = 0;
							($scope.UserPublicChannels[i]).notifications = $scope.updateNotificationsOnLoadPublic($scope.UserPublicChannels[i]);
							($scope.UserPublicChannels[i]).mentions = $scope.updateMentionsOnLoadPublic($scope.UserPublicChannels[i]);
							console.log("$scope.UserPublicChannels["+i+"] is "+$scope.UserPublicChannels[i]);
						}
					}

					});
		};

		/*function that gets user's private channels on login*/
		$scope.getUserPrivateChannels = function(nickname){
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/FindPrivateChannelsServlet',
			}).then(
					function(response){
						console.log("FindPrivateChannelsServlet called, response is "+response.data);
						if (response.data!=undefined){
								$scope.UserPrivateChannels = response.data;
							for(var i=0;i<$scope.UserPrivateChannels.length;i++){
								console.log("$scope.UserPrivateChannels["+i+"] is "+$scope.UserPrivateChannels[i]);
								/*setting the attributes mentions and notifications to each of the user's channel, and afterwards updating them*/
								$scope.UserPrivateChannels[i].mentions = 0;
								$scope.UserPrivateChannels[i].notifications = 0;
								$scope.UserPrivateChannels[i].notifications = $scope.updateNotificationsOnLoadPrivate(($scope.UserPrivateChannels[i]));
								$scope.UserPrivateChannels[i].mentions = $scope.updateMentionsOnLoadPrivate(($scope.UserPrivateChannels[i]));

							}
						}

					});
		};

		/*method activated when user tries to login to site*/
		$scope.userLogin = function(){
			var userCredentials = {
					userName : $scope.userName,
					password : $scope.password
			};
			$http.post(//{
				/*method: 'POST',
				url: */"http://localhost:8080/Proj/LoginServlet",
				/*data: */JSON.stringify(userCredentials)
			/*}*/).then(
					function(response){
						if ((typeof response.data === 'string')&&(response.data.trim() == "fail")){
							$scope.ErrorExists = true;
							$scope.ErrorMsg = "wrong username or password";
						}
						else{
							console.log("LoginServlet called, response is "+response.data.userName);
							if (response.data.userName != undefined){
							$scope.userName=response.data.userName;
							console.log("userName is "+ $scope.userName);
							$scope.password=response.data.password;
							console.log("password is "+ $scope.password);
							$scope.userNickname=response.data.userNickname;
							console.log("userNickname is "+ $scope.userNickname);
							$scope.userDescription=response.data.userDescription;
							console.log("userDescription is "+ $scope.userDescription);
							$scope.photoURL=response.data.photoURL;
							console.log("photoURL is "+ $scope.photoURL);
							$scope.islogged = response.data.islogged;
							$scope.lastlastlogged = new Date(response.data.lastlogged)
							$scope.lastLogged = Date.now()
							$scope.welcomeScreen=false;
							$scope.getUserPublicChannels($scope.userNickname);
							$scope.getUserPrivateChannels($scope.userNickname);
							$scope.showChannels = true;
						}
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
								$scope.ErrorExists = true;
							$scope.ErrorMsg = response.data.userName;
							}else{
								$scope.userName = response.data.userName;
								$scope.password=response.data.password;
								$scope.userNickname=response.data.userNickname;
								$scope.userDescription=response.data.userDescription;
								$scope.photoURL=response.data.photoURL;
								$scope.islogged = response.data.islogged;
								$scope.lastLogged =new Date(response.data.lastLogged)
								$scope.lastlastlogged = new Date(response.data.lastLogged)
								$scope.welcomeScreen=false;
								$scope.showChannels = true;

							}

					});
		};

		/*function that is called when a user chooses to logout*/
		$scope.logout = function(){
			var UserDetails = {
					userName : $scope.userNickname,
					lastActiveChannel : $scope.ActiveChannel
			};
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/LogoutServlet',
				data: JSON.stringify(UserDetails)
			}).then(
					function(response){
						if ((typeof response.data === 'string') && (response.data.trim() == "success")){

							$scope.userName="";
							console.log("userName is "+$scope.userName);
							$scope.password="";
							$scope.userNickname="";
							$scope.userDescription="";
							$scope.photoURL="";
							$scope.islogged = false;
							$scope.lastLogged =new Date();
							$scope.lastlastlogged = new Date(data.lastlogged);
						$scope.welcomeScreen=true;
					$scope.showChannels = false;
						}
					});
		};



		  /*function that removes a specific public channel from a user's channels*/
		$scope.publicChannelRemove = function(channelName){
			var subscription = {
					username : $scope.userName,
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
							for (var i=0;i<$scope.UserPublicChannels.length;i++)
								if ($scope.UserPublicChannels[i].channel == subscription.channel)
									index = i;
							if (index > -1){
								$scope.UserPublicChannels.splice(index, 1);
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
							for (var i=0;i<$scope.UserPrivateChannels.length;i++)
								if ($scope.UserPrivateChannels[i].channel == response.data.name)
									index = i;
							if (index > -1){
								$scope.UserPrivateChannels.splice(index, 1);

							}
						}
					});
		};

		/*function that set displayed channel to selected channel*/
		$scope.setChannel = function(channelName){
		$scope.showSearchResults = false;
			if (($scope.ActiveChannel != undefined) && ($scope.ActiveChannel !=""))
				$scope.removeUserFromChannelList($scope.ActiveChannel);
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
							$scope.ThreadsToShow=data.data;
							console.log("ThreadsToShow are "+$scope.ThreadsToShow);
							/*resetting mentions and notifications indicators for channel*/
							for(var i=0;i<$scope.UserPublicChannels.length;i++){
								if ($scope.UserPublicChannels[i].channel == channelName){
									$scope.UserPublicChannels[i].mentions = 0;
									$scope.UserPublicChannels[i].notifications = 0;
								}
							}
							/*resetting mentions and notifications indicators for channel*/
							for(var i=0;i<$scope.UserPrivateChannels.length;i++){
								if ($scope.UserPrivateChannels[i].name == channelName){
									$scope.UserPrivateChannels[i].mentions = 0;
									$scope.UserPrivateChannels[i].notifications = 0;
								}
							}
							/*setting replies for all extracted threads to hidden*/
							for (var i=0;i<$scope.ThreadsToShow.length;i++){
								($scope.ThreadsToShow[i])["showReplies"]=false;
								$scope.ThreadsToShow[i].replies=[];
							}

							/*setting view to show threads and to mark active channel*/
							console.log("threads to show length is "+$scope.ThreadsToShow.length);
							console.log("threads to show length is "+$scope.ThreadsToShow[0]);
							if ($scope.ThreadsToShow.length > 0){
								$scope.lastThreadDate = $scope.ThreadsToShow[length-1];
								$scope.firstThreadDate = $scope.ThreadsToShow[0];
							}
						$scope.showThreads = true;
						console.log("showThreads are "+$scope.showThreads);
						$scope.ActiveChannel = channelName;
						console.log("ActiveChannel are "+$scope.ActiveChannel);

						}
						// else return arr;
					});
		};

		// $scope.$watch(function(){
		// 	console.log("Digest Loop Fired!");
		// });

		$scope.removeUserFromChannelList = function(channelName){
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

		$scope.openPrivateChannel = function(nickname){
			var credentials = {
					usera : $scope.userNickname,
					userb : nickname
			};
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetPrivateChatServlet',
				data: JSON.stringify(credentials)
			}).then(
					function(response){
						console.log("data received from openPrivateChannel is "+response.data);
						if (response.data != null){
							setChannel(response.data.name);
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
								url: 'http://localhost:8080/Proj/CreatePrivateChatServlet',
								data: JSON.stringify(channelDetails)
							}).then(
									function(response){
										if (response.data != null){
											$scope.UserPrivateChannels.push(response.data);
											setChannel(response.data.name);
											$scope.websocket.send(channelDetails);
										}});
						}
					});
		};


		/*function that fetches the 10 newest threads from databse*/
		$scope.getNewestThreads = function(channelName){
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
							$scope.ThreadsToShow=response.data;
							for (var i=0;i<$scope.ThreadsToShow.length;i++){
								($scope.ThreadsToShowThreadsToShow[i])["showReplies"]=false;
								$scope.ThreadsToShow[i].replies=[];
							}
							$scope.showThreads =true;
							$scope.ActiveChannel = channelName;
							/*updating lastThreadDate for the scrollupdown functions*/
							if ($scope.ThreadsToShow.length > 0){
								$scope.lastThreadDate = $scope.ThreadsToShow[length-1].lastUpdate;//newest
								console.log("lastThreadDate is "+$scope.lastThreadDate);
								$scope.firstThreadDate = $scope.ThreadsToShow[0].lastUpdate;//oldest
								console.log("firstThreadDate is "+$scope.firstThreadDate);
							}
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
						$scope.showThreads = true;
							$scope.ActiveChannel = channelName;
							/*actually adding the newly acquired threads to content*/
							for (var i=0;i<newThreads.length;i++){
								/*add elemet to beginning of thread array*/
								$scope.ThreadsToShow.unshift(newThreads[i]);
								/*remove elemet from end of thread array*/
								$scope.ThreadsToShow.pop();
							}

							/*updating lastThreadDate for the scrollupdown functions*/
							$scope.lastThreadDate = $scope.ThreadsToShow[length-1].lastUpdate;//newest
							$scope.firstThreadDate = $scope.ThreadsToShow[0].lastUpdate;//oldest
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
						$scope.showThreads = true;
							$scope.ActiveChannel = channelName;
							/*actually adding the newly acquired threads to content*/
							for (var i=0;i<newThreads.length;i++){
								/*add elemet to end of thread array*/
								$scope.ThreadsToShow.push(newThreads[i]);
								/*remove elemet from beginning of thread array*/
								$scope.ThreadsToShow.shift();
							};
							/*updating lastThreadDate for the scrollupdown functions*/
							$scope.lastThreadDate = $scope.ThreadsToShow[length-1].lastUpdate;//newest
							$scope.firstThreadDate = ($scope.ThreadsToShow[0]).lastUpdate;//oldest
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
				url: 'http://localhost:8080/Proj/GetNextThreadUpServlet',
				data: JSON.stringify(channelToGet)
			}).then(
					function(response){
						console.log("GetNextThreadUpServlet called, response is "+response.data);
						if (response.data != null){
							var newThread = response.data;
							newThread["showReplies"]=false;
						// 	sessionStorage.setItem("showThreads",JSON.stringify(true));
						// $scope.showThreads = JSON.parse(sessionStorage.getItem("showThreads"));
							// sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
							// $scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
							/*add elemet to beginning of thread array*/
							$scope.ThreadsToShow.unshift(newThread);
							/*remove elemet from end of thread array*/
							$scope.ThreadsToShow.pop();

							/*updating lastThreadDate for the scrollupdown functions*/
							$scope.lastThreadDate = $scope.ThreadsToShow[length-1].lastUpdate;//newest
							$scope.firstThreadDate = $scope.ThreadsToShow[0].lastUpdate;//oldest
						}
						//else return arr;
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
				url: 'http://localhost:8080/Proj/GetNextThreadDownServlet',
				data: JSON.stringify(channelToGet)
			}).then(
					function(response){
						console.log("getNextThreadUp result "+response.data);
						if (response.data != null){
							var newThread = response.data;
							newThread["showReplies"]=false;
						// 	sessionStorage.setItem("showThreads",JSON.stringify(true));
						// $scope.showThreads = JSON.parse(sessionStorage.getItem("showThreads"));
						// 	sessionStorage.setItem("ActiveChannel",JSON.stringify(channelName));
						// 	$scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
							/*add elemet to end of thread array*/
							$scope.ThreadsToShow.push(newThread);
							/*remove elemet from beginning of thread array*/
							$scope.ThreadsToShow.shift();
							/*updating lastThreadDate for the scrollupdown functions*/
							$scope.lastThreadDate = $scope.ThreadsToShow[length-1].lastUpdate;//newest
							$scope.firstThreadDate = $scope.ThreadsToShow[0].lastUpdate;//oldest
						}
						//else return arr;
					});
		};

		/*scrolling part - when scrolling bring more threads*/
		var win = $(window);
		$scope.lastY = win.scrollTop();
		win.on('scroll',function(){
			var currY = win.scrollTop();
			var y = ((currY > $scope.lastY)? 'down' : ((currY===$scope.lastY)? 'none':'up'));
			console.log("showThreads is checked for scrolling and it is "+$scope.showThreads);
			if(($scope.showThreads)&&($scope.ThreadsToShow.length > 9)){
				if (y=='down')
					$scope.getNextThreadDown($scope.ActiveChannel);
				else if (y=='up')
					$scope.getNextThreadUp($scope.ActiveChannel);
				$scope.lastY = currY;
			}
		});

		/*fucntion that checks whether the current checked channel from channels list is active*/
		$scope.isActiveChannel = function(channelName){
			return $scope.ActiveChannel == channelName;
		};

		$scope.fetchReplies = function(thread){
			console.log("fetch replies called");
			$scope.thread.showReplies = !$scope.thread.showReplies;
			if ($scope.thread.showReplies)
				getReplies(thread);
		};
		/*function that gets a thread id and returns an array of replies to it*/
		$scope.getReplies = function(thread){
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/GetRepliesServlet/threadID/'+thread_id
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
		$scope.addReply = function(thread_id,thread_author){
			/*double click will cancel replying*/
			if ($scope.replyParentId == thread_id){
					$scope.replyIndication = false;
					console.log("replyIndication is "+$scope.replyIndication);
					$scope.replyParentId =-1;
					console.log("replyParentId is "+$scope.replyParentId);
					$scope.replyTo = "";
					console.log("replyTo is "+$scope.replyTo);
					$scope.message_input=$scope.message_input.replace("@"+thread_author+":","");
					console.log("message input is "+$scope.message_input);
			}
			else {
				/*you can only reply to a single message*/
				if ($scope.replyParentId == -1){
					$scope.replyParentId =thread_id;
					console.log("replyParentId is "+$scope.replyParentId);
					$scope.replyIndication = true;
					console.log("replyIndication is "+$scope.replyIndication);
					$scope.replyTo= "@"+thread_author+":";
					console.log("replyTo is "+$scope.replyTo);
					if ($scope.message_input != undefined)
						$scope.message_input=$scope.replyTo+$scope.message_input;
					else $scope.message_input=$scope.replyTo;
					console.log("message input is "+$scope.message_input);
				}
				else{
					$scope.replyParentId =thread_id;
					console.log("replyParentId is "+$scope.replyParentId);
					$scope.replyIndication = true;
					console.log("replyIndication is "+$scope.replyIndication);
					$scope.replyTo= "@"+thread_author+":";
					console.log("replyTo is "+$scope.replyTo);
					if ($scope.message_input != undefined)
						$scope.message_input=$scope.replyTo+$scope.message_input;
					else $scope.message_input=$scope.replyTo;
					console.log("message input is "+$scope.message_input);
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
					url: 'http://localhost:8080/Proj/PostThreadServlet',
					data: JSON.stringify(message)
				}).then(
						function(response){
						if (!((typeof response.data === 'string') && (response.data.trim() == "fail"))){
							console.log("PostThreadServlet called, response is "+response.data);
								//sending the message in websocket
								$scope.websocket.send(JSON.stringify(message));
								//reseting the chat typing field
								$scope.message_input="";
								if ($scope.ThreadsToShow.length>9)
									$scope.ThreadsToShow.unshift();
								$scope.ThreadsToShow.push(response.data);
							}
						});
			}else{
		/*now handling the case where the posted message is a reply*/
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
					url: 'http://localhost:8080/Proj/PostReplyServlet',
					data: JSON.stringify(message)
				}).then(
						function(response){
							console.log("PostReplyServlet called, response is "+response.data);
							if (!((typeof response.data === 'string') && (response.data.trim() == "fail"))){
								//sending the message in websocket
								$scope.websocket.send(JSON.stringify(message));
								//reseting the chat typing field
								$scope.message_input="";
								for (var i=0;i<$scope.ThreadsToShow.length;i++)
									if ($scope.ThreadsToShow[i].id==$scope.replyParentId)
										$scope.ThreadsToShow[i].numberOfReplies++;
							}
						});
			}
		};

		/*function that recieves username and channelname and returns true if user is subscribed to channel or false otherwise*/
		$scope.checkSubscription = function(channelName){
			for (var i=0;i<$scope.UserPublicChannels.length;i++){
				if (($scope.UserPublicChannels[i]).channel == channelName)
					return "public";
			}
			for (var i=0;i<$scope.UserPrivateChannels.lenght;i++){
				if (($scope.UserPrivateChannels[i]).name == channelName)
					return "private";
			}
			return "none";
		};
		/*function that updates notifications count for a specific public channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateNotificationsPublic = function(channelName){
			for (var i=0;i<$scope.UserPublicChannels.length;i++){
				if (($scope.UserPublicChannels[i]).channel == channelName){
					($scope.UserPublicChannels[i]).notifications++;
				}
			}
		};
		/*function that updates notifications count for a specific private channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateNotificationsPrivate = function(channelName){
			for (var i=0;i<$scope.UserPrivateChannels.length;i++)
				if (($scope.UserPrivateChannels[i]).name == channelName)
					($scope.UserPrivateChannels[i]).notifications++;

		};

		/*function that updates mentions count for a specific public channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateMentionsPublic = function(channelName){
			for (var i=0;i<$scope.UserPublicChannels.length;i++)
				if (($scope.UserPublicChannels[i]).channel == channelName)
					($scope.UserPublicChannels[i]).mentions++;
		};

		/*function that updates mentions count for a specific private channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateMentionsPrivate = function(channelName){
			for (var i=0;i<$scope.UserPrivateChannels.length;i++)
				if (($scope.UserPrivateChannels[i]).name == channelName)
					($scope.UserPrivateChannels[i]).mentions++;
		};

		/*function that updates notifications count for a specific public channel according to a new message for subscribed member that
		has just signed in to app*/
		$scope.updateNotificationsOnLoadPublic = function(subscription){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : $scope.userNickname,
					previousLog : $scope.lastlastlogged,
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
		$scope.updateMentionsOnLoadPublic = function(subscription){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : $scope.userNickname,
					previousLog : $scope.lastlastlogged,
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
		$scope.updateNotificationsOnLoadPrivate = function(channel){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : $scope.userNickname,
					previousLog : $scope.lastlastlogged,
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
		$scope.updateMentionsOnLoadPrivate = function(channel){
			/*sub will pass the channel details of which to check whether or not there were mentions of the user*/
			var userDetails = {
					nickname : $scope.userNickname,
					previousLog : $scope.lastlastlogged,
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
		$scope.createPublicChannel = function(){
				console.log("we are here at createPublicChannel");
				$scope.welcomeScreen=false;
				console.log($scope.welcomeScreen);
				$scope.createChannelScreen = true;
				console.log($scope.createChannelScreen);
		};

		/*function that actually creates the public channel upon clicking on create button on create public channel pannel*/
		$scope.publicChannelCreate = function(){
			var newChannelDetails = {
					channelName : $scope.channel_name,
					channelType : null,
					channelCreator : $scope.userName,
					//channelCreationTime : Date.now(),
					channelDescription :$scope.channel_description
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
							$scope.UserPublicChannels.push(subscription);
						}
					});
				$scope.welcomeScreen=false;
			$scope.createChannelScreen = false;
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
				url: 'http://localhost:8080/Proj/SearchPublicChannelsServlet',
				data: JSON.stringify(searchInfo)
			}).then(
					function(response){
						console.log("SearchPublicChannelsServlet called, response is "+response.data);
						if (response.data != undefined){
							var channels = response.data;
							//empty search results array first*/
							$scope.searchPublicChannels=[];
							for (var i=0;i<channels.length;i++)
								$scope.searchPublicChannels.push(channels[i]);
						$scope.showSearchResults = true;
						console.log("channels are "+$scope.searchPublicChannels);
						console.log("showSearchResults is "+$scope.showSearchResults);
						}
					});
		};

		$scope.channelSubscribe = function(channelName){
		$scope.showSearchResults = false;
			var subscriptionInfo = {
					channel : channelName,
					user : $scope.userNickname
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
							$scope.searchPublicChannels.push(channel);
							$scope.searchPublicChannels[$scope.searchPublicChannels.length-1].mentions = 0;
							$scope.searchPublicChannels[$scope.searchPublicChannels.length-1].notifications = 0;

						}
					});
		};

		};

})();
