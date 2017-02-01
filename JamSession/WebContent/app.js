(function () {
'use strict';

angular.module('jamSession', [])
.controller('jamSessionController', jamSessionController);

jamSessionController.$inject = ['$scope','$http'];
function jamSessionController($scope,$http) {
  $scope.welcomeScreen=true;
  $scope.showSideBar = true;
  $scope.tab = "signup";

  $scope.selectTab = function(setTab) {
    $scope.tab = setTab;
  };

  $scope.isSelected = function(checkTab){
    return $scope.tab === checkTab;
  };

/*list of user information*/
  $scope.userName = "";
  $scope.password = "";
  $scope.userNickname = "";
  $scope.userDescription = "";
  $scope.photoURL = "";



  $scope.userSignUp = function(){
    var newUserDetails = {
							userName : $scope.userName,
							password : $scope.password,
              userNickname : $scope.userNickname,
              userDescription : $scope.userDescription,
              userPhotoURL : $scope.photoURL
						};
		var res = $http.post('/SignUpServlet', newUserDetails);
    res.success(function(data){
        $scope.username = data.userName;
        $scope.password = data.password;
        $scope.userNickname = data.userNickname;
        $scope.userDescription = data.userDescription;
        $scope.userPhotoURL = data.userPhotoURL;
    })
  };

  $scope.userLogin = function(){
    var userCredentials = {
							userName : $scope.userName,
							password : $scope.password,
						};
		var res = $http.post('/LoginServlet', userCredentials);
    res.success(function(data){
        $scope.username = data.userName;
        $scope.password = data.password;
        $scope.userNickname = data.userNickname;
        $scope.userDescription = data.userDescription;
        $scope.userPhotoURL = data.userPhotoURL;
    });
  };

};
})();
