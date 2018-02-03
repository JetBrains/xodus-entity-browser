angular.module('xodus')
    .directive('toggleCheckbox', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            transclude: true,
            replace: false,
            require: 'ngModel',
            link: function ($scope, $element, $attr, require) {
                var ngModel = require;
                $element.parent().addClass('animation-disabled');

                function updateModelFromElement() {
                    var checked = $element.prop('checked');
                    if (checked !== ngModel.$viewValue) {
                        ngModel.$setViewValue(checked);
                        $scope.$apply();
                    }
                }

                function updateElementFromModel() {
                    $element.trigger('change');
                }

                $element.on('change', updateModelFromElement);

                $scope.$watch(function () {
                    return ngModel.$viewValue;
                }, updateElementFromModel);

                $scope.$watch(function () {
                    return $element.attr('disabled');
                }, function (newVal) {
                    $element.bootstrapToggle(!newVal ? "enable" : "disable");
                });

                $element.bootstrapToggle();
            }
        };
    }]);