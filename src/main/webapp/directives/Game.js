define([], function () {//<game></game>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/game.html",
            controller: function ($scope) {

                $scope.data = {
                    gId: $scope.data.gId,
                    shiftOpponents: 0,
                    deckSize:0,
                    strongCard:null,
                    yackInfo:null,
                    players: [],
                    myHand: null,
                    isReady: false,
                    isStarted: false,
                    unregisterArr: [],
                    turnPosIndex: -1,
                    defenderPosIndex: -1
                };

                $scope.readyClick = function () {
                    console.log("ready click");
                    $scope.data.isReady = !$scope.data.isReady;
                    $scope.toServer({
                        address: "game.readyOrNot.request",
                        action: $scope.ACTIONS.SEND,
                        data: {
                            gId: $scope.data.gId,
                            isReady: $scope.data.isReady
                        }
                    });
                };

                $scope.startGame = function () {
                    $scope.data.isStarted = true;
                    $scope.getMyHand();
                    window.setTimeout(function () {
                        $scope.$apply()
                    }, 0);
                };

                $scope.getMyHand = function () {
                    $scope.toServer({
                        address: "game.myHand.request",
                        data: {
                            gId: $scope.data.gId
                        },
                        action: $scope.ACTIONS.SEND,
                        callBackSuccess: $scope.myHandUpdate
                    });
                };

                $scope.gameUpdate = function (game) {
                    console.log("gameUpdate", game);

                    $scope.data.players = game.players;

                    if (game.isStarted) {
                        $scope.data.isStarted = true;
                        $scope.data.deckSize = game.deckSize;
                        $scope.data.yackInfo = game.yackInfo;
                        $scope.data.strongCard = game.strongCard;
                        $scope.flowUpdate(game);
                        $scope.$broadcast("reloadHands");
                        $scope.$broadcast("reloadYack");
                        $scope.$broadcast("reloadDeck");
                    }

                    $scope.$broadcast("reloadPlayers");

                    $scope.$apply();
                };

                $scope.flowUpdate = function (game) {
                    $scope.data.turnPosIndex = game.flowInfo.turnPosIndex;
                    $scope.data.defenderPosIndex = game.flowInfo.defenderPosIndex;
                };

                $scope.myHandUpdate = function (data) {
                    console.log("myHandUpdate", data);
                    $scope.data.myHand = data ? (data.myHand || []) : [];
                    $scope.$broadcast("reloadMyHand");
                    $scope.$apply();
                };

                $scope.getPlayerCardsNum = function (index) {
                    var player = $scope.getPlayerByIndex($scope.getIndexByPositionedIndex(index));

                    return player ? player.cardsNum : 0;
                };

                $scope.getPlayerName = function (positionedIndex) {
                    var player = $scope.getPlayerByIndex($scope.getIndexByPositionedIndex(positionedIndex));

                    return player ? player.userName : null;
                };

                $scope.isDefender = function (positionedIndex) {
                    console.log($scope.data.defenderPosIndex,positionedIndex,$scope._getShiftedPos($scope.getIndexByPositionedIndex(positionedIndex)),$scope.data.defenderPosIndex === $scope._getShiftedPos($scope.getIndexByPositionedIndex(positionedIndex)));
                    return $scope.data.defenderPosIndex === $scope._getShiftedPos($scope.getIndexByPositionedIndex(positionedIndex));
                };

                $scope.isFirstAttacker = function (positionedIndex) {
                    return $scope.data.turnPosIndex === $scope._getShiftedPos($scope.getIndexByPositionedIndex(positionedIndex));
                };

                $scope.getIndexByPositionedIndex = function (positionedIndex) {
                    var playersLen = $scope.data.players.length;
                    var index = positionedIndex;

                    if (playersLen === 2) {//so when we have one on one, they will see each other as one against the other.
                        if (2 === positionedIndex) {
                            return 1;
                        }
                        else if (1 === positionedIndex) {
                            return -1;
                        }
                    }
                    else if (playersLen === 3) {
                        if (3 === positionedIndex) {//so when we have 3 players, I will see both opponents in both of my sides.
                            return 2;
                        }
                        else if (2 === positionedIndex) {
                            return -1;
                        }
                    }

                    if (index >= playersLen) {
                        return -1;
                    }

                    return index;
                };

                $scope.getPlayerByIndex = function (index) {
                    if (-1 != index) {
                        return $scope.data.players[$scope._getShiftedPos(index)];
                    }
                    return null;
                };

                $scope._getShiftedPos = function (index) {
                    return (index + $scope.data.shiftOpponents) % $scope.data.players.length
                };

                $scope.firstGameUpdate = function (game) {

                    console.log("firstGameUpdate");

                    $scope.data.unregisterArr.push($scope.toServer({
                        address: "game.info.update." + $scope.data.gId,
                        action: $scope.ACTIONS.LISTEN,
                        callBackSuccess: $scope.gameUpdate
                    }));

                    if (game.isStarted) {
                        console.log("firstGameUpdate", "game.isStarted=true");
                        $scope.startGame();
                    }
                    else {
                        $scope.data.unregisterArr.push($scope.toServer({
                            address: "game.started." + $scope.data.gId,
                            action: $scope.ACTIONS.LISTEN,
                            callBackSuccess: $scope.startGame
                        }));
                    }

                    $scope.data.shiftOpponents = game.requesterPosIdx === -1 ? 0 : game.requesterPosIdx;
                    $scope.data.myHand = $scope.data.myHand || (game.requesterPosIdx !== -1 ? [] : null);

                    $scope.gameUpdate(game);
                };

                $scope.$on("doAction", function (event, data) {
                    if (data && data.action) {
                        switch (data.action) {
                            case "ATTACK":
                                if (data.attackCard) {
                                    $scope.toServer({
                                        address: "game.action.request",
                                        action: $scope.ACTIONS.SEND,
                                        data: {
                                            gId: $scope.data.gId,
                                            actionType:"ATTACK",
                                            attackCard: data.attackCard
                                        },
                                        callBackSuccess:function(callBackData) {
                                            $scope.myHandUpdate(callBackData);
                                        }
                                    });
                                    break;
                                }
                            case "ANSWER":
                                if (data.attackCard) {
                                    $scope.toServer({
                                        address: "game.action.request",
                                        action: $scope.ACTIONS.SEND,
                                        data: {
                                            gId: $scope.data.gId,
                                            actionType: "ANSWER",
                                            attackCard: data.attackCard,
                                            answerCard: data.answerCard
                                        },
                                        callBackSuccess: function (callBackData) {
                                            $scope.myHandUpdate(callBackData);
                                        }
                                    });
                                    break;
                                }
                            default:
                                console.log("Empty action/Illegal action", "for data: ", data);
                        }
                    }
                    else {
                        console.log("Empty action?", "for data: ", data);
                    }
                });

                $scope.toServer({
                    address: "game.info.request",
                    action: $scope.ACTIONS.SEND,
                    data: {
                        gId: $scope.data.gId
                    },
                    callBackSuccess: function (data) {

                        if (data && data.game) {
                            $scope.firstGameUpdate(data.game);
                        }
                        else {
                            console.log("FIX ME.-> Game.js");
                        }
                    }
                });

                /*$scope.$on("$destroy",function() {
                 for(var index in $scope.data.unregisterArr) {
                 $scope.data.unregisterArr[index]();
                 }
                 });*/

            }
        }
    }
});