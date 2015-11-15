angular.module('xodus').controller('ConfirmationController', ['$scope', '$uibModalInstance', '$q', 'item',
    function($scope, $modal, $q, item) {
        $scope.item = item;
        $scope.error = null;
        $scope.doAction = function() {
            if (angular.isFunction(item.customAction)) {
                $q.when(item.customAction()).then(function() {
                        $modal.close(true)
                    }, function() {
                        $scope.error = 'Error occurred while communication with server. Check server logs.';
                    }
                )
            } else {
                $modal.close(true);
            }
        };
        $scope.cancel = function() {
            $modal.dismiss('cancel');
        };
        $scope.closeError = function() {
            $scope.error = null;
        }
    }])
;
