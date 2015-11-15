angular.module('xodus').controller('PropertiesController', ['$scope', 'EntitiesService',
    function($scope, entities) {
        var props = this;
        $scope.properties = $scope.state.current.properties;

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
                return property.name == item.name;
            });
            if (sameProperties.length > 1) {
                propsForm[inputName].$setValidity("duplicated", false);
            } else {
                propsForm[inputName].$setValidity("duplicated", true);
            }
        };

        //stock angular min/max validation works bad with dynamic types
        props.validateType = function(property) {
            var maxValue = property.type.maxValue;
            var minValue = property.type.minValue;
            if (maxValue || minValue) {
                var value = parseInt(property.value);
                if (value) {
                    var inputName = $scope.properties.indexOf(property) + 'value';
                    var input = $scope.propsForm[inputName];
                    if (maxValue && value > maxValue) {
                        input.$setValidity("max", false);
                    } else {
                        input.$setValidity("max", true);
                    }
                    if (minValue && value < minValue) {
                        input.$setValidity("min", false);
                    } else {
                        input.$setValidity("min", true);
                    }
                }
            }
        }

    }]);