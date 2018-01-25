angular.module('xodus').controller('FormViewController', ['$scope', 'entitiesService', '$timeout', 'navigationService',
    function ($scope, entitiesService, $timeout, navigationService) {
        var formViewCtrl = this;
        var entities = entitiesService($scope.fullDatabase());
        formViewCtrl.fullDatabase = $scope.fullDatabase();
        formViewCtrl.navigation = navigationService(formViewCtrl.fullDatabase);

        formViewCtrl.changeSummary = {

        };

        formViewCtrl.find = function (items, link) {
            var found = null;
            angular.forEach(items, function (item) {
                if (item.name === link.name && item.typeId === link.typeId && item.entityId === link.entityId) {
                    found = item;
                }
            });
            return found;
        };
        initialize();

        formViewCtrl.save = function () {
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
                formViewCtrl.error = response.data && response.data.msg || 'Unknown error';
                $scope.toggleView();
            });
        };

        formViewCtrl.closeError = function () {
            formViewCtrl.error = null;
        };

        formViewCtrl.revert = function () {
            $scope.state.revert();
            if (!formViewCtrl.isNew) {
                $scope.toggleView();
            }
        };

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
    }]);