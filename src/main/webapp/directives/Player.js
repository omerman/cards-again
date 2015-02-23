define([], function () {//<player></player>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/player.html",
            scope: {
                getPalyerData: "&playerData"
            },
            controller: function ($scope) {

                $scope.initialize = function () {
                    $scope.data = {
                        unregisterArr: []
                    };
                    $scope.updatePlayerData();
                };
                $scope.updatePlayerData = function () {
                    var playerData = $scope.getPalyerData();
                    $scope.data.playerName = playerData.name || "";
                    $scope.data.defender = playerData.isDefender;
                    $scope.data.isFirstAttacker = playerData.isFirstAttacker;
                    $scope.data.isLoser = playerData.isLoser;
                };

                $scope.initialize();

                $scope.data.unregisterArr.push($scope.$on('reloadPlayers', function () {

                    $scope.updatePlayerData();
                    console.log("render player: " + $scope.data.playerName);
                }));


            }
        }
    }
});