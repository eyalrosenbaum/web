(function () {
	'use strict';

	angular.module('jamSession', [])
	.controller('jamSessionController', jamSessionController);

	jamSessionController.$inject = ['$scope','$http','$location'];
	function jamSessionController($scope,$http,$location) {
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
		$scope.ThreadsToShow=[];
		$scope.UserPublicChannels=[];
		$scope.UserPrivateChannels=[];
		$scope.ActiveChannel ="";
		$scope.lastThreadDate = Date.now();
		$scope.firstThreadDate = Date.now();

		/*-1 is the value for a message that is not a reply in the variable replyParentId*/
		$scope.replyParentId =-1;
		$scope.replyIndication = false;
		$scope.showSearchResults = false;
		$scope.numberOfNewMessages = 0;
		$scope.numberOfMentions = 0;
		$scope.replyTo="";
		$scope.channel_description="";
		$scope.channel_name="";
		$scope.searchPublicChannels=[];

		/*tab is the active tab on login signup panel*/
		$scope.tab = "signup";

		/*scope variables to bind and retrieve data from index.html*/
		$scope.userName="";
		$scope.password="";
		$scope.userNickname="";
		$scope.userDescription="";
		$scope.photoURL="";
		$scope.lastLogged =new Date();
		$scope.lastlastlogged = new Date();

		/*method to change from login to sign up and vice versa on welcome screen*/
		$scope.selectTab = function(setTab) {
			$scope.tab = setTab;
		};

		/*method to change view from login to sign up and vice versa on welcome screen*/
		$scope.isSelected = function(checkTab){
			return $scope.tab === checkTab;
		};



		/*setting up websocket on client to send messages*/
		var wsUri = "ws://"+window.location.host+window.location.pathname+"chat";
		console.log("wsUri is "+wsUri);
		var websocket = new WebSocket(wsUri);
		//$scope.websocket = new WebSocket(wsUri);
		/*$scope.*/websocket.onopen = function(evt){
			console.log("connected to server");
		};
		//what happens when we recieve a message
		/*$scope.*/websocket.onmessage = function(evt){
			notify(evt.data);
		};
		/*$scope.*/websocket.onerror = function(evt){
			console.log('ERROR: '+evt.data);
		};
		/*$scope.*/websocket.onclose = function(evt){
			websocket = null;
			console.log("disconnected from server");
		};

		/*posting a message to screen - need to write this*/
		var notify = function(message) {
			console.log("starting notify function");
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
						url: '/GetPrivateChatServlet',
						data: credentials
					}).then(
							function(response){
								console.log("/GetPrivateChatServlet called, response is "+response.data);
								var data = JSON.stringify(response.data);
								var json = JSON.parse(data);
								if (data != null){
									/*entering private channel to channels list and updating  it's mentions and notifications*/
									$scope.UserPrivateChannels.push(json);
									($scope.UserPrivateChannels[$scope.UserPrivateChannels.length-1]).setAttribute(mentions,0);
									($scope.UserPrivateChannels[$scope.UserPrivateChannels.length-1]).setAttribute(notifications,1);
								}
							});
				}}
			else{
				/*for users that are on the channel that contains the new message, the page should refresh according to the thread updated*/
				if ($scope.ActiveChannel == message.channel){
					/*getting 10 newest threads to show*/
					getNewestThreads(ActiveChannel);
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
		};
		/*function that gets user's public channels on login*/
		$scope.getUserPublicChannels = function(nickname){
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/FindSubscriptionServlet',
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("FindSubscriptionServlet called, response is "+response.data);
						for (var x in json){
							$scope.UserPublicChannels.push(x);
							console.log("x is "+x);
						}

						for(var i=0;i<$scope.UserPublicChannels.length;i++){
							($scope.UserPublicChannels[i]).mentions = 0;
							($scope.UserPublicChannels[i]).notifications = 0;
							($scope.UserPublicChannels[i]).notifications = updateNotificationsOnLoadPublic($scope.UserPublicChannels[i]);
							($scope.UserPublicChannels[i]).mentions = updateMentionsOnLoadPublic($scope.UserPublicChannels[i]);
							console.log("$scope.UserPublicChannels["+i+"] is "+$scope.UserPublicChannels[i]);
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
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						if (data!=null){
							for (var x in json){
								$scope.UserPrivateChannels.push(x);
							}
							for(var i=0;i<$scope.UserPublicChannels.length;i++){
								($scope.UserPrivateChannels[i]).setAttribute(mentions,0);
								($scope.UserPrivateChannels[i]).setAttribute(notifications,0);
								($scope.UserPrivateChannels[i]).notifications = updateNotificationsOnLoadPrivate(($scope.UserPrivateChannels[i]));
								($scope.UserPrivateChannels[i]).mentions = updateMentionsOnLoadPrivate(($scope.UserPrivateChannels[i]));
								console.log("$scope.UserPrivateChannels["+i+"] is "+$scope.UserPrivateChannels[i]);
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
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("LoginServlet called, response is "+response.data);
						console.log("LoginServlet called, json is "+json);
						if (json != null){
							$scope.userName=json.userName;
							console.log("userName is "+ $scope.userName);
							$scope.password=json.password;
							console.log("password is "+ $scope.password);
							$scope.userNickname=json.userNickname;
							console.log("userNickname is "+ $scope.userNickname);
							$scope.userDescription=json.userDescription;
							console.log("userDescription is "+ $scope.userDescription);
							$scope.photoURL=json.photoURL;
							console.log("photoURL is "+ $scope.photoURL);
							$scope.islogged = json.islogged;
							$scope.lastlastlogged = new Date(json.lastlogged)
							$scope.lastLogged = Date.now()
							$scope.welcomeScreen=false;
							$scope.getUserPublicChannels($scope.userNickname);
							$scope.getUserPrivateChannels($scope.userNickname);
							$scope.showChannels = true;
							console.log("userPrivateChannels are at login "+$scope.UserPrivateChannels.length);
							console.log("UserPublicChannels are at login "+$scope.UserPublicChannels.length);
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
						console.log("SignupServlet called, response is "+JSON.stringify(response.data));
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log(data);
						console.log(json);
						console.log("username is "+json.userName);
						if((json.userName == "Error username taken")||(json.userName == "Error nickname taken")){
							console.log("there was an error");
								$scope.ErrorExists = true;
							$scope.ErrorMsg = json.userName;
							}else{
								$scope.userName = json.userName;
								$scope.password=json.password;
								$scope.userNickname=json.userNickname;
								$scope.userDescription=json.userDescription;
								$scope.photoURL=json.photoURL;
								$scope.islogged = json.islogged;
								$scope.lastLogged =new Date(json.lastLogged)
								$scope.lastlastlogged = new Date(json.lastLogged)
							$scope.welcomeScreen=false;
								$scope.getUserPublicChannels($scope.userNickname);
								$scope.getUserPrivateChannels($scope.userNickname);
							$scope.showChannels = true;
							console.log("userPrivateChannels are at login "+$scope.UserPrivateChannels);
							console.log("UserPublicChannels are at login "+$scope.UserPublicChannels);
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
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("LogoutServlet called, response is "+response.data);
						console.log("LogoutServlet called, response is "+json);
						console.log(json.trim() == "success");
						if (json.trim() == "success"){

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



		``  /*function that removes a specific public channel from a user's channels*/
		$scope.publicChannelRemove = function(channelName){
			var subscription = {
					username : $scope.userName,
					channel : channelName,
					type : "public"
			}
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/UnsubscribeServlet',
				data: subscription
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("UnsubscribeServlet called, response is "+response.data);
						if (json == "success"){
							/*removing the channel from users channel list on screen*/
							var index = -2;
							for (i=0;i<$scope.UserPublicChannels.length;i++)
								if ($scope.UserPublicChannels[i].channel == subscription.channel)
									index = i;
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
				url: 'http://localhost:8080/Proj/RemovePrivateChannelServlet',
				data: subscription
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("RemovePrivateChannelServlet called, response is "+data.data);
						console.log("data recieved from privateChannelRemove is "+data);
						if (json != "fail"){
							/*removing the channel from users channel list on screen*/
							var index = -2;
							for (i=0;i<$scope.UserPrivateChannels.length;i++)
								if ($scope.UserPrivateChannels[i].channel == data.name)
									index = i;
							if (index > -1){
								$scope.UserPrivateChannels.splice(index, 1);
								sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPrivateChannels));

							}
						}
					});
		};

		/*function that set displayed channel to selected channel*/
		$scope.setChannel = function(channelName){
			sessionStorage.setItem("showSearchResults",JSON.stringify(false));
		$scope.showSearchResults = JSON.parse(sessionStorage.getItem("showSearchResults"));
			$scope.ActiveChannel = JSON.parse(sessionStorage.getItem("ActiveChannel"));
			if (($scope.ActiveChannel != undefined) && ($scope.ActiveChannel !=""))
				$scope.removeUserFromChannelList($scope.ActiveChannel);
			var arr =[];
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/GetThreadsServlet/channelName/'+channelName
			}).then(
					function(data){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("GetThreadsServlet called, response is "+data.data);
						console.log(data);
						if (json != null){
							/*extracting threads from database according to channel*/
							$scope.ThreadsToShow=json;
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
							for (var i=0;i<ThreadsToShow.length;i++){
								(ThreadsToShow[i])["showReplies"]=false;
							}

							/*setting view to show threads and to mark active channel*/
						$scope.showThreads = true;
							$scope.ActiveChannel = channelName;
						}
						// else return arr;
					});
		};

		$scope.removeUserFromChannelList = function(channelName){
			var data={
				name : channelName
			};
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/RemoveUserFromChannelListServlet',
				data: data
			}).then(
				function(response){
					var data = JSON.stringify(response.data);
					var json = JSON.parse(data);
					console.log("data sent from removeUserFromChannelList is "+json);
				});
		};

		$scope.openPrivateChannel = function(nickname){
			var credentials = {
					usera : $scope.userNickname,
					userb : nickname
			}
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/GetPrivateChatServlet',
				data: credentials
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("data received from openPrivateChannel is "+json);
						if (json != null){
							setChannel(json.name);
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
								data: channelDetails
							}).then(
									function(response){
										var data = JSON.stringify(response.data);
										var json = JSON.parse(data);
										if (json != null){
											$scope.UserPrivateChannels.push(json);
											setChannel(json.name);
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
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("GetNewestThreadsServlet called, response is "+data.data);
						if (json != null){
							/*removing the channel from users channel list on screen*/
							$scope.ThreadsToShow=json;
							for (i=0;i<$scope.ThreadsToShow.length;i++)
								(ThreadsToShow[i])["showReplies"]=false;
						$scope.showThreads =true;
							$scope.ActiveChannel = channelName;
							/*updating lastThreadDate for the scrollupdown functions*/

							$scope.lastThreadDate = $scope.ThreadsToShow[length-1].lastUpdate;//newest

							$scope.firstThreadDate = $scope.ThreadsToShow[0].lastUpdate;//oldest
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
				data: channelToGet
			}).then(
					function(data){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("GetNextTenThreadsUpServlet called, response is "+response.data);
						if (json != null){
							/*removing the channel from users channel list on screen*/
							var newThreads = data;
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
				data: channelToGet
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("GetNextTenThreadsDownServlet called, response is "+response.data);
						if (json != null){
							/*removing the channel from users channel list on screen*/
							var newThreads = data;
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
				data: channelToGet
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("GetNextThreadUpServlet called, response is "+response.data);
						console.log("getNextThreadUp result "+data);
						if (json != null){
							var newThread = data;
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
				data: channelToGet
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("getNextThreadUp result "+response);
						if (json != null){
							var newThread = data;
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
			if($scope.showThreads){
			if (y=='down')
				$scope.getNextThreadDown($scope.ActiveChannel);
			else if (y=='up')
				$scope.getNextThreadUp($scope.ActiveChannel);
			$scope.lastY = currY;
		}});


		/*fucntion that checks whether the current checked channel from channels list is active*/
		$scope.isActiveChannel = function(channelName){
			return $scope.ActiveChannel == channelName;
		};

		/*function that gets a thread id and returns an array of replies to it*/
		$scope.getReplies = function(thread_id){
			var arr =[];
			$http({
				method: 'GET',
				url: 'http://localhost:8080/Proj/GetRepliesServlet/threadID/'+thread_id
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("data from getReplies is "+data);
						if (json != null){
							/*returning array of replies*/
							for (var i=0;i<data.length;i++){
								(json[i])["showReplies"]=false;
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
			$scope.replyIndication = false;
				$scope.replyParentId =-1;
				$scope.message_input.replace("@thread_author","");
			}
			else {
				/*you can only reply to a single message*/
				if ($scope.replyParentId == -1){
					$scope.replyParentId =thread_id;
				$scope.replyIndication = true;
					$scope.replyTo=thread_author;
					$scope.message_input="@"+thread_author+$scope.message_input;
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
					data: message
				}).then(
						function(response){
							var data = JSON.stringify(response.data);
							var json = JSON.parse(data);
							console.log("PostThreadServlet called, response is "+response.data);
							if (json == "success"){
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
					url: 'http://localhost:8080/Proj/PostReplyServlet',
					data: message
				}).then(
						function(response){
							var data = JSON.stringify(response.data);
							var json = JSON.parse(data);
							console.log("PostReplyServlet called, response is "+response.data);
							if (json == "success"){
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
			for (var i=0;i<$scope.UserPrivateChannels.length;i++){
				if (($scope.UserPrivateChannels[i]).name == channelName){
					($scope.UserPrivateChannels[i]).notifications++;
			}
		};

		/*function that updates mentions count for a specific public channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateMentionsPublic = function(channelName){
			for (var i=0;i<$scope.UserPublicChannels.length;i++){
				if (($scope.UserPublicChannels[i]).channel == channelName){
					($scope.UserPublicChannels[i]).mentions++;
				}
			}
		};

		/*function that updates mentions count for a specific private channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateMentionsPrivate = function(channelName){
			for (var i=0;i<$scope.UserPrivateChannels.length;i++){
				if (($scope.UserPrivateChannels[i]).name == channelName){
					($scope.UserPrivateChannels[i]).mentions++;
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
				url: 'http://localhost:8080/Proj/GetNotificationsServlet',
				data: userDetails
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("data recieved from GetNotificationsServlet is "+response);
						if (json != 0)
							return json;
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
				url: 'http://localhost:8080/Proj/GetMentionsServlet',
				data: userDetails
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("data recieved from GetMentionsServlet is "+response);
						if (json != 0)
							return json;
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
				url: 'http://localhost:8080/Proj/GetNotificationsServlet',
				data: userDetails
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("data recieved from GetNotificationsServlet is "+response);
						if (json != 0)
							return json;
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
				url: 'http://localhost:8080/Proj/GetMentionsServlet',
				data: userDetails
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("data recieved from GetMentionsServlet is "+data);
						if (json != 0)
							return json;
					});
		};

		/*function that sets the view of create public channel pannel*/
		$scope.createPublicChannel = function(){
						console.log("we are here at createPublicChannel");
					$scope.welcomeScreen=true;
					console.log($scope.welcomeScreen);
				$scope.createChannelScreen = true;
				console.log($scope.createChannelScreen);
		};

		/*function that actually creates the public channel upon clicking on create button on create public channel pannel*/
		$scope.publicChannelCreate = function(){
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
				url: 'http://localhost:8080/Proj/CreateChannelServlet',
				data: newChannelDetails
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("CreateChannelServlet called, response is "+response.data);
						if (json != "fail"){
							var subscription = data;
							$scope.UserPublicChannels.push(subscription);
							sessionStorage.setItem("UserPublicChannels",JSON.stringify($scope.UserPublicChannels));
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
				data: searchInfo
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("SearchPublicChannelsServlet called, response is "+response.data);
						if (json != null){
							var channels = json;
							//empty search results array first*/
							$scope.searchPublicChannels=[];
							for (var i=0;i<channels.length;i++)
								$scope.searchPublicChannels.push(channels[i]);
						$scope.showSearchResults = true;
						}
					});
		};

		$scope.channelSubscribe = function(channelName){
			sessionStorage.setItem("showSearchResults",JSON.stringify(false));
		$scope.showSearchResults = JSON.parse(sessionStorage.getItem("showSearchResults"));
			var subscriptionInfo = {
					channel : channelName,
					user : $scope.userName
			}
			$http({
				method: 'POST',
				url: 'http://localhost:8080/Proj/PublicChannelSubscribeServlet',
				data: subscriptionInfo
			}).then(
					function(response){
						var data = JSON.stringify(response.data);
						var json = JSON.parse(data);
						console.log("PublicChannelSubscribeServlet called, response is "+response.data);
						if (json != "fail"){
							var channel = json;
							$scope.searchPublicChannels.push(channel);
							$scope.searchPublicChannels[$scope.searchPublicChannels.length-1].setAttribute(mentions,0);
							$scope.searchPublicChannels[$scope.searchPublicChannels.length-1].setAttribute(notifications,0);
							sessionStorage.setItem("searchPublicChannels",JSON.stringify($scope.searchPublicChannels));
							$scope.searchPublicChannels=JSON.parse(sessionStorage.getItem("searchPublicChannels"));

						}
					});
		};

		};
	};
})();
