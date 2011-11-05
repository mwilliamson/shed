(function() {
    var NodeJavaScriptImporter = function() {
        return {
            importValueFromModule: function(type) {
                return function(moduleName, valueName) {
                    var module = require(moduleName);
                    var parts = valueName.split(".");
                    var current = module;
                    for (var i = 0; i < parts.length; i += 1) {
                        current = current[parts[i]];
                    }
                    return current;
                };
            }
        };
    };
    SHED.exportValue("shed.javascript.NodeJavaScriptImporter", NodeJavaScriptImporter);
})();
