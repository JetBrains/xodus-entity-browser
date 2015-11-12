angular.module('xodus').directive('formView', function() {
    return {
        restrict: 'E',
        scope: {
            entityId: '=',
            entityTypeId: '=',
            backToSearch: '&'
        },
        replace: true,
        templateUrl: 'views/directives/form-view.html'
    };
});
