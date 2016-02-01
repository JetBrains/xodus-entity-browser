angular.module('xodus').directive('entityTypeView', function() {
    return {
        restrict: 'E',
        scope: {
            selectedType: '&'
        },
        replace: true,
        templateUrl: 'views/directives/data-view.html'
    };
});
