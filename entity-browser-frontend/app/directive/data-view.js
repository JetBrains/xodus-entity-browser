angular.module('xodus').directive('entityTypeView', ['templateUrl', function (templateUrl) {
    return {
        restrict: 'E',
        scope: {
            selectedType: '&'
        },
        replace: true,
        template: require('../templates/data-view.html')
    };
}]);
