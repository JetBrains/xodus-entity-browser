angular.module('xodus').directive('formView', function() {
    return {
        restrict: 'E',
        scope: {
            entityId: '=',
            entityTypeId: '=',
            backToSearch: '&'
        },
        replace: true,
        templateUrl: 'views/directives/form-view.html',
        link: function(scope, element, attrs) {
            scope.editMode = (scope.entityId === null);
            scope.toggleView = function() {
                scope.editMode = !scope.editMode;
            };
            scope.cancel = function() {
                scope.backToSearch();
            };
            scope.getMessage = getMessage;

            scope.hasError = function(formName, inputName) {
                var field = getForm(formName)[inputName];
                return field && field.$invalid && field.$dirty;
            };
            scope.makeDirty = function(form) {
                angular.forEach(Object.keys(form), function(key) {
                    var value = form[key];
                    if (value && angular.isFunction(value.$setDirty)) {
                        value.$setDirty(true);
                    }
                });
            };
            scope.getForm = getForm;

            function getMessage(formName, name) {
                var field = getForm(formName)[name];
                if (('undefined' === typeof(field)) || field.$valid) {
                    return undefined;
                }

                var message = '';
                if (field.$error['number']) {
                    message += ' - not a number';
                }
                if (field.$error['min']) {
                    message += ' - too small';
                }
                if (field.$error['max']) {
                    message += ' - too large';
                }
                if (field.$error['pattern']) {
                    message += ' - not match pattern';
                }
                if (field.$error['duplicated']) {
                    message += ' - duplicated';
                }
                if (field.$error['required'] && !message) {
                    message += ' - required field';
                }
                return '\u2718' + message;
            }

            function getForm(name) {
                return element.find('form[name="' + name + '"]').scope()[name];
            }
        }
    };
});
