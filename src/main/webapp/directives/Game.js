define([], function () {//<game></game>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/game.html",
            controller: function ($scope) {

                $scope.initData = function () {
                    $scope.data = {
                        gId: $scope.data.gId,
                        unregisterArr: []
                    };
                    $scope.setGameData(null);
                };

                $scope.setGameData = function (game) {
                    if (game) {
                        var prevDeckSize = $scope.data.size;
                        var wasITheDefender = $scope.isDefender(0);
                        $scope.data.isStarted = game.isStarted;
                        $scope.data.players = game.players;
                        $scope.data.yackInfo = game.yackInfo;
                        $scope.setFlowData(game.flowInfo);
                        $scope.setDeckData(game.deckInfo);
                        //if someone collected the yack,
                        // and prev deck size was higher than 0,
                        // and I've had a hand,
                        // and (it needs to be completed
                        //      or I was the defender and I have collected the cards which means I'm not the first attacker.)
                        if (0 == $scope.data.yackInfo.length &&
                            prevDeckSize > 0 &&
                            game.data.myHand &&
                            ($scope.data.myHand.size() < 6 || (wasITheDefender && !$scope.isFirstAttacker(0)))) {
                            $scope.getMyHand();
                        }
                    }
                    else {
                        $scope.data.isStarted = false;
                        $scope.data.isReady = false;
                        $scope.data.players = [];
                        $scope.data.shiftOpponents = 0;
                        $scope.data.yackInfo = null;
                        $scope.data.myHand = null;
                        $scope.setFlowData(null);
                        $scope.setDeckData(null);
                    }

                };

                $scope.setFlowData = function (flowInfo) {
                    if (flowInfo) {
                        $scope.data.flow = {
                            turnPosIndex: flowInfo.turnPosIndex,
                            defenderPosIndex: flowInfo.defenderPosIndex,
                            isCollectingPossible: flowInfo.isCollectingPossible,
                            isDoneAttackingPossible: flowInfo.isDoneAttackingPossible,
                            isDefenderCollecting: flowInfo.isDefenderCollecting
                        };
                    }
                    else {
                        $scope.data.flow = {
                            turnPosIndex: -1,
                            defenderPosIndex: -1,
                            isCollectingPossible: false
                        };
                    }
                };

                $scope.setDeckData = function (deckInfo) {
                    if (deckInfo) {
                        $scope.data.deck = {
                            size: deckInfo.size,
                            strongCard: deckInfo.strongCard
                        }
                    }
                    else {
                        $scope.data.deck = {
                            size: 0,
                            strongCard: null
                        };
                    }
                };

                $scope.initData();

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

                    $scope.setGameData(game);
                    $scope.$broadcast("reloadHands");
                    $scope.$broadcast("reloadYack");
                    $scope.$broadcast("reloadDeck");
                    $scope.$broadcast("reloadPlayers");

                    $scope.$apply();
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
                    return $scope.data.flow.defenderPosIndex === $scope._getShiftedPos($scope.getIndexByPositionedIndex(positionedIndex));
                };

                $scope.isFirstAttacker = function (positionedIndex) {
                    return $scope.data.flow.turnPosIndex === $scope._getShiftedPos($scope.getIndexByPositionedIndex(positionedIndex));
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
                        callBackSuccess: $scope.gameUpdate,
                        callBackError: function () {
                            $scope.changeLocation("/");
                        }
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
                $scope.collect = function () {
                    $scope.doAction({
                        action: "COLLECT"
                    });
                };
                $scope.doneAttacking = function () {
                    $scope.doAction({
                        action: "DONE_ATTACKING"
                    });
                };
                $scope.doAction = function (data) {
                    if (data && data.action) {
                        switch (data.action) {
                            case "ATTACK":
                                if (data.attackCard) {
                                    $scope.toServer({
                                        address: "game.action.request",
                                        action: $scope.ACTIONS.SEND,
                                        data: {
                                            gId: $scope.data.gId,
                                            actionType: "ATTACK",
                                            attackCard: data.attackCard
                                        },
                                        callBackSuccess: $scope.myHandUpdate,
                                        callBackError: function (callBackData) {
                                            $scope.myHandUpdate(callBackData);
                                            console.log(callBackData.error);
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
                                        },
                                        callBackError: function (callBackData) {
                                            $scope.myHandUpdate(callBackData);
                                            console.log(callBackData.error);
                                        }
                                    });
                                    break;
                                }
                            case "COLLECT":
                            {
                                $scope.toServer({
                                    address: "game.action.request",
                                    action: $scope.ACTIONS.SEND,
                                    data: {
                                        gId: $scope.data.gId,
                                        actionType: "COLLECT"
                                    },
                                    callBackSuccess: function (callBackData) {
                                        $scope.myHandUpdate(callBackData);
                                    },
                                    callBackError: function (callBackData) {
                                        $scope.myHandUpdate(callBackData);
                                        console.log(callBackData.error);
                                    }
                                });
                                break;
                            }
                            case "DONE_ATTACKING":
                            {
                                $scope.toServer({
                                    address: "game.action.request",
                                    action: $scope.ACTIONS.SEND,
                                    data: {
                                        gId: $scope.data.gId,
                                        actionType: "DONE_ATTACKING"
                                    },
                                    callBackSuccess: function (callBackData) {
                                        $scope.myHandUpdate(callBackData);
                                    },
                                    callBackError: function (callBackData) {
                                        $scope.myHandUpdate(callBackData);
                                        console.log(callBackData.error);
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
                };

                $scope.$on("doAction", function (event, data) {
                    $scope.doAction(data);
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
                            $scope.resetData();
                            console.log("FIX ME.-> Game.js");
                        }
                    }
                });

            }
        }
    }
});