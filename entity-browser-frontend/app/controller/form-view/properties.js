angular.module('xodus').controller('PropertiesController', ['$scope', 'entitiesService',
    function($scope, entitiesService) {
        var props = this;
        var entities = entitiesService($scope.fullDatabase());

        $scope.properties = $scope.state.current.properties;
        angular.forEach($scope.properties, entities.appendValidation);
        props.allTypes = entities.allTypes();

        props.newProperty = function() {
            $scope.properties.push(entities.newProperty());
        };

        props.removeProperty = function(property) {
            var index = $scope.properties.indexOf(property);
            $scope.properties.splice(index, 1);
        };

        props.removeValue = function(property) {
            property.value = null;
        };

        props.checkDuplicates = function(property) {
            var inputName = $scope.properties.indexOf(property) + 'name';
            var propsForm = $scope.propsForm;
            var sameProperties = $scope.properties.filter(function(item) {
                return property.name === item.name;
            });
            propsForm[inputName].$setValidity("duplicated", sameProperties.length <= 1);
        };

        //stock angular min/max validation works bad with dynamic types
        props.validateType = function(property) {
            var maxValue = property.type.validation.maxValue;
            var minValue = property.type.validation.minValue;
            if (maxValue || minValue) {
                var value = parseInt(property.value);
                if (value) {
                    var inputName = $scope.properties.indexOf(property) + 'value';
                    var input = $scope.propsForm[inputName];
                    input.$setValidity("max", !maxValue || value < maxValue);
                    input.$setValidity("min", !minValue || value > minValue);
                }
            }
        }

    }]);