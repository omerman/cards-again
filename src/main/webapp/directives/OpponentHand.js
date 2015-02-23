define([], function () {//<opphand></opphand>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/opponentHand.html",
            scope: {
                "getCardsNum": "&cardsNum"
            },
            controller: function ($scope) {

                $scope.data = {
                    cardsRange: new Array($scope.getCardsNum() || 0),
                    unregisterArr: []
                };

                $scope.data.unregisterArr.push($scope.$on('reloadHands', function () {
                    $scope.data.cardsRange = new Array($scope.getCardsNum() || 0);
                }));

            }
        }
    }
});