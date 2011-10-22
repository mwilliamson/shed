(function() {
    var sys = require("sys");
    
    SHED.exportValue("shed.sys", {
        print: function(str) {
            sys.print(str);
        }
    });
})();
