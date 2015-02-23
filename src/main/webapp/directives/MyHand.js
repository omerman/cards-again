define([], function () {//<myhand></myhand>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/myHand.html",
            scope: {
                getMyHand: "&myHand"
            },
            controller: function ($scope) {
                $scope.data = {
                    myHand: $scope.getMyHand(),
                    unregisterArr: []
                };

                $scope.data.unregisterArr.push($scope.$on('reloadMyHand', function () {
                    $scope.data.myHand = $scope.getMyHand();

                    /*var cards = [];
                     for(var i = 1,j;i<5;i++) {
                     for(var j = 2;j<15;j++) {
                     cards.push({suit:i,rank:j});
                     }
                     }
                     $scope.data.myHand = cards;*/
                }));
            }
        }
    }
});