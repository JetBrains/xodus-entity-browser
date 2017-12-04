angular.module('xodus').controller('BlobsController', [
    '$scope',
    function ($scope, navigation) {
        $scope.uiBlobs = $scope.state.current.blobs;


        $scope.downloadLink = function (blob) {
            return $scope.formViewCtrl.navigation.api.blobLink($scope.state.current, blob.name);
        };

    }]
);