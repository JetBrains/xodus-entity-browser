angular.module('xodus').directive('entityTypeView', [function () {
    return {
        restrict: 'E',
        scope: {
            selectedType: '&'
        },
        replace: true,
        template: require('../templates/data-view.html')
    };
}]);
