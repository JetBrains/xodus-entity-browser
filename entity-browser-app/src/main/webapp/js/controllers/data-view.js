angular.module('xodus').controller('DataViewController', [
    'EntityTypeService',
    'EntitiesService',
    'NavigationService',
    '$scope',
    '$uibModal',
    '$routeParams',
    function (types, entities, navigation, $scope, $uibModal, $routeParams) {
        var dataView = this;
        dataView.searchQuery = searchQuery();
        dataView.pageSize = 50;
        $scope.type = $scope.selectedType();
        $scope.$on('$routeUpdate', function () {
            dataView.searchQuery = searchQuery();
            dataView.pager = newPager(dataView.searchQuery);
            dataView.pager.pageChanged(1);
        });
        dataView.pager = newPager(dataView.searchQuery);
        dataView.newInstance = entities.newEntity($scope.type.id, $scope.type.name);

        //comment this if you want to load data on view show
        dataView.pager.pageChanged(1);

        dataView.edit = function (item) {
            navigation.toEntity(item.typeId, item.id);
        };

        dataView.deleteItem = function (item) {
            $uibModal.open({
                animation: true,
                templateUrl: 'views/directives/confirmation-dialog.html',
                controller: 'ConfirmationController',
                resolve: {
                    item: function () {
                        return {
                            label: 'Deleting ' + item.label,
                            message: 'Are you sure you want to delete ' + item.label + '?',
                            action: 'Delete',
                            customAction: function () {
                                return entities.deleteEntity(item.typeId, item.id);
                            }
                        };
                    }
                }
            }).result.then(function (result) {
                if (result) {
                    dataView.onSearch();
                }
            });
        };

        dataView.refresh = function () {
            dataView.pager.pageChanged();
        };

        dataView.blobLink = function (entity, blob) {
            return navigation.api.blobLink(entity, blob.name);
        };

        function newPager(searchTerm) {
            return {
                totalCount: 0,
                items: [],
                currentPage: 1,
                expanded: {},
                pageChanged: function () {
                    var pageNo = this.currentPage;
                    var offset = (pageNo - 1) * dataView.pageSize;
                    var self = this;
                    self.currentPage = pageNo;
                    types.search($scope.selectedType().id, searchTerm, offset).then(function (data) {
                        self.items = data.items;
                        self.totalCount = data.totalCount;
                        dataView.isSearchExecuted = true;
                    });
                },
                hasPagination: function () {
                    return this.totalCount > dataView.pageSize;
                },
                hasResults: function () {
                    return this.items.length > 0;
                },
                expand: function (entity) {
                    this.expanded[entity.id] = true;
                },
                isExpanded: function(entity){
                    return angular.isDefined(this.expanded[entity.id]);
                }
            };
        }

        function searchQuery() {
            return $routeParams.q ? $routeParams.q : null;
        }
    }]);

