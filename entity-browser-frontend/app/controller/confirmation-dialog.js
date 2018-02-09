angular.module('xodus').controller('ConfirmationController',
    [
        '$scope',
        '$uibModalInstance',
        '$q',
        'item',
        'alert',
        function ($scope, $modal, $q, item, alert) {
            $scope.item = item;
            $scope.doAction = function () {
                if (angular.isFunction(item.customAction)) {
                    $q.when(item.customAction()).catch(alert.showHttpError).then(function () {
                        return $modal.close(true);
                    });
                } else {
                    $modal.close(true);
                }
            };
            $scope.cancel = function () {
                $modal.dismiss('cancel');
            };
        }]
);
