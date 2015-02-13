define([],function() {//<opphand></opphand>
    return function(){
        return {
            restrict: 'C',
            replace: false,
            controller: function($scope) {
                var destroyDestroyer = $scope.$on("$destroy",function() {
                    destroyDestroyer();
                    if($scope && $scope.data && $scope.data.unregisterArr) {
                        for (var index in $scope.data.unregisterArr) {
                            $scope.data.unregisterArr[index]();
                        }
                    }
                });

            }
        }
    }
});