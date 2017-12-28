angular.module('xodus').directive('entityLink', function () {
    return {
        restrict: 'E',
        scope: {
            isEditMode: '=',
            onRemove: '&'
        },
        template: require('../templates/entity-link.html'),
        link: function (scope, element, attrs) {
            scope.linkToSearch = '/databases/' + attrs.dbuuid + '/entities/' + attrs.typeid + '-' + attrs.entityid;
            scope.title = attrs.title;
        }
    }
});
