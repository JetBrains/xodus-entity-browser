angular.module('xodus').controller('FormViewController', ['$scope', 'EntitiesService', 'EntityTypeService', '$timeout',
    function ($scope, entities, types, $timeout) {
        var formView = this;

        $scope.find = function(items, link) {
            var found = null;
            angular.forEach(items, function(item) {
                if (item.name === link.name && item.typeId === link.typeId && item.entityId === link.entityId) {
                    found = item;
                }
            });
            return found;
        };
        initialize();

        formView.save = function () {
            var state = $scope.state;
            var propsForm = $scope.getForm('propsForm');
            $scope.makeDirty(propsForm);
            if (propsForm.$invalid) {
                return;
            }
            $scope.toggleView();
            var changeSummary = entities.getChangeSummary(state.initial, state.current);
            entities.save(state.initial, changeSummary).then(function (response) {
                state.update(response.data);
            }, function (response) {
                formView.error = response.data.msg;
                $scope.toggleView();
            });
        };

        formView.closeError = function () {
            formView.error = null;
        };

        formView.revert = function () {
            $scope.state.revert();
            if (!formView.isNew) {
                $scope.toggleView();
            }
        };

        function initialize() {
            $scope.state = null;
            if ($scope.entityId) {
                entities.byId($scope.entityTypeId, $scope.entityId).then(function (entity) {
                    $scope.state = newState(entity);
                    formView.error = null;
                    updateContext();
                });
            } else {
                var type = types.byId($scope.entityTypeId);
                $scope.state = newState({
                    typeId: $scope.entityTypeId,
                    type: type.name,
                    links: [],
                    properties: [],
                    blobs: []
                });
                updateContext();
            }
        }

        function newState(entity) {
            return {
                initial: angular.copy(entity),
                current: angular.copy(entity),
                revert: function () {
                    this.current = angular.copy(this.initial);
                    forceReload();
                },
                update: function (newOne) {
                    this.initial = angular.copy(newOne);
                    this.current = angular.copy(newOne);
                    forceReload();
                }
            }
        }

        function forceReload() {
            var state = $scope.state;
            $scope.state = null;
            $timeout(function () {
                $scope.state = state;
                updateContext();
            }, 0);
        }

        function updateContext() {
            var initial = $scope.state.initial;
            formView.isNew = !angular.isDefined(initial.id);
            formView.label = (formView.isNew ? 'New ' + initial.type : initial.label);
        }
    }]);