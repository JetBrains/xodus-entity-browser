angular.module('xodus').controller('FormViewController', ['$scope', 'EntitiesService', 'EntityTypeService', '$timeout',
    function($scope, entities, types, $timeout) {
        var formView = this;

        $scope.foundByName = function(items, name) {
            var found = null;
            angular.forEach(items, function(item) {
                if (name === item.name) {
                    found = item;
                }
            });
            return found;
        };
        initialize();

        formView.save = function() {
            var state = $scope.state;
            var propsForm = $scope.getForm('propsForm');
            $scope.makeDirty(propsForm);
            if (propsForm.$invalid) {
                return;
            }
            $scope.toggleView();
            var changeSummary = entities.getChangeSummary(state.initial, state.current);
            entities.save(state.initial, changeSummary).then(function(response) {
                state.update(response.data);
            }, function(response) {
                formView.error = response.data.msg;
                $scope.toggleView();
            });
        };

        formView.closeError = function() {
            formView.error = null;
        };

        formView.revert = function() {
            $scope.state.revert();
        };

        function initialize() {
            $scope.state = null;
            if ($scope.entityId) {
                entities.byId($scope.entityTypeId, $scope.entityId).then(function(entity) {
                    $scope.state = newState(entity);
                    formView.error = null;
                    updateContext();
                    formView.allTypes = entities.allTypes();
                });
            } else {
                types.byId($scope.entityTypeId).then(function(type) {
                    $scope.state = newState({
                        typeId: $scope.entityTypeId,
                        type: type.name,
                        links: [],
                        properties: [],
                        blobs: []
                    });
                    updateContext();
                });
            }
        }

        function newState(entity) {
            return {
                initial: angular.copy(entity),
                current: angular.copy(entity),
                revert: function() {
                    this.current = angular.copy(this.initial);
                    forceReload();
                },
                update: function(newOne) {
                    this.initial = angular.copy(newOne);
                    this.current = angular.copy(newOne);
                    forceReload();
                }
            }
        }

        function forceReload() {
            var state = $scope.state;
            $scope.state = null;
            $timeout(function() {
                $scope.state = state;
                updateContext();
            }, 0);
        }

        function updateContext() {
            var initial = $scope.state.initial;
            formView.isNew = !angular.isDefined(initial.id);
            formView.label = (formView.isNew ? 'New ' + initial.type : initial.label);
        }
    }])
    .controller('BlobsController', ['$scope', '$http', function($scope, $http) {
        $scope.uiBlobs = $scope.state.current.blobs;

        $scope.download = function(blob) {
            var path = 'api/type/' + $scope.entityTypeId + '/entity/' + $scope.entityId + "/blob/" + blob.name;
            $http.get(path).success(function(data) {
                var anchor = angular.element('<a/>');
                anchor.attr({
                    href: 'data:attachment/text;charset=utf-8' + encodeURI(data),
                    target: '_blank',
                    download: 'blob.txt'
                })[0].click();
            });
        }

    }]);