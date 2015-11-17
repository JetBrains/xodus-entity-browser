angular.module('xodus').controller('BlobsController', ['$scope', '$http', function($scope, $http) {
    $scope.uiBlobs = $scope.state.current.blobs;

    $scope.downloadLink = function(blob) {
        return 'api/type/' + $scope.entityTypeId + '/entity/' + $scope.entityId + "/blob/" + blob.name;
    };

}]);