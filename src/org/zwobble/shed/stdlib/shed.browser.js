(function(exports) {
    var modules = {};
    var moduleCallbacks = {};
    
    exports.export = function(name, value) {
        var parts = name.split(".");
        var current = exports;
        for (var i = 0; i < parts.length - 1; i += 1) {
            current[parts[i]] = current[parts[i]] || {};
            current = current[parts[i]];
        }
        current[parts[parts.length - 1]] = value;
        modules[name] = value;
        
        for (var i = 0; i < moduleCallbacks[name].length; i += 1) {
            moduleCallbacks[name][i](name, value);
        }
    };
    
    exports.require = function() {
        var moduleNames = Array.prototype.slice.call(arguments, 0, -1);
        var moduleValueArguments = [];
        var moduleValueArgumentsReceived = 0;
        var originalCallback = arguments[arguments.length - 1];
        var callback = function(moduleName, moduleValue) {
            moduleValueArguments[moduleNames.indexOf(moduleName)] = moduleValue;
            moduleValueArgumentsReceived += 1;
            if (moduleValueArgumentsReceived === moduleNames.length) {
                originalCallback.apply(null, moduleValueArguments);
            }
        };
        moduleNames.forEach(function(name) {
            if (name in modules) {
                callback(name, modules[name]);
            } else {
                moduleCallbacks[name] = moduleCallbacks[name] || [];
                moduleCallbacks[name].push(callback);
                
                var scriptTag = document.createElement('script');
                scriptTag.type = 'text/javascript';
                scriptTag.src = '/stdlib/' + name.replace(/\./g, "/") + ".js";
                scriptTag.async = true;
                var firstScript = document.getElementsByTagName('script')[0];
                firstScript.parentNode.insertBefore(scriptTag, firstScript);
            }
        });
    };
    
    exports.alert = function(value) {
        alert(value)
    };
})(window.SHED = {});
