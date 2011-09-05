(function() {
    var JavaScriptImporter = function() {
        return {
            importValue: function(type) {
                return function(name) {
                    var parts = name.split(".");
                    var current = window;
                    for (var i = 0; i < parts.length; i += 1) {
                        current = current[parts[i]];
                    }
                    return current;
                };
            }
        };
    };
    SHED.export("shed.javascript.JavaScriptImporter", JavaScriptImporter);
})();
