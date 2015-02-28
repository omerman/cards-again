require.config({
    baseUrl: "",
    paths: {
        'angular': 'js/angular',
        'angular-route': 'js/angular-route',
        'angularAMD': 'js/angularAMD',
        app: "app",
        sockjs: "js/sockjs",
        vertxbus: "js/vertxbus",
        dragJS: "js/drag",
        interact: "js/interact"
    },
    shim: { 'angularAMD': ['angular'], 'angular-route': ['angular'], app: ['angular']},
    deps: ['app']
});