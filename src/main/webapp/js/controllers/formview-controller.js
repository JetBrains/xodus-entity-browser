angular.module('xodus').controller('FormViewController', ['$scope', 'EntitiesService',
    function($scope, entities) {
        var formView = this;
        formView.hasError = false;
        var state = {
            initial: angular.copy($scope.entity()),
            current: angular.copy($scope.entity()),
            revert: function() {
                this.current = angular.copy($scope.entity());
            },
            update: function(newOne) {
                this.initial = this.current;
                this.current = newOne;
            }
        };
        initialize();

        formView.newProperty = function() {
            $scope.properties.push(entities.newProperty());
        };
        formView.toggleView = function() {
            $scope.editMode = !$scope.editMode;
        };

        formView.removeProperty = function(property) {
            var index = $scope.properties.indexOf(property);
            $scope.properties.splice(index, 1);
        };

        formView.save = function() {
            formView.editMode = false;
            var changeSummary = entities.getChangeSummary(state.initial, state.current);
            entities.save(state.initial, changeSummary).then(function(response) {
                state.update(response.data);
                initialize();
            }, function(response) {
                formView.hasError = true;
                formView.error = response.data.msg;
                formView.editMode = true;
            });
        };

        formView.revert = function() {
            state.revert();
            initialize();
        };

        formView.cancel = function() {
            $scope.backToSearch();
        };
        formView.removeValue = function(property) {
            property.value = null;
        };

        function initialize() {
            formView.hasError = false;
            formView.isNew = !angular.isDefined(state.initial.id);
            $scope.properties = state.current.properties;
            $scope.blobs = state.current.blobs;
            $scope.links = state.current.links;

            $scope.editMode = formView.isNew;
            formView.label = (formView.isNew ? 'New ' + state.initial.type : state.initial.label);
            formView.allTypes = entities.allTypes();
        }
    }]);