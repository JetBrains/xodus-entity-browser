angular.module('xodus').controller('FormViewController', ['$scope', 'entitiesService', '$timeout', 'navigationService', 'alert',
    function ($scope, entitiesService, $timeout, navigationService, alert) {
        var formViewCtrl = this;
        var entities = entitiesService($scope.fullDatabase());
        formViewCtrl.fullDatabase = $scope.fullDatabase();
        formViewCtrl.navigation = navigationService(formViewCtrl.fullDatabase);

        formViewCtrl.find = function (items, link) {
            return items.find(function (item) {
                return item.name === link.name && item.id === link.id
            });
        };

        initialize();

        formViewCtrl.save = function () {
            // var state = $scope.state;
            var propsForm = $scope.getForm('propsForm');
            $scope.makeDirty(propsForm);
            if (propsForm.$invalid) {
                return;
            }
            var changeSummary = entities.getChangeSummary($scope.state.initial.properties, $scope.state.current.properties, []);

            entities.save($scope.state.initial, changeSummary).then(function (response) {
                alert.success($scope.state.initial.label + ' updated');
                $scope.toggleView();
                $scope.state.update(response.data);
                return response;
            }, alert.showHttpError);
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