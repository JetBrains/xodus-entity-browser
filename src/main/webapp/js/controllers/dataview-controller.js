angular.module('xodus').controller('DataViewController', ['EntityTypeService', 'EntitiesService', '$scope', '$uibModal',
    function(types, entities, $scope, $uibModal) {
        var dataView = this;
        dataView.isSearchExecuted = false;
        dataView.isListView = true;

        dataView.searchQuery = null;
        dataView.pageSize = 50;
        $scope.type = $scope.selectedType();
        dataView.pager = newPager(null);
        dataView.newInstance = entities.newEntity($scope.type.id, $scope.type.name);
        dataView.currentEntityId = null;

        //uncomment this if you want to load data on view show
        //dataView.pager.pageChanged(1);

        dataView.onSearch = function() {
            dataView.pager = newPager(dataView.searchQuery);
            dataView.pager.pageChanged();
        };

        dataView.toggleView = toggleView;
        dataView.newEntity = function() {
            toggleView();
            dataView.currentEntityId = null;
        };

        dataView.edit = function(item) {
            toggleView();
            dataView.currentEntityId = item.id;
        };

        dataView.deleteItem = function(item) {
            $uibModal.open({
                animation: true,
                templateUrl: 'views/directives/confirmation-dialog.html',
                controller: 'ConfirmationController',
                resolve: {
                    item: function() {
                        return {
                            label: 'Deleting ' + item.label,
                            message: 'Are you sure you want to delete ' + item.label + '?',
                            action: 'Delete',
                            customAction: function() {
                                return entities.deleteEntity(item.typeId, item.id);
                            }
                        };
                    }
                }
            }).result.then(function(result) {
                    if (result) {
                        dataView.onSearch();
                    }
                });
        };

        dataView.refresh = function() {
            dataView.toggleView();
            dataView.pager.pageChanged();
        };

        $scope.openInfo = function() {
            var searchInfo = $uibModal.open({
                animation: true,
                templateUrl: 'search-info.html',
                size: 'lg',
                controller: ['$scope', function($scope) {
                    $scope.closeInfo = function() {
                        searchInfo.dismiss();
                    };
                }]
            })
        };

        function newPager(searchTerm) {
            return {
                totalCount: 0,
                items: [],
                currentPage: 1,
                pageChanged: function() {
                    var pageNo = this.currentPage;
                    var offset = (pageNo - 1) * dataView.pageSize;
                    var self = this;
                    self.currentPage = pageNo;
                    types.search($scope.selectedType().id, searchTerm, offset).then(function(data) {
                        self.items = data.items;
                        self.totalCount = data.totalCount;
                        dataView.isSearchExecuted = true;
                    });
                },
                hasPagination: function() {
                    return this.totalCount > dataView.pageSize;
                },
                hasResults: function() {
                    return this.items.length > 0;
                }
            };
        }

        function toggleView() {
            dataView.isListView = !dataView.isListView;
        }
    }]);

