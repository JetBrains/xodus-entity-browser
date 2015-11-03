angular.module('xodus').controller('ConfirmationController', ['$scope', '$uibModalInstance', 'item',
    function($scope, $modal, item) {
        $scope.item = item;
        $scope.deleteItem = function() {
            $modal.dismiss('cancel');
        };
        $scope.cancel = function() {
            $modal.close(true);
        };
    }]);
