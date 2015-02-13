define([],function() {//<play></play>
    return function(){
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/play.html",
            controller: function($scope,$element,$attrs) {
                $scope.data = {
                    gId:$attrs.gId
                };
                /*$scope.$on("getGId",function(event,data) {
                   data.callBack($attrs.gId);
                });*/
            }
        }
    }
});