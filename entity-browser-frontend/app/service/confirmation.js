angular.module('xodus').factory('ConfirmationService', [
    '$uibModal',
    'templateUrl',
    function ($uibModal, templateUrl) {
        return function (text, callback) {
            return $uibModal.open({
                animation: true,
                template: require('../templates/confirmation-dialog.html'),
                controller: 'ConfirmationController',
                resolve: {
                    item: function () {
                        return text;
                    }
                }
            }).result.then(function (result) {
                if (callback && result) {
                    callback(result)
                }
                return result
            });
        }
    }]
);

