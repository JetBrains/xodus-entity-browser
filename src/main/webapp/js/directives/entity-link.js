angular.module('xodus').directive('entityLink', function () {
    return {
        restrict: 'E',
        template: function (element, attrs) {
            return '<a href="#/type/' + attrs.typeid + '/entity/' + attrs.entityid + '">' + attrs.title + '</a>';
        }
    };
});
