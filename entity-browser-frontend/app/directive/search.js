angular.module('xodus').directive('search', [function () {
    return {
        restrict: 'E',
        scope: {},
        replace: true,
        controller: 'SearchController',
        controllerAs: 'searchCtrl',
        bindToController: true,
        template: require('../templates/search.html')
    };
}]);
