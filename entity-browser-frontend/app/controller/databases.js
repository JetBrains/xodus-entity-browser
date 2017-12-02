angular.module('xodus').controller('DatabasesController', [
        '$scope',
        '$http',
        '$uibModal',
        '$route',
        'EntityTypeService',
        'navigationService',
        'databaseService',
        function ($scope, http, $uibModal, $route, types, navigation, databaseService) {
            var databasesCtrl = this;
            var hubKey = 'jetPassServerDb';
            var youtrackKey = 'teamsysstore';

            databasesCtrl.predefinedKeys = [
                {name: 'Hub', key: hubKey},
                {name: 'YouTrack', key: youtrackKey}
            ];
            databasesCtrl.dbs = databaseService.databases.map(function (db) {
                if (!db.title) {
                    if (db.key === hubKey) {
                        db.description = 'HUB';
                    } else if (db.key === youtrackKey) {
                        db.description = 'YouTrack';
                    } else {
                        db.description = db.key;
                    }
                }
                return db;
            });

            databasesCtrl.changeDB = function (database) {
                databaseService.update(database);
            };

            databasesCtrl.openDialog = function () {
                $uibModal.open({
                    animation: true,
                    template: require('../templates/new-db-dialog.html'),
                    controller: 'DBDialogController',
                    controllerAs: 'dbDialog'
                }).result.then(function (result) {
                    if (result) {
                        databaseService.databases.push(result.db);
                    }
                });
            };
            databasesCtrl.forceReload = navigation.forceReload;
            databasesCtrl.deleteDB = function (database, $event) {
                $event.stopPropagation();
                if (database.markForDelete) {
                    databaseService.deleteDB(database);
                    return;
                }
                database.markForDelete = true;
            };
            databasesCtrl.undoDeleteDB = function (database, $event) {
                database.markForDelete = false;
                $event.stopPropagation();
            };
        }
    ]
);