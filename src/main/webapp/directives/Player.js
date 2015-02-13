define([],function() {//<player></player>
    return function(){
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/player.html",
            scope: {
                "getPlayerName":"&playerName",
                "isDefender":"&",
                "isFirstAttacker":"&"
            },
            controller: function($scope) {
                $scope.data = {
                    playerName : $scope.getPlayerName() || "",
                    defender: $scope.isDefender(),
                    isFirstAttacker: $scope.isFirstAttacker(),
                    turn:false,
                    unregisterArr:[]
                };

                $scope.data.unregisterArr.push($scope.$on('reloadPlayers', function() {

                    $scope.data.playerName = $scope.getPlayerName() || "";
                    $scope.data.defender = $scope.isDefender();
                    $scope.data.isFirstAttacker = $scope.isFirstAttacker();
                    console.log("render player: "+$scope.data.playerName);
                }));
            }
        }
    }
});