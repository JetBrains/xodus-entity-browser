angular.module('xodus').controller('DatabaseController', [
    'databaseService',
    '$routeParams',
    'currentDatabase',
    function (databaseService, $routeParams, currentDatabase) {
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
                databaseCtrl.fullDatabase = angular.extend({}, db, {
                    types: types
                });
                currentDatabase.set(databaseCtrl.fullDatabase)
            })
        } else {
            databaseCtrl.loaded = true;
        }
    }]);
