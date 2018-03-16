angular.module('xodus').controller('DatabasesController', [
        '$scope',
        '$http',
        '$uibModal',
        '$route',
        'EntityTypeService',
        'navigationService',
        'databaseService',
        'ConfirmationService',
        'alert',
        function ($scope, http, $uibModal, $route, types, navigation, databaseService, confirmationService, alert) {
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
                    } else if (!db.key) {
                        db.description = 'Default';
                    } else {
                        db.description = db.key;
                    }
                }
                return db;
            });

            databasesCtrl.openDialog = function () {
                $uibModal.open({
                    animation: true,
                    template: require('../templates/new-db-dialog.html'),
                    controller: 'DBDialogController',
                    controllerAs: 'dbDialogCtrl'
                }).result.then(function (db) {
                    alert.success('Database added: ' + db.location);
                    $route.reload();
                    return db;
                });
            };

            databasesCtrl.deleteDB = function (database) {
                confirmDelete(database, function () {
                    databaseService.deleteDB(database).then(function () {
                        $route.reload();
                    });
                });
            };

            databasesCtrl.startOrStop = function (database, isStart) {
                databaseService.startOrStop(database, isStart).then(function () {
                    $route.reload();
                });
            };

            function confirmDelete(db, callback) {
                return confirmationService({
                    label: 'Delete database',
                    message: 'Are you sure you want to delete database ' + db.description + ':' + db.location + ' ?',
                    action: 'Delete'
                }, function (result) {
                    if (result) {
                        callback();
                    }
                });
            }

        }
    ]
);