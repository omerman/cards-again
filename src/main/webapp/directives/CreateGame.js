define([],function() {//<creategame></creategame>
    return function(){
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/createGame.html",
            controller: function($scope) {

                $scope.data = {
                    name:""
                };
                $scope.max = {
                    name:15
                };
                $scope.message = {
                    name:""
                };

                $scope.create = function() {
                  if($scope.validate()) {

                    $scope.toServer({
                            address: "game.create.request",
                            action: $scope.ACTIONS.SEND,
                            data: $scope.data,
                        callBackSuccess: function(callBackData){
                            if (callBackData && callBackData.gId) {
                                $scope.changeLocation("/play/" + callBackData.gId);
                            }
                        },
                        callBackAfterLogin: function(isLoggedIn) {
                            if(isLoggedIn) {
                                $scope.create();
                            }
                            else {
                                console.log("Could not login..");
                            }
                        }
                    });
                  }
                };

                $scope.validate = function() {
                    return !($scope.message.name = $scope.data.name ?"" :true);
                }

            }
        }
    }
});