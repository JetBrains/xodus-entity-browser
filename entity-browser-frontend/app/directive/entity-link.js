angular.module('xodus').directive('entityLink', ['currentDatabase', function (currentDatabase) {
    return {
        restrict: 'E',
        scope: {
            link: '=',
            isEditMode: '=',
            isNew: '=',
            isDeleted: '=',
            onRemove: '&'
        },
        template: require('../templates/entity-link.html'),
        link: function (scope, element, attrs) {
            scope.linkToEntity = '/databases/' + currentDatabase.get().uuid + '/entities/' + scope.link.id;
            scope.title = attrs.title;
        }
    }
}]);
