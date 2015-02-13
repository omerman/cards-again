define([],function() {//<myhand></myhand>
    return function(){
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/myHand.html",
            scope: {
              getMyHand:"&myHand"
            },
            controller: function($scope) {
                $scope.data = {
                    myHand : $scope.getMyHand(),
                    unregisterArr:[]
                };

                $scope.data.unregisterArr.push($scope.$on('reloadMyHand', function() {
                    console.log("hands reloaded.",$scope.getMyHand());
                    $scope.data.myHand = $scope.getMyHand();
                }));
            }
        }
    }
});