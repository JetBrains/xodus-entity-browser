angular.module('xodus').directive('entityLink', function () {
    return {
        restrict: 'E',
        scope: {
            link: '=',
            dbUuid: '=',
            isEditMode: '=',
            onRemove: '&'
        },
        template: require('../templates/entity-link.html'),
        link: function (scope, element, attrs) {
            scope.linkToEntity = '/databases/' + scope.dbUuid + '/entities/' + scope.link.id;
            scope.title = attrs.title;
        }
    }
});
