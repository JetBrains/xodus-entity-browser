angular.module('xodus').controller('BlobsController', ['$scope', '$http', function($scope, $http) {
    $scope.uiBlobs = $scope.state.current.blobs;

    $scope.download = function(blob) {
        var path = 'api/type/' + $scope.entityTypeId + '/entity/' + $scope.entityId + "/blob/" + blob.name;
        $http.get(path).success(function(data) {
            var anchor = angular.element('<a/>');
            anchor.attr({
                href: 'data:attachment/text;charset=utf-8' + encodeURI(data),
                target: '_blank',
                download: 'blob.txt'
            })[0].click();
        });
    }
}]);