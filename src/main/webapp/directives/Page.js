define([], function () {//<page></page>
    return function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/page.html",
            controller: ["$scope", "$location", "vertxEventBus", function ($scope, $location, vertxEventBus) {

                //DEFINE FUNCS & PARAMS - START

                //if eventbus not connected, fire the event on connect(using onReadyQueue).
                //otherwise, fire event now.
                $scope.onReadyQueue = [];
                $scope.callBackAfterLogin = null;
                $scope.eventBusConnected = $scope.eventBusConnected || false;

                /**
                 *
                 * @param settings.address
                 * @param settings.action
                 * @param settings.data
                 * @param settings.callBackSuccess
                 * @param settings.callBackError
                 * @param settings.callBackAfterLogin
                 */
                $scope.toServer = function (settings) {

                    var address = settings.address;
                    var action = settings.action;
                    var data = settings.data;
                    var callBackSuccess = settings.callBackSuccess;
                    var callBackError = settings.callBackError;
                    var callBackAfterLogin = settings.callBackAfterLogin;
                    var returnVal = null;

                    var wrappedCallBack = function (callBackData) {

                        window.setTimeout(function () {
                            if (callBackData) {
                                if (callBackData.status === "auth_required") {

                                    if (callBackAfterLogin) {
                                        $scope.callBackAfterLogin = callBackAfterLogin;

                                        $scope.changeLocation("/login")
                                    }
                                    else {
                                        console.log("address: " + address + "requires log in. supply 'callBackAfterLogin' for auto redirect to login page.");
                                    }
                                    return;
                                }
                                if (callBackData.error) {
                                    if (callBackError) {
                                        callBackError(callBackData);
                                    }
                                    else {
                                        alert(callBackData.error);
                                    }
                                    return;
                                }
                            }
                            if (callBackSuccess) {
                                callBackSuccess(callBackData);
                            }
                        }, 0);
                    };

                    var func;
                    if (action === $scope.ACTIONS.SEND) {
                        func = function () {
                            vertxEventBus.send(address, data, wrappedCallBack);
                        };
                    } else if (action === $scope.ACTIONS.LISTEN) {
                        func = function () {
                            vertxEventBus.registerHandler(address, wrappedCallBack);
                        };
                        returnVal = function () {
                            vertxEventBus.unregisterHandler(address, wrappedCallBack);
                        }
                    } else {
                        console.error("$scope.toServer requires action to be populated with a value from this if-else ;)");
                        return;
                    }

                    if (vertxEventBus.readyState() === 1) {//vertxEventBus is ready.
                        func();
                    }
                    else {//verxEventBus is not ready.
                        console.log("Not ready yet, inserting to queue");

                        $scope.onReadyQueue.push(func);
                    }

                    return returnVal;
                };

                /**
                 *
                 * @param settings.data
                 * @param settings.callBack
                 */
                $scope.login = function (settings) {

                    var data = settings.data;
                    var callBack = settings.callBack;

                    vertxEventBus.login(data.username, data.password, function (callBackData) {

                        var isLoggedIn = callBackData && callBackData.status === "ok";

                        if (typeof callBack === "function") {
                            callBack(isLoggedIn);
                        }
                        if (typeof $scope.callBackAfterLogin === "function") {

                            var futureFunc = $scope.callBackAfterLogin;
                            window.setTimeout(function () {
                                futureFunc(isLoggedIn);
                            }, 500);

                            $scope.callBackAfterLogin = null;

                        }

                        if (!isLoggedIn) {
                            console.log("could not log in");
                        }
                        else {
                            $scope.$broadcast("loggedIn", callBackData);
                        }
                    });
                };

                $scope.changeLocation = function (path) {
                    $scope.$apply(function () {
                        $location.path(path);
                    });
                };

                $scope.ACTIONS = {
                    SEND: "SEND",
                    LISTEN: "LISTEN"
                };

                //DEFINE FUNCS & PARAMS - END

                //going through onReadyQueue, and executing calls to server.
                vertxEventBus.onopen = function () {

                    var func;
                    while ($scope.onReadyQueue.length) {
                        func = $scope.onReadyQueue.shift();
                        func();
                    }
                    $scope.eventBusConnected = true;
                };

                vertxEventBus.onclose = function () {
                    if ($scope.eventBusConnected) {
                        window.location.reload();//reload the whole page.
                    }
                };


            }]
        }
    }
});