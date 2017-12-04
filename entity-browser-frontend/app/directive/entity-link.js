angular.module('xodus').directive('entityLink', function () {
    return {
        restrict: 'E',
        template: function (element, attrs) {
            return '<a href="/databases/' + attrs.dbuuid+ '/entities/' + attrs.typeid + '-' + attrs.entityid + '">' + attrs.title + '</a>';
        }
    };
});
