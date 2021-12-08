angular.module('xodus').controller('BlobsController', [
    '$scope',
    'navigationService',
    'currentDatabase',
    function ($scope, navigationService, currentDatabase) {
        const blobs = this;
        $scope.uiBlobs = $scope.state.current.blobs;
        const navigation = navigationService(currentDatabase.get());

        blobs.downloadBlob = function (blob) {
            return navigation.downloadBlob($scope.state.current, blob);
        };

        blobs.downloadBlobString = function (blob) {
            return navigation.downloadBlobString($scope.state.current, blob);
        };

    }]
);