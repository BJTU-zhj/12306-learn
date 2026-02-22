//所有的SessionKey都可以放这里，可以壁面key更改不同步问题

SESSION_ORDER="SESSION_ORDER";
SESSION_TICKET_PARAMS="SESSION_TICKET_PARAMS";

SessionStorage = {
    get: function (key) {
        var v = sessionStorage.getItem(key);
        if (v && typeof(v) !== "undefined" && v !== "undefined") {
            return JSON.parse(v);
        }
    },
    set: function (key, data) {
        sessionStorage.setItem(key, JSON.stringify(data));
    },
    remove: function (key) {
        sessionStorage.removeItem(key);
    },
    clearAll: function () {
        sessionStorage.clear();
    }
};
