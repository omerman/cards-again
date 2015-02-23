define([], function () {//<gameslist></gameslist>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/gamesList.html",
            controller: function ($scope) {

                $scope.data = {
                    pId: null,
                    gamesList: [],
                    unregisterArr: []
                };

                $scope.gamesUpdate = function (data) {
                    console.log("applying");
                    $scope.$apply(function () {
                        $scope.data.gamesList = data.gamesList;
                    });
                };

                $scope.data.unregisterArr.push($scope.$on("loggedIn", function (event, data) {
                    $scope.toServer({
                        address: "my.info.request",
                        action: $scope.ACTIONS.SEND,
                        callBackSuccess: function (data) {
                            $scope.data.pId = data.pId;
                        }
                    });
                }));

                //start listening to games list
                $scope.data.unregisterArr.push($scope.toServer({
                    address: "games.list.update",
                    action: $scope.ACTIONS.LISTEN,
                    callBackSuccess: $scope.gamesUpdate
                }));

                //send first request for games.
                $scope.toServer({
                    address: "games.list.request",
                    action: $scope.ACTIONS.SEND,
                    callBackSuccess: $scope.gamesUpdate
                });

                $scope.hrefByGameIndex = function (index) {
                    var game = $scope.data.gamesList[index];
                    return (game.players.indexOf($scope.data.pId) > -1 ? "#play/" : "#watch/") + game.gId;
                };
            }
        }
    }
});