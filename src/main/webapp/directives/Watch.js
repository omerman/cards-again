define([],function() {//<game></game>
    return function(){
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/watch.html",
            controller: function($scope,$element,$attrs) {
                $scope.data = {
                    gId: $attrs.gId
                };

                $scope.onJoinSuccess = function(data) {
                    $scope.changeLocation("/play/"+$scope.data.gId);
                };

                $scope.onJoinError = function(data) {
                    console.log("TODO: Play.js-> $scope.onJoinError");
                };

                $scope.join = function() {
                    $scope.toServer({
                        address: "game.join.request",
                        data: {
                            gId: $scope.data.gId
                        },
                        action: $scope.ACTIONS.SEND,
                        callBackAfterLogin:$scope.join,
                        callBackSuccess: $scope.onJoinSuccess,
                        callBackError: $scope.onJoinError
                    });
                }
            }
        }
    }
});