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
		/*-1 is the value for a message that is not a reply in the variable replyParentId*/
		$scope.replyParentId =-1;
		$scope.replyIndication = false;
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
		$scope.lastDate = new Date();
		$scope.firstDate = new Date();

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
			$scope.loading = true;
			refreshMessages();
			$scope.loading = false;
			// var threadList = $("#listOfThreads");
			// var pre = document.createElement("li");
			// pre.style.wordWrap = "break-word";
			// pre.innerHTML = message;
			// chatConsole.appendChild(pre);
		};

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
							$scope.user.lastlogged = new Date(data.lastlogged);
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
              /*removing the channel from users channel list on screen*/
              $scope.ThreadsToShow=data;
              for (i=0;i<ThreadsToShow.length;i++){
                (ThreadsToShow[i])["showReplies"]=false;
              }
              $scope.showThreads = true;
              $scope.ActiveChannel = channelName;
              }
							else return arr;
            });
    };

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



		/*get a single thread message by date scroll up*/
		var getMessageUp = function(){
			$http({
				method: 'GET',
				url: '/GetSingleMessageUp',
				data: $scope.lastDate
			}).then(
					function(data){
						if (data != null){
							$scope.lastMessage = data;
							$("#listOfThreads").append($scope.lastMessage);
						}
					});
		};

		/*get a single thread message by date scroll down*/
		var getMessageDown = function(){
			$http({
				method: 'GET',
				url: '/GetSingleMessageDown',
				data: $scope.firstDate
			}).then(
					function(data){
						if (data != null){
							$scope.lastMessage = data;
							$("#listOfThreads").append($scope.lastMessage);
						}
					});
		};

		/*get ten messages*/
		var refreshMessages = function(){
			$http({
				method: 'GET',
				url: '/GetMessages',
				data: $scope.lastDate
			}).then(
					function(data){
						if (data != null){
							$scope.tenMessages = data;

						}
					});
		};
		/*post new thread*/
		var insertNewThread = function(){
			$http({
				method: 'Post',
				url: '/PostNewThread',
				data: $scope.lastDate
			}).then(
					function(data){
						if (data != null){
							$scope.tenMessages = data;
						}
					});
		};
		/*function that recieves a message and sends it to server for update in DB*/
		$scope.insertMessageToDB = function (message){
			var res = $http.post('/InsertMessage',messsage);
		};

		$scope.updateMessages = function(){
			// insert here code that takes 10 most recently updated threads and posts them to page
			var res = $http.post('/GetTenRecentThreads');
			res.success(function(data){
				// <!--first clearing the array-->
				$scope.messages = [];
				//entering updated threads from DB
				$scope.messages.push(data);
			});
		};

		/*
                var connectFunc = function connect() {

                var wsUri = "ws://"+window.location.host+"/jamSession/chatMessage/"+$scope.username;
                websocket = new WebSocket(wsUri);
                websocket.onopen = function(evt) {
                notify("Connected to Chat Server...");
              };
              websocket.onmessage = function(evt) {
              notify(evt.data);
            };
            websocket.onerror = function(evt) {
            notify('ERROR: ' + evt.data);
          };

          websocket.onclose = function(evt) {
          websocket = null;
        };

        // connectBtn.hidden = true;
        // sendBtn.hidden = false;
        // logoutBtn.hidden = false;
        // userInput.value = '';
      };
		 */

		var sendMessageFunc = function sendMessage() {
			if (websocket != null){
				websocket.send($scope.username);
			}
		}

		var notifyFunc = function notify(message) {
			$scope.messages.push(message);
			$scope.insertMessageToDB(message);
			$scope.updateMessages();
		}

		function logout(){
			websocket.close();
			//   connectBtn.hidden = false;
			//   sendBtn.hidden = true;
			//   logoutBtn.hidden = true;
			//   userInput.value = '';
			// notify("Logged out...");
		}



	};
})();
