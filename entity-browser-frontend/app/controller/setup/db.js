angular.module('xodus').controller('DBController', [
        '$scope',
        '$http',
        '$uibModal',
        '$route',
        'EntityTypeService',
        'NavigationService',
        'DatabaseService',
        function ($scope, http, $uibModal, $route, types, navigation, databases) {
            var db = this;

            var hubKey = 'jetPassServerDb';
            var youtrackKey = 'teamsysstore';

            db.predefinedKeys = [
                {name: 'Hub', key: hubKey},
                {name: 'YouTrack', key: youtrackKey}
            ];
            $scope.dbs = [];
            databases.getAppState().then(function (summary) {
                $scope.dbs = summary.recent;
                angular.forEach($scope.dbs, function (db) {
                    if (!db.title) {
                        if (db.key === hubKey) {
                            db.title = 'HUB: ' + db.location;
                        } else if (db.key === youtrackKey) {
                            db.title = 'YouTrack: ' + db.location;
                        } else {
                            db.title = db.key + ':' + db.location;
                        }
                    }
                    db.isCurrent = summary.current && (summary.current.location === db.location && summary.current.key === db.key)
                })
            });
            db.changeDB = function (database) {
                databases.update(database);
            };
            db.openDialog = function () {
                $uibModal.open({
                    animation: true,
                    template: require('../../templates/change-db-dialog.html'),
                    controller: 'DBDialogController',
                    controllerAs: 'dbDialog'
                }).result.then(function (result) {
                    if (result) {
                        navigation.toType();
                    }
                });
            };
            db.forceReload = navigation.forceReload;
            db.deleteDB = function (database, $event) {
                $event.stopPropagation();
                if (database.markForDelete) {
                    databases.deleteDB(database);
                    return;
                }
                database.markForDelete = true;
            };
            db.undoDeleteDB = function (database, $event) {
                database.markForDelete = false;
                $event.stopPropagation();
            };
        }
    ]
)
;