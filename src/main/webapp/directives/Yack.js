/**
 * Created by omerpr on 06/02/2015.
 */
define([], function () {//<yack></yack>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/yack.html",
            scope: {
                "getYack": "&yackInfo"
            },
            controller: function ($scope, $element) {

                $scope.data = {
                    yack: [
                        /*{backCard:{suit:3,rank:3},frontCard:{suit:2,rank:2}},
                         {backCard:{suit:3,rank:7},frontCard:{suit:2,rank:10}},
                         {backCard:{suit:3,rank:7},frontCard:{suit:2,rank:10}}*/
                    ],
                    unregisterArr: [],
                    hoveringYack: false
                };

                interact($element[0]).dropzone({

                    /*var draggableElement = event.relatedTarget,
                     dropzoneElement = event.target;*/

                    // only accept elements matching this CSS selector
                    accept: '.card',
                    // Require a 75% element overlap for a drop to be possible
                    overlap: 0.75,

                    // listen for drop related events:

                    ondropactivate: function (event) {
                        //console.log("ondropactivate");
                    },
                    ondragenter: function (event) {
                        //console.log("ondragenter");
                        event.relatedTarget.classList.add('yackDrop');
                    },
                    ondragleave: function (event) {
                        //console.log("ondragleave");
                        event.relatedTarget.classList.remove('yackDrop');

                    },
                    ondrop: function (event) {
                        //console.log("ondrop");
                    },
                    ondropdeactivate: function (event) {
                        //console.log("ondropdeactivate");
                        //event.relatedTarget.classList.remove('yackDrop');
                    }
                });

                $scope.data.unregisterArr.push($scope.$on('reloadYack', function () {
                    $scope.data.yack = $scope.getYack();

                }));

            }
        }
    }
});