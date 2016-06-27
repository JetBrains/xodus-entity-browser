angular.module('xodus').controller('EntityController', [
    '$scope',
    '$routeParams',
    function ($scope, $routeParams) {
        $scope.typeId = $routeParams.typeId;
        $scope.entityId = $routeParams.entityId;
        if (!angular.isDefined($scope.entityId)) {
            $scope.entityId = null;
        }
    }]
);