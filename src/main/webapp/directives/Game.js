define([], function () {//<game></game>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/game.html",
            controller: function ($scope) {

                $scope.isHandExists = function () {
                    return $scope.data.myHand != null;
                };

                $scope.initData = function () {
                    $scope.data = {
                        gId: $scope.data.gId,
                        unregisterArr: []
                    };
                    $scope.setGameData(null);
                };

                $scope.setGameData = function (game) {
                    if (game) {
                        var prevDeckSize = $scope.data.deck.size;
                        var prevLoser = $scope.data.loser;
                        var wasITheDefender = $scope.isDefender(0);

                        $scope.data.isStarted = game.isStarted;
                        $scope.data.players = game.players;
                        $scope.data.yackInfo = game.yackInfo;
                        $scope.data.loser = game.loser;
                        $scope.setFlowData(game.flowInfo);
                        $scope.setDeckData(game.deckInfo);
                        //if someone collected the yack,
                        // and I've had a hand,
                        // and
                        // (    (it needs to be completed and prev deck size was higher than 0)
                        //      or I was the defender and I have collected the cards which means I'm not the first attacker.
                        // )
                        if ($scope.data.yackInfo && !$scope.data.yackInfo.length &&
                            $scope.isHandExists() &&
                            ((prevLoser || ($scope.data.myHand.length < 6 && prevDeckSize > 0) ) || (wasITheDefender && !$scope.isFirstAttacker(0)))) {
                            console.log("FETCH MY HAND!");
                            $scope.getMyHand();
                        }
                    }
                    else {
                        $scope.data.isStarted = false;
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
                            isDefenderCollecting: flowInfo.isDefenderCollecting,
                            doneAttackingUsers: flowInfo.doneAttackingUsers
                        };
                    }
                    else {
                        $scope.data.flow = {
                            turnPosIndex: -1,
                            defenderPosIndex: -1,
                            isCollectingPossible: false,
                            isDoneAttackingPossible: false,
                            isDefenderCollecting: false,
                            doneAttackingUsers: []
                        };
                    }
                };

                $scope.setDeckData = function (deckInfo) {
                    if (deckInfo) {
                        $scope.data.deck = {
                            size: deckInfo.size,
                            strongCard: deckInfo.strongCard,
                            strongCardSuit: deckInfo.strongCardSuit
                        }
                    }
                    else {
                        $scope.data.deck = {
                            size: 0
                        };
                    }
                };

                $scope.initData();

                $scope.readyClick = function () {
                    $scope.toServer({
                        address: "game.readyOrNot.request",
                        action: $scope.ACTIONS.SEND,
                        data: {
                            gId: $scope.data.gId
                        }
                    });
                };

                $scope.startGame = function () {
                    window.setTimeout(function () {
                        $scope.$apply(function () {
                            console.log("applying");
                            $scope.data.isStarted = true;
                            $scope.getMyHand();
                        });
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


                    $scope.$apply(function () {
                        console.log("Game data", game);
                        console.log("applying");
                        $scope.setGameData(game);
                        $scope.$broadcast("reloadHands");
                        $scope.$broadcast("reloadYack");
                        $scope.$broadcast("reloadDeck");
                        $scope.$broadcast("reloadPlayers");
                    });
                };

                $scope.myHandUpdate = function (data) {


                    $scope.$apply(function () {
                        console.log("applying");
                        if (!data || !data.myHand) {
                            $scope.data.myHand = [];
                        }
                        else {
                            $scope.data.myHand = data.myHand.sort(function (elem1, elem2) {
                                return  ($scope.isStrongCard(elem1) && !$scope.isStrongCard(elem2)) ||
                                    !($scope.isStrongCard(elem2) && !$scope.isStrongCard(elem1)) &&
                                    (elem1.suit * 100) + elem1.rank > (elem2.suit * 100) + elem2.rank;
                            });
                        }
                        $scope.$broadcast("reloadMyHand");
                    });
                };

                $scope.isStrongCard = function (card) {
                    return $scope.data.deck.strongCardSuit === card.suit;
                };

                $scope.getPlayerCardsNum = function (positionedIndex) {
                    console.log("getPlayerCardsNum", positionedIndex);
                    var player = $scope.getPlayerByIndex($scope.getIndexByPositionedIndex(positionedIndex));

                    return player ? player.cardsNum : 0;
                };

                $scope.getPlayerName = function (positionedIndex) {
                    var player = $scope.getPlayerByIndex($scope.getIndexByPositionedIndex(positionedIndex));

                    return player ? player.userName : null;
                };

                $scope.isGameStarted = function () {
                    return $scope.data.isStarted;
                };

                $scope.isPlayerExists = function (positionedIndex) {
                    return $scope.getPlayerByIndex($scope.getIndexByPositionedIndex(positionedIndex));
                };

                $scope.isPlayerReady = function (positionedIndex) {
                    if ($scope.isGameStarted()) {
                        return false;
                    }
                    var player = $scope.getPlayerByIndex($scope.getIndexByPositionedIndex(positionedIndex));
                    return player && player.isReady;
                };

                $scope.isPlayerDone = function (positionedIndex) {
                    if (!$scope.isGameStarted()) {
                        return false;
                    }
                    return -1 != $scope.data.flow.doneAttackingUsers.indexOf($scope.getPlayerName(positionedIndex));
                };

                $scope.isDefender = function (positionedIndex) {
                    return $scope.data.flow.defenderPosIndex === $scope._getShiftedPos($scope.getIndexByPositionedIndex(positionedIndex));
                };

                $scope.isDefenderCollecting = function () {
                    return $scope.data.flow.isDefenderCollecting;
                };

                $scope.isFirstAttacker = function (positionedIndex) {
                    return $scope.data.flow.turnPosIndex === $scope._getShiftedPos($scope.getIndexByPositionedIndex(positionedIndex));
                };

                $scope.isLoser = function (playerName) {
                    return $scope.data.loser === playerName;
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

                    $scope.data.unregisterArr.push($scope.toServer({
                        address: "game.info.update." + $scope.data.gId,
                        action: $scope.ACTIONS.LISTEN,
                        callBackSuccess: $scope.gameUpdate,
                        callBackError: function () {
                            $scope.changeLocation("/");
                        }
                    }));

                    if (game.isStarted) {
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

                    if (!$scope.data.myHand) {
                        $scope.data.myHand = game.requesterPosIdx !== -1 ? [] : null;
                    }


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
                $scope.getPlayerData = function (positionedIndex) {
                    var playerName = $scope.getPlayerName(positionedIndex)
                    return {
                        name: playerName,
                        isPlayerReady: $scope.isPlayerReady(positionedIndex),
                        isPlayerDone: $scope.isPlayerDone(positionedIndex),
                        isDefender: $scope.isDefender(positionedIndex),
                        isFirstAttacker: $scope.isFirstAttacker(positionedIndex),
                        isCollecting: $scope.isDefenderCollecting(),
                        isLoser: $scope.isLoser(playerName)
                    };
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
                            $scope.setGameData(null);
                            console.log("FIX ME.-> Game.js");
                        }
                    }
                });

            }
        }
    }
});