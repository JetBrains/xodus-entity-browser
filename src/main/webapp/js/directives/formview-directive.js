angular.module('xodus').directive('formView', function() {
    return {
        restrict: 'E',
        scope: {
            entity: '&',
            backToSearch: '&'
        },
        replace: true,
        templateUrl: 'views/directives/form-view.html'
    };
});
