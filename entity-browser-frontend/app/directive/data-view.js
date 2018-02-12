angular.module('xodus').directive('typeView', [function () {
    return {
        restrict: 'E',
        scope: {},
        replace: true,
        controller: 'DataViewController',
        controllerAs: 'dataViewCtrl',
        bindToController: true,
        template: require('../templates/data-view.html')
    };
}]);
