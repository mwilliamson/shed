(function(exports) {
    exports.export = function(name, value) {
        var parts = name.split(".");
        var current = exports;
        for (var i = 0; i < parts.length - 1; i += 1) {
            current[parts[i]] = current[parts[i]] || {};
            current = current[parts[i]];
        }
        current[parts[i]] = value;
    };
})(typeof exports === "undefined" ? (window.SHED = {}) : exports);
