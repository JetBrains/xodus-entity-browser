angular.module('xodus').controller('PropertiesController',
    [
        '$scope',
        'entitiesService',
        'currentDatabase',
        function ($scope, entitiesService, currentDatabase) {
            var propertiesCtrl = this;
            var entities = entitiesService(currentDatabase.get());

            propertiesCtrl.properties = $scope.state.current.properties;
            propertiesCtrl.allPropertyTypes = entities.allPropertyTypes();
            propertiesCtrl.properties.forEach(entities.appendValidation);

            propertiesCtrl.newProperty = function () {
                propertiesCtrl.properties.push(entities.newProperty());
            };

            propertiesCtrl.removeProperty = function (property) {
                var index = propertiesCtrl.properties.indexOf(property);
                propertiesCtrl.properties.splice(index, 1);
            };

            propertiesCtrl.removeValue = function (property) {
                property.value = null;
            };

            propertiesCtrl.checkDuplicates = function (property) {
                var inputName = propertiesCtrl.properties.indexOf(property) + 'name';
                var propsForm = $scope.propsForm;
                var sameProperties = propertiesCtrl.properties.filter(function (item) {
                    return property.name === item.name;
                });
                propsForm[inputName].$setValidity("duplicated", sameProperties.length <= 1);
            };

            //stock angular min/max validation works bad with dynamic types
            propertiesCtrl.validateType = function (property) {
                var maxValue = property.type.validation.maxValue;
                var minValue = property.type.validation.minValue;
                if (maxValue || minValue) {
                    var value = parseInt(property.value);
                    if (value) {
                        var inputName = propertiesCtrl.properties.indexOf(property) + 'value';
                        var input = $scope.propsForm[inputName];
                        input.$setValidity("max", !maxValue || value < maxValue);
                        input.$setValidity("min", !minValue || value > minValue);
                    }
                }
            }

        }]);