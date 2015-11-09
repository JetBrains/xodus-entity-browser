angular.module('xodus').controller('FormViewController', ['$scope', 'EntitiesService',
    function($scope, fields) {
        var formView = this;
        initialize();

        formView.newProperty = function() {
            $scope.properties.push(fields.newProperty());
        };
        formView.toggleView = function() {
            $scope.editMode = !$scope.editMode;
        };

        formView.removeProperty = function(property) {
            var index = $scope.properties.indexOf(property);
            $scope.properties.splice(index, 1);
        };

        formView.save = function(property) {

        };

        formView.revert = initialize;

        formView.cancel = function() {
            $scope.backToSearch();
        };
        formView.removeValue = function(property) {
            property.value = null;
        };

        function initialize() {
            var entity = angular.copy($scope.entity());

            formView.isNew = !angular.isDefined(entity.id);
            $scope.properties = entity.properties;
            $scope.blobs = entity.blobs;
            $scope.links = entity.links;

            $scope.editMode = formView.isNew;
            formView.label = (formView.isNew ? 'New ' + entity.type : entity.label);
            formView.allTypes = fields.allTypes();
        }
    }]);