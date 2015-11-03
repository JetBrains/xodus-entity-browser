angular.module('xodus').directive('entityTypeView', function() {
    return {
        restrict: 'E',
        scope: {
            selectedType: '&'
        },
        replace: true,
        templateUrl: 'views/dataview-directive.html'
    };
});
