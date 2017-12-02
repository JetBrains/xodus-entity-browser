angular.module('xodus').controller('BlobsController', [
    '$scope',
    'navigationService',
    function ($scope, navigation) {
        $scope.uiBlobs = $scope.state.current.blobs;

        $scope.downloadLink = function (blob) {
            return navigation.api.blobLink($scope.state.current, blob.name);
        };

    }]
);