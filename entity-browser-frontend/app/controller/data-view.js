const FileSaver = require('file-saver');

angular.module('xodus').controller('DataViewController', [
    'EntityTypeService',
    'entitiesService',
    'navigationService',
    '$scope',
    '$uibModal',
    '$routeParams',
    'currentDatabase',
    '$http',
    function (typeService, entitiesService, navigationService, $scope, $uibModal, $routeParams, currentDatabase, $http) {
        const dataViewCtrl = this;
        dataViewCtrl.$onInit = function () {
            const navigation = navigationService(currentDatabase.get());

            dataViewCtrl.searchQuery = searchQuery();
            dataViewCtrl.pageSize = 50;
            dataViewCtrl.type = databaseType();
            dataViewCtrl.fullDatabase = currentDatabase.get();
            const entities = entitiesService(currentDatabase.get());

            $scope.$on('$routeUpdate', function () {
                dataViewCtrl.searchQuery = searchQuery();
                dataViewCtrl.type = databaseType();
                dataViewCtrl.pager = newPager(dataViewCtrl.searchQuery);
                dataViewCtrl.pager.pageChanged(1);
            });

            dataViewCtrl.pager = newPager(dataViewCtrl.searchQuery);
            dataViewCtrl.newInstance = entities.newEntity(dataViewCtrl.type.id, dataViewCtrl.type.name);

            //comment this if you want to load data on view show
            dataViewCtrl.pager.pageChanged(1);

            dataViewCtrl.edit = function (item) {
                navigation.toEntity(item.typeId, item.id, true);
            };
            dataViewCtrl.hasLinksToDisplay = function (entity) {
                return (entity.links || []).find(function (link) {
                    return link.totalCount > 0;
                });
            };

            dataViewCtrl.deleteItem = function (item) {
                $uibModal.open({
                    animation: true,
                    template: require('../templates/confirmation-dialog.html'),
                    controller: 'ConfirmationController',
                    resolve: {
                        item: function () {
                            return {
                                label: 'Deleting ' + item.label,
                                message: 'Are you sure you want to delete ' + item.label + '?',
                                action: 'Delete',
                                customAction: function () {
                                    return entities.deleteEntity(item.id);
                                }
                            };
                        }
                    }
                }).result.then(function (result) {
                    if (result) {
                        dataViewCtrl.refresh();
                    }
                });
            };

            dataViewCtrl.refresh = function () {
                dataViewCtrl.pager.pageChanged();
            };

            dataViewCtrl.downloadBlob = function (entity, blob) {
              return navigation.downloadBlob(entity, blob);
            };

            dataViewCtrl.downloadBlobString = function (entity, blob) {
                return navigation.downloadBlobString(entity, blob);
            };
        };

        function newPager(searchTerm) {
            return {
                totalCount: 0,
                items: [],
                error: null,
                currentPage: 1,
                expanded: {},
                pageChanged: function () {
                    const pageNo = this.currentPage;
                    const offset = (pageNo - 1) * dataViewCtrl.pageSize;
                    const self = this;
                    self.currentPage = pageNo;
                    typeService.search(currentDatabase.get(), dataViewCtrl.type.id, searchTerm, offset)
                        .then(function (data) {
                            self.items = data.items;
                            self.totalCount = data.totalCount;
                            self.error = null;
                            dataViewCtrl.isSearchExecuted = true;
                        }, function (error) {
                            if (error.data && error.data.msg) {
                                self.error = error.data.msg;
                                dataViewCtrl.isSearchExecuted = true;
                            }
                        });
                },
                hasPagination: function () {
                    return this.totalCount > dataViewCtrl.pageSize;
                },
                hasResults: function () {
                    return this.items.length > 0;
                },
                expand: function (entity) {
                    this.expanded[entity.id] = true;
                },
                isExpanded: function (entity) {
                    return angular.isDefined(this.expanded[entity.id]);
                }
            };
        }

        function searchQuery() {
            return $routeParams.q ? $routeParams.q : null;
        }

        function databaseType() {
            const typeId = $routeParams.typeId ? $routeParams.typeId : currentDatabase.get().types[0].id;
            return currentDatabase.get().types.find(function (type) {
                return type.id === parseInt(typeId);
            });
        }
    }]);

