define([], function () {//<drop-zone></drop-zone>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                yackPair: "=yackPair"
            },
            templateUrl: "views/yackPair.html",
            controller: function ($scope, $element) {

                console.log($scope.yackPair);

                interact($element[0]).dropzone({

                    // only accept elements matching this CSS selector
                    accept: '.card',
                    // Require a 75% element overlap for a drop to be possible
                    overlap: 0.60,

                    // listen for drop related events:

                    ondropactivate: function (event) {
                        //console.log("yackPair: ondropactivate");
                    },
                    ondragenter: function (event) {
                        //console.log("yackPair: ondragenter");

                        $scope.clearOtherYacks();
                        if (!$scope.yackPair.frontCard) {
                            event.target.classList.add('active');
                            event.relatedTarget.classList.add("yackCardDrop");
                        }
                        else {
                            event.target.classList.add('not-allowed');
                            event.relatedTarget.classList.remove("yackCardDrop");
                        }
                    },
                    ondragleave: function (event) {
                        //console.log("yackPair: ondragleave");
                        event.target.classList.remove('active');
                        event.target.classList.remove('not-allowed');
                        event.relatedTarget.classList.remove("yackCardDrop");
                    },
                    ondrop: function (event) {
                        //console.log("yackPair: ondrop");
                    },
                    ondropdeactivate: function (event) {
                        //console.log("yackPair: ondropdeactivate");
                        //event.relatedTarget.classList.remove('yackDrop');
                    }
                });

                $scope.clearOtherYacks = function () {
                    var otherYackPairs = document.querySelectorAll('.yack .yackPair.active');
                    for (var i = 0; i < otherYackPairs.length; i++) {
                        otherYackPairs[i].classList.remove('active');
                        otherYackPairs[i].classList.remove('not-allowed');
                    }
                }
            }
        }
    }
});