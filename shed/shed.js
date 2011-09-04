(function(exports) {
    var unit = {};
    var identityFunction = function(value) {
        return value;
    };
    exports.String = identityFunction;
    exports.Boolean = identityFunction;
    exports.Number = identityFunction;
    exports.Unit = function() {
        return unit;
    };
    for (var i = 0; i < 20; i += 1) {
        exports["Function" + i] = function() {
            return {};
        };
    }
    
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
