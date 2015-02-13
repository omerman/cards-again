define(["dragJS"], function (Draggabilly) {//<my-card></my-card>
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
            link: function ($scope, $elem, attrs) {
                var draggie = new Draggabilly($elem[0], {containment: ".game"});
                draggie.on("dragEnd", $scope.dragEnd);
                interact($elem[0]).draggable({});
            },
            controller: function ($scope, $element) {

                $scope.dragEnd = function (draggie, event, pointer) {

                    var cardClasses = $element[0].classList;


                        if (cardClasses.contains("yackCardDrop")) {
                            if(!document.querySelector(".yack .yackPair.active")) {
                                return;
                            }
                            //answer on a card.
                            $scope.$emit("doAction", {
                                action:"ANSWER",
                                attackCard: {
                                    rank: Number(document.querySelector(".yack .yackPair.active .backCard .rank").getAttribute("rank")),
                                    suit: Number(document.querySelector(".yack .yackPair.active .backCard .suit").getAttribute("suit"))
                                },
                                answerCard: {
                                    rank: $scope.rank,
                                    suit: $scope.suit
                                }
                            });
                        }
                        else if(cardClasses.contains("yackDrop")) {
                            //attack with card.
                            $scope.$emit("doAction", {
                                action:"ATTACK",
                                attackCard: {
                                    rank: $scope.rank,
                                    suit: $scope.suit
                                }
                            });
                        }
                        else {
                            $scope.toStartPoint(draggie);
                        }
                    };

                $scope.toStartPoint = function (draggie) {
                    draggie.resetLeftTop();
                };
            }
        }
    }
});