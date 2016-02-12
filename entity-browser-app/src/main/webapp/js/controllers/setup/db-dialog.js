angular.module('xodus').controller('DBDialogController', [
    '$scope',
    '$http',
    '$uibModalInstance',
    function ($scope, $http, $modalInstance) {
        $scope.error = null;
        $scope.onSuccess = function () {
            $modalInstance.dismiss(true);
        };
        $scope.changeDB = function () {
            $scope.$broadcast('applyDBChange');
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.getMessage = function (name) {
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
        }
    }]);