requirejs.config({
    shim: {
        "bootstrap": {
            deps: ["jquery"]
        }
    },
    paths: {
        "jquery": "jquery-2.1.4.min",
        "bootstrap": "bootstrap.min"
    }
});

require(['jquery', 'bootstrap', 'mustache'], function ($, bootStrap, m) {
    // DOM ready
    $(function () {
        // TODO
    });
});
