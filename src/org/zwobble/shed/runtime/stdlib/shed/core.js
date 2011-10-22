SHED.exportValue("shed.core", (function() {
    var exports = {};
    var unit = {};
    var identityFunction = function(value) {
        return value;
    };
    var String = exports.String = identityFunction;
    var Boolean = exports.Boolean = identityFunction;
    var Number = exports.Number = function(value) {
        return {
            __value: value,
            add: function(other) {
                return Number(value + other.__value);
            },
            subtract: function(other) {
                return Number(value - other.__value);
            },
            multiply: function(other) {
                return Number(value * other.__value);
            },
            toString: function() {
                return String(value.toString());
            },
            equals: function(other) {
                return Boolean(value == other.__value);
            }
        };
    };
    exports.Unit = function() {
        return unit;
    };
    for (var i = 0; i < 20; i += 1) {
        exports["Function" + i] = function() {
            return {};
        };
    }
})());
