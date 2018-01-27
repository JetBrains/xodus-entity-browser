angular.module('xodus').controller('DBDialogController', [
    '$scope',
    '$http',
    '$uibModalInstance',
    'databaseService',
    '$route',
    function ($scope, $http, $modalInstance, databaseService, $route) {
        var dbDialogCtrl = this;
        dbDialogCtrl.error = null;

        dbDialogCtrl.onSuccess = function () {
            $modalInstance.dismiss(true);
        };

        dbDialogCtrl.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        dbDialogCtrl.getMessage = function (name) {
            var field = $scope.database[name];
            if (('undefined' === typeof(field)) || field.$valid) {
                return undefined;
            }
            var message = '';
            if (field.$error['required']) {
                message += ' - is required';
            } else {
                message += ' - is invalid';
            }
            return message;
        };

        dbDialogCtrl.db = {
            location: angular.isDefined(dbDialogCtrl.location) ? dbDialogCtrl.location : null,
            key: angular.isDefined(dbDialogCtrl.key) ? dbDialogCtrl.key : null
        };

        dbDialogCtrl.saveDB = function () {
            if ($scope.database.$valid) {
                databaseService.update(dbDialogCtrl.db).then(function (data) {
                    $route.reload();
                }, function () {
                    dbDialogCtrl.error = 'Error while changing database. Changes not applied.';
                });
            }
        };
        dbDialogCtrl.closeError = function () {
            dbDialogCtrl.error = null;
        };

        var hubKey = 'jetPassServerDb';
        var youtrackKey = 'teamsysstore';


        dbDialogCtrl.predefinedKeys = [
            {name: 'Hub', key: hubKey},
            {name: 'YouTrack', key: youtrackKey}
        ];

        dbDialogCtrl.applyKey = function (item) {
            dbDialogCtrl.db.key = item.key;
        };
    }]);