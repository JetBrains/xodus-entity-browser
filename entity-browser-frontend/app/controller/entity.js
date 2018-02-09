angular.module('xodus').controller('EntityController', [
    'databaseService',
    'entitiesService',
    '$routeParams',
    '$location',
    '$q',
    function (databaseService, entitiesService, $routeParams, $location, $q) {
        var entityCtrl = this;
        entityCtrl.loaded = false;
        entityCtrl.types = [];

        var db = databaseService.databases.find(function (db) {
            return db.uuid === $routeParams.databaseId;
        });

        if (db && db.opened) {
            databaseService.getTypes(db).then(function (types) {
                entityCtrl.fullDB = angular.extend({}, db, {
                    types: types
                });
            }).then(function () {
                var entityId = $routeParams.entityId;
                entityCtrl.isNew = (entityId === 'new');
                if (entityCtrl.isNew) {
                    var typeId = parseInt($location.search().typeId);
                    var type = entityCtrl.fullDB.types.find(function (type) {
                        return type.id === typeId;
                    });
                    return $q.when({
                        typeId: type.id,
                        type: type,
                        links: [],
                        properties: [],
                        blobs: [],
                        id: null
                    });
                } else {
                    return entitiesService(entityCtrl.fullDB).byId(null, entityId);
                }
            }).then(function (data) {
                entityCtrl.entity = data;
                entityCtrl.loaded = true;
            })
        } else {
            entityCtrl.loaded = true;
        }
    }]);
