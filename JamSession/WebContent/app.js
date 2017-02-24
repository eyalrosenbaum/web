(function () {
	'use strict';

	angular.module('jamSession', [])
	.controller('jamSessionController', jamSessionController);

	jamSessionController.$inject = ['$scope','$http'];
	function jamSessionController($scope,$http) {
		/*welcomeScreen is true when we display login or signup panel and false after user is logged into site*/
		$scope.welcomeScreen=true;
		$scope.showThreads = true;
		$scope.loading = false;
		$scope.showSideBar = true;
		$scope.ErrorExists = false;
		$scope.showChannels = false;
		$scope.ErrorMsg = "";
		$scope.ThreadsToShow=[];
		$scope.UserPublicChannels = [];
		$scope.UserPrivateChannels = [];
		$scope.ActiveChannel = "";
		$scope.lastThreadDate = Date.now();
		$scope.firstThreadDate = Date.now();
		/*-1 is the value for a message that is not a reply in the variable replyParentId*/
		$scope.replyParentId =-1;
		$scope.replyIndication = false;
		$scope.numberOfNewMessages = 0;
		$scope.numberOfMentions = 0;
		$scope.replyTo="";
		/*tab is the active tab on login signup panel*/
		$scope.tab = "signup";
		$scope.user = {
				/*list of user information*/
				userName : "",
				password : "",
				userNickname : "",
				userDescription : "",
				photoURL : "",
				isLogged : true,
				lastLogged : new Date()
		};

		/*scope variables to bind and retrieve data from index.html*/
		$scope.userName="";
		$scope.password="";
		$scope.userNickname="";
		$scope.userDescription="";
		$scope.photoURL="";
		$scope.lastLogged = new Date();
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
					method: 'GET',
					url: '/GetPrivateChatServlet',
					data: credentials
				}).then(
						function(data){
							if (data != null){
								/*entering private channel to channels list and updating  it's mentions and notifications*/
								$scope.UserPrivateChannels.push(data);
								($scope.UserPrivateChannels[$scope.UserPrivateChannels.length-1]).setAttribute(mentions,0);
								($scope.UserPrivateChannels[$scope.UserPrivateChannels.length-1]).setAttribute(notifications,1);
							}
						});
			}}
			else{
				/*for users that are on the channel that contains the new message, the page should refresh according to the thread updated*/
				if ($scope.ActiveChannel == message.channel){
					$scope.loading = true;
					/*getting 10 newest threads to show*/
					getNewestThreads(ActiveChannel);
					$scope.loading = false;
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
							$scope.user.userName = data.userName;
							$scope.user.password = data.password;
							$scope.user.userNickname = data.userNickname;
							$scope.user.userDescription = data.userDescription;
							$scope.user.photoURL = data.photoURL;
							$scope.user.islogged = data.islogged;
							$scope.user.lastlastlogged = new Date(data.lastlogged);
							$scope.user.lastlogged = Date.now();
							$scope.welcomeScreen=false;
							getUserPublicChannels($scope.user.userNickname);
							getUserPrivateChannels($scope.user.userNickname);
							$scope.showChannels = true;
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
								$scope.ErrorExists = true;
								$scope.ErrorMsg == data.userName;
							}
							else{
								$scope.user.userName = data.userName;
								$scope.user.password = data.password;
								$scope.user.userNickname = data.userNickname;
								$scope.user.userDescription = data.userDescription;
								$scope.user.photoURL = data.photoURL;
								$scope.user.islogged = data.islogged;
								$scope.user.lastlogged = new Date(data.lastlogged);
								$scope.user.lastlastlogged = new Date(data.lastlogged);
								$scope.welcomeScreen=false;
								getUserPublicChannels($scope.user.userNickname);
								getUserPrivateChannels($scope.user.userNickname);
								$scope.showChannels = true;
							}
						}
					});
		};

		/*function that gets user's public channels on login*/
		$scope.getUserPublicChannels = function(nickname){
			var credentials = {
					userNickname : nickname
			};
			$http({
				method: 'GET',
				url: '/FindSubscriptionServlet',
				data: credentials
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

						}
					});
		};

		/*function that gets user's private channels on login*/
		$scope.getUserPrivateChannels = function(nickname){
			var credentials = {
					userNickname : nickname
			};
			$http({
				method: 'GET',
				url: '/FindPrivateChannelsServlet',
				data: credentials
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
						if (data != null){
							/*removing the channel from users channel list on screen*/
							var index = $scope.UserPrivateChannels.indexOf(data);
							if (index > -1){
								$scope.UserPublicChannels.splice(index, 1);
							}
						}
					});
		};

		/*function that set displayed channel to selected channel*/
		$scope.setChannel = function(channelName){
			var channelToGet = {
					name : channelName
			};
			var arr =[];
			$http({
				method: 'GET',
				url: '/GetThreadsServlet',
				data: channelToGet
			}).then(
					function(data){
						if (data != null){
							/*extracting threads from database according to channel*/
							$scope.ThreadsToShow=data;
							/*resetting mentions and notifications indicators for channel*/
							for(i=0;i<$scope.UserPublicChannels.length;i++){
								if ($scope.UserPublicChannels[i].channel == channelToGet.name){
									$scope.UserPublicChannels[i].mentions = 0;
									$scope.UserPublicChannels[i].notifications = 0;
								}
							}
							for(i=0;i<$scope.UserPrivateChannels.length;i++){
								if ($scope.UserPrivateChannels[i].name == channelToGet.name){
									$scope.UserPrivateChannels[i].mentions = 0;
									$scope.UserPrivateChannels[i].notifications = 0;
								}
							}
							/*setting replies for all extracted threads to hidden*/
							for (i=0;i<ThreadsToShow.length;i++){
								(ThreadsToShow[i])["showReplies"]=false;
							}
							/*setting view to show threads and to mark active channel*/
							$scope.showThreads = true;
							$scope.ActiveChannel = channelName;
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
				method: 'GET',
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
										}});
						}
					});
		};


		/*function that fetches the 10 newest threads from databse*/
		$scope.getNewestThreads = function(channelName){
			var channelToGet = {
					name : channelName,
					//date : $scope.lastThreadDate
			};
			var arr =[];
			$http({
				method: 'GET',
				url: '/GetNewestThreadsServlet',
				data: channelToGet
			}).then(
					function(data){
						if (data != null){
							/*removing the channel from users channel list on screen*/
							$scope.ThreadsToShow=data;
							for (i=0;i<$scope.ThreadsToShow.length;i++){
								(ThreadsToShow[i])["showReplies"]=false;
							}
							$scope.showThreads = true;
							$scope.ActiveChannel = channelName;
							/*updating lastThreadDate for the scrollupdown functions*/
							$scope.lastThreadDate = ($scope.ThreadsToShow[length-1]).lastUpdate;//newest
							$scope.firstThreadDate = ($scope.ThreadsToShow[0]).lastUpdate;//oldest
						}
						else return arr;
					});
		};

		/*function that fetches next 10 threads from databse*/
		$scope.getNextTenThreadsUp = function(channelName){
			var channelToGet = {
					name : channelName,
					date : $scope.firstThreadDate
			};
			var arr =[];
			$http({
				method: 'GET',
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
							$scope.showThreads = true;
							$scope.ActiveChannel = channelName;
							/*actually adding the newly acquired threads to content*/
							for (i=0;i<newThreads.length;i++){
								/*add elemet to beginning of thread array*/
								$scope.ThreadsToShow.unshift(newThreads[i]);
								/*remove elemet from end of thread array*/
								$scope.ThreadsToShow.pop();
							}

							/*updating lastThreadDate for the scrollupdown functions*/
							$scope.lastThreadDate = ($scope.ThreadsToShow[length-1]).lastUpdate;//newest
							$scope.firstThreadDate = ($scope.ThreadsToShow[0]).lastUpdate;//oldest
						}
						else return arr;
					});
		};


		/*function that fetches next 10 threads from databse*/
		$scope.getNextTenThreadsDown = function(channelName){
			var channelToGet = {
					name : channelName,
					date : $scope.lastThreadDate
			};
			var arr =[];
			$http({
				method: 'GET',
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
							$scope.showThreads = true;
							$scope.ActiveChannel = channelName;
							/*actually adding the newly acquired threads to content*/
							for (i=0;i<newThreads.length;i++){
								/*add elemet to end of thread array*/
								$scope.ThreadsToShow.push(newThreads[i]);
								/*remove elemet from beginning of thread array*/
								$scope.ThreadsToShow.shift();
							};
							/*updating lastThreadDate for the scrollupdown functions*/
							$scope.lastThreadDate = ($scope.ThreadsToShow[length-1]).lastUpdate;//newest
							$scope.firstThreadDate = ($scope.ThreadsToShow[0]).lastUpdate;//oldest
						}
						else return arr;
					});
		};

		/*function that fetches next thread from databse on scrollup*/
		$scope.getNextThreadUp = function(channelName){
			var channelToGet = {
					name : channelName,
					date : $scope.firstThreadDate
			};
			var arr =[];
			$http({
				method: 'GET',
				url: '/GetNextThreadUpServlet',
				data: channelToGet
			}).then(
					function(data){
						if (data != null){
							var newThread = data;
							newThread["showReplies"]=false;
							$scope.showThreads = true;
							$scope.ActiveChannel = channelName;
							/*add elemet to beginning of thread array*/
							$scope.ThreadsToShow.unshift(newThread);
							/*remove elemet from end of thread array*/
							$scope.ThreadsToShow.pop();


							/*updating lastThreadDate for the scrollupdown functions*/
							$scope.lastThreadDate = ($scope.ThreadsToShow[length-1]).lastUpdate;//newest
							$scope.firstThreadDate = ($scope.ThreadsToShow[0]).lastUpdate;//oldest
						}
						else return arr;
					});
		};


		/*function that fetches next thread from databse on scroll down*/
		$scope.getNextThreadDown = function(channelName){
			var channelToGet = {
					name : channelName,
					date : $scope.lastThreadDate
			};
			var arr =[];
			$http({
				method: 'GET',
				url: '/GetNextThreadDownServlet',
				data: channelToGet
			}).then(
					function(data){
						if (data != null){
							var newThread = data;
							newThread["showReplies"]=false;
							$scope.showThreads = true;
							$scope.ActiveChannel = channelName;
							/*add elemet to end of thread array*/
							$scope.ThreadsToShow.push(newThread);
							/*remove elemet from beginning of thread array*/
							$scope.ThreadsToShow.shift();

							/*updating lastThreadDate for the scrollupdown functions*/
							$scope.lastThreadDate = ($scope.ThreadsToShow[length-1]).lastUpdate;//newest
							$scope.firstThreadDate = ($scope.ThreadsToShow[0]).lastUpdate;//oldest
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
			var MessageToGetRepliesTo = {
					id : thread_id
			};
			var arr =[];
			$http({
				method: 'GET',
				url: '/GetRepliesServlet',
				data: MessageToGetRepliesTo
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
				$scope.replyIndication = false;
				$scope.replyParentId = -1;
				$scope.message_input.replace("@thread_author","");
			}
			else {
				/*you can only reply to a single message*/
				if ($scope.replyParentId == -1){
					$scope.replyParentId = thread_id;
					$scope.replyIndication = true;
					$scope.replyTo = thread_author;
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
				if (($scope.UserPublicChannels[i]).channel == channelName)
					($scope.UserPublicChannels[i]).notifications++;
			}
		};
		/*function that updates notifications count for a specific private channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateNotificationsPrivate = function(channelName){
			for (i=0;i<$scope.UserPrivateChannels.length;i++){
				if (($scope.UserPrivateChannels[i]).name == channelName)
					($scope.UserPrivateChannels[i]).notifications++;
			}
		};

		/*function that updates mentions count for a specific public channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateMentionsPublic = function(channelName){
			for (i=0;i<$scope.UserPublicChannels.length;i++){
				if (($scope.UserPublicChannels[i]).channel == channelName)
					($scope.UserPublicChannels[i]).mentions++;
			}
		};

		/*function that updates mentions count for a specific private channel according to a new message for subscribed member that
		is not currently in the channel*/
		$scope.updateMentionsPrivate = function(channelName){
			for (i=0;i<$scope.UserPrivateChannels.length;i++){
				if (($scope.UserPrivateChannels[i]).name == channelName)
					($scope.UserPrivateChannels[i]).mentions++;
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
				method: 'GET',
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
				method: 'GET',
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
				method: 'GET',
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
				method: 'GET',
				url: '/GetMentionsServlet',
				data: userDetails
			}).then(
					function(data){
						if (data != 0)
							return data;
					});
		};





	};
})();
