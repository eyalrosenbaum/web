(function () {
'use strict';

angular.module('jamSession', [])
.controller('jamSessionController', jamSessionController);

jamSessionController.$inject = ['$scope'];
function jamSessionController($scope) {
  $scope.showSideBar = false;
  $scope.tab = "signup";

  $scope.selectTab = function(setTab) {
    $scope.tab = setTab;
  };

  $scope.isSelected = function(checkTab){
    return $scope.tab === checkTab;
  };
};
})();
