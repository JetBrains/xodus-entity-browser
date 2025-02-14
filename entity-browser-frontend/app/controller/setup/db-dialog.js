angular.module('xodus').controller('DBDialogController', [
    '$scope',
    '$http',
    '$uibModalInstance',
    'databaseService',
    function ($scope, $http, $modalInstance, databaseService) {
        var dbDialogCtrl = this;

        dbDialogCtrl.error = null;

        var hubKey = 'jetPassServerDb';
        var youtrackKey = 'teamsysstore';


        dbDialogCtrl.predefinedKeys = [
            {name: 'Hub', key: hubKey},
            {name: 'YouTrack', key: youtrackKey}
        ];

        dbDialogCtrl.keyOptions = [
            {
                title: 'Default',
                key: null
            },
            {
                title: 'YouTrack',
                key: youtrackKey
            },
            {
                title: 'Hub',
                key: hubKey
            },
            {
                title: 'Custom',
                key: ""
            }
        ];
        dbDialogCtrl.selectedkeyOption = dbDialogCtrl.keyOptions[3];
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
            key: angular.isDefined(dbDialogCtrl.key) ? dbDialogCtrl.key : null,
            opened: true,
            encrypted: false,
            readonly: true,
            watchReadonly: true
        };

        dbDialogCtrl.saveDB = function () {
            if ($scope.database.$valid) {
                databaseService.add(dbDialogCtrl.db).then(function (db) {
                    return $modalInstance.close(db);
                });
            }
        };

        dbDialogCtrl.setKeyOption = function (keyOption) {
            dbDialogCtrl.selectedkeyOption = keyOption;
            dbDialogCtrl.db.key = keyOption.key;
        };

        dbDialogCtrl.isKeyOption = function (keyOption) {
            return keyOption.title === dbDialogCtrl.selectedkeyOption.title;
        };

        dbDialogCtrl.isCustomKeyOption = function () {
            return dbDialogCtrl.selectedkeyOption.title === dbDialogCtrl.keyOptions[3].title;
        };
    }]);