angular.module('xodus').controller('FormViewController', [
    '$scope',
    'entitiesService',
    '$timeout',
    'navigationService',
    'alert',
    'currentDatabase',
    function ($scope, entitiesService, $timeout, navigationService, alert, currentDatabase) {
        var formViewCtrl = this;
        var fullDatabase = currentDatabase.get();
        var entities = entitiesService(fullDatabase);

        function initialize() {
            $scope.state = newState($scope.entity());
            updateContext();
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
            formViewCtrl.linkChanges = [];
            $timeout(function () {
                $scope.state = state;
                updateContext();
            }, 0);
        }

        function updateContext() {
            var initial = $scope.state.initial;
            formViewCtrl.isNew = !angular.isDefined(initial.id);
            formViewCtrl.label = (formViewCtrl.isNew ? 'New ' + initial.type : initial.label);
        }

        function find(items, link) {
            return items.find(function (item) {
                return item.name === link.name && item.id === link.id
            });
        }

        function save() {
            var propsForm = $scope.getForm('propsForm');
            $scope.makeDirty(propsForm);
            if (propsForm.$invalid) {
                return;
            }
            var changeSummary = entities.getChanges($scope.state.initial.properties, $scope.state.current.properties, formViewCtrl.linkChanges);

            entities.save($scope.state.initial, changeSummary).then(function (response) {
                var savedEntity = response.data;
                alert.success(savedEntity.label + ' updated');
                $scope.toggleView();
                if ($scope.state.initial.id) {
                    $scope.state.update(savedEntity);
                    return response;
                } else {
                    formViewCtrl.navigation.toEntity(savedEntity.typeId, savedEntity.id, false);
                }
            }, alert.showHttpError);
        }

        function revert() {
            $scope.state.revert();
            if (!formViewCtrl.isNew) {
                $scope.toggleView();
            }
        }

        formViewCtrl.find = find;
        formViewCtrl.save = save;
        formViewCtrl.revert = revert;
        formViewCtrl.linkChanges = [];
        formViewCtrl.navigation = navigationService(fullDatabase);

        initialize();
    }]);