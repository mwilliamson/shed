(function() {
    var isNode = typeof exports === "undefined";
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
        
        if (!isNode) {
            exports.require = function(name) {
                ensureStdLibFileIsLoaded(name);
            };
        } 
        
    })(isNode ? (window.SHED = {}) : exports);
})();
