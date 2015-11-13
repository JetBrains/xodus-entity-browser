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

        props.checkName = function(property) {
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

        function findSame(name) {
            var founded = null;
            angular.forEach($scope.properties, function(link) {
                if (link.name == name) {
                    founded = link;
                }
            });
            return founded;
        }
    }]);