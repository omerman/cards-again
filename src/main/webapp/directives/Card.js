define([], function () {//<card></card>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                rank: '=',
                suit: "=",
                accept: "&"
            },
            templateUrl: "views/card.html",
            controller: function ($scope) {
            }
        }
    }
});