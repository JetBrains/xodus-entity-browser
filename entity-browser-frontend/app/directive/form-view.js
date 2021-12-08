angular.module('xodus').directive('formView', [
    '$uibModal',
    '$location',
    '$window',
    'navigationService',
    'ConfirmationService',
    'currentDatabase',
    function ($uibModal, $location, $window, navigationService, confirmation, currentDatabase) {
        return {
            restrict: 'E',
            scope: {
                entity: '&'
            },
            replace: true,
            template: require('../templates/form-view.html'),
            link: function (scope, element) {
                var navigation = navigationService(currentDatabase.get());

                scope.editMode = (scope.entity().id === null || $location.search().edit);
                scope.toggleView = function () {
                    scope.editMode = !scope.editMode;
                };
                scope.cancel = function () {
                    if (scope.editMode && !currentDatabase.get().readonly) {
                        confirmExit(toSearch);
                    } else {
                        navigation.toType(scope.entity().typeId);
                    }
                };
                scope.getMessage = getMessage;

                scope.hasError = function (formName, inputName) {
                    var field = getForm(formName)[inputName];
                    return field && field.$invalid && field.$dirty;
                };
                scope.makeDirty = function (form) {
                    angular.forEach(Object.keys(form), function (key) {
                        var value = form[key];
                        if (value && angular.isFunction(value.$setDirty)) {
                            value.$setDirty(true);
                        }
                    });
                };
                scope.getForm = getForm;
                var cleanUp = scope.$on('$locationChangeStart', function (event, next) {
                        if (scope.editMode && !currentDatabase.get().readonly) {
                            confirmExit(function () {
                                $window.location = next; //$location.path is too buggy
                            });
                            event.preventDefault();
                        }
                    }
                );
                scope.$on('$destroy', cleanUp);

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
                    return angular.element($('form[name="' + name + '"]')).scope()[name];
                }

                function confirmExit(callback) {
                    return confirmation({
                        label: 'You can loose unsaved data',
                        message: 'Are you sure to proceed?',
                        action: 'Proceed'
                    }, function (result) {
                        if (result) {
                            scope.editMode = false;
                            callback();
                        }
                    });
                }

                function toSearch() {
                    navigation.toType(scope.entity().typeId);
                }
            }
        };
    }])
;
