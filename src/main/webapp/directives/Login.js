define([],function() {//<creategame></creategame>
    return function(){
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/login.html",
            controller: function($scope) {

                $scope.data = {
                    username:"",
                    password:""
                };
                $scope.message = {
                    username:"",
                    password:""
                };

                $scope.login = function() {
                    if($scope.validate()) {

                        $scope.$parent.login({data:$scope.data,callBack:function(isLoggedIn) {

                            if(isLoggedIn) {
                                $scope.changeLocation("/");
                            }

                        }});
                    }
                };

                $scope.validate = function() {
                    return !($scope.message.username = $scope.data.username ?"" :true)
                            |
                            !($scope.message.password = $scope.data.password ?"" :true);
                }

            }
        }
    }
});