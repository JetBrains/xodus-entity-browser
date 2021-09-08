angular.module('xodus').controller('BlobsController', [
    '$scope',
    'navigationService',
    'currentDatabase',
    function ($scope, navigationService, currentDatabase) {
        $scope.uiBlobs = $scope.state.current.blobs;
        var navigation = navigationService(currentDatabase.get());

        $scope.downloadLink = function (blob) {
            return navigation.api.blobLink($scope.state.current.id, blob.name);
        };

        $scope.downloadStringLink = function (blob) {
            return navigation.api.blobStringLink($scope.state.current.id, blob.name);
        };

    }]
);