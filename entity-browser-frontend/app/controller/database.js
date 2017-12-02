angular.module('xodus').controller('DatabaseController', [
    'databaseService',
    '$routeParams',
    function (databaseService, $routeParams) {
        var databaseCtrl = this;
        databaseCtrl.loaded = false;
        databaseCtrl.found = false;
        databaseCtrl.types = [];

        var db = databaseService.databases.find(function (db) {
            return db.uuid === $routeParams.databaseId;
        });

        if (db && db.opened) {
            databaseCtrl.found = true;
            databaseService.getTypes(db).then(function (types) {
                databaseCtrl.loaded = true;
                databaseCtrl.fullDB = angular.extend({}, db, {
                    types: types
                });
            })
        } else {
            databaseCtrl.loaded = true;
        }
    }]);
