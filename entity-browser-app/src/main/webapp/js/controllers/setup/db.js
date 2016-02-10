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

        $scope.dbs = [];
        databases.getSummary().then(function (summary) {
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
                db.isCurrent = (summary.location === db.location && summary.key === db.key)
            });
        });
        db.changeDB = function (database) {
            databases.update(database);
        };
        db.openDialog = function () {
            $uibModal.open({
                animation: true,
                templateUrl: 'views/directives/change-db-dialog.html',
                controller: 'DBDialogController'
            }).result.then(function (result) {
                if (result) {
                    navigation.toType();
                }
            });
        };
        db.forceReload = navigation.forceReload;
    }]
);