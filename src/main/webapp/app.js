define(
    ['angularAMD',
        'angular-route',
        'directives/Page',
        'directives/Login',
        'directives/Navigation',
        'directives/CreateGame',
        'directives/GamesList',
        'directives/Watch',
        'directives/Play',
        'directives/Game',
        'directives/Deck',
        'directives/Player',
        'directives/MyHand',
        'directives/OpponentHand',
        'directives/Card',
        'directives/MyCard',
        'directives/Yack',
        'directives/YackPair',
        'directives/Destroyer',
        'js/angularVertxbus',
        'interact'],
    function (angularAMD, Router, Page, Login, Navigation, CreateGame, GamesList, Watch, Play, Game, Deck, Player, MyHand, OppHand, Card, MyCard, Yack, YackPair, Destroyer) {

        var app = angular.module("webapp", ['ngRoute', 'knalli.angular-vertxbus']);


        app.directive("page", Page);//parent directive.
        app.directive("login", Login);//parent directive.
        app.directive("navigation", Navigation);
        app.directive("createGame", CreateGame);
        app.directive("gamesList", GamesList);
        app.directive("watch", Watch);
        app.directive("play", Play);
        app.directive("game", Game);
        app.directive("deck", Deck);
        app.directive("player", Player);
        app.directive("myHand", MyHand);
        app.directive("oppHand", OppHand);
        app.directive("myCard", MyCard);
        app.directive("card", Card);
        app.directive("yack", Yack);
        app.directive("yackPair", YackPair);
        app.directive("autoUnregister", Destroyer);

        app.config(['vertxEventBusProvider', '$routeProvider', function (vertxEventBusProvider, $routeProvider) {

            var hostUrlArr = window.location.href.split("/");

            vertxEventBusProvider
                .enable()
                .useReconnect()
                .useSockJsReconnectInterval(3000)
                .useSockJsStateInterval(3000)
                .useUrlServer(hostUrlArr[0] + "//" + hostUrlArr[2]);

            $routeProvider
                .when("/createGame", angularAMD.route({
                    template: "<create-game></create-game>"
                }))
                .when("/play/:gId", angularAMD.route({
                    template: function (params) {
                        return "<play g-id=" + params.gId + "></play>"
                    }
                }))
                .when("/watch/:gId", angularAMD.route({
                    template: function (params) {
                        return "<watch g-id=" + params.gId + "></watch>"
                    }
                }))
                .when("/login", angularAMD.route({
                    template: "<login></login>"
                }))
                .otherwise("/")
        }]);

        return angularAMD.bootstrap(app);
    });
