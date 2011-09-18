(function() {
    var exports = {};
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
    
    SHED.exportValue("shed.core", exports);
})();
