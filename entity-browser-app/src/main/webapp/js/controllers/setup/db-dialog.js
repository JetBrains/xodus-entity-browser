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
    }]);