angular.module('xodus').controller('LinksController', ['$scope', 'entitiesService', 'EntityTypeService',
    function ($scope, entitiesService, types) {
        var linksCtrl = this;

        linksCtrl.entities = [];
        linksCtrl.allEntityTypes = $scope.fullDatabase().types;
        linksCtrl.newLink = newLink();
        linksCtrl.currentLinks = currentLinks;

        linksCtrl.searchEntities = function (q) {
            types.search($scope.fullDatabase(), linksCtrl.newLink.type.id, q, 0, 10).then(function (data) {
                linksCtrl.entities = data.items;
            });
        };

        linksCtrl.resetNewEntityType = function () {
            linksCtrl.newLink.value = null;
            linksCtrl.searchEntities(null);
        };

        linksCtrl.onRemoveLink = function (linksChanges) {
            return function (linkedEntity) {
                linksChanges.push({
                    name: linkedEntity.name,
                    oldValue: linkedEntity,
                    newValue: null
                });
            };
        };

        linksCtrl.totallyRemoveLink = function (name, linksChanges) {
            var foundEntity = currentLinks().find(function (link) {
                return link.name === name;
            });

            if (foundEntity) {
                var index = currentLinks().indexOf(foundEntity);
                if (index >= 0) {
                    currentLinks().splice(index, 1);
                }
                linksChanges.push({
                    name: name,
                    totallyRemoved: true
                });
            }
        };

        linksCtrl.addNewLink = function (link, linksChanges) {
            var linksForm = $scope.linksForm;
            $scope.makeDirty(linksForm);
            if (linksForm.$valid) {
                var found = currentLinks().find(function (link) {
                    return link.name === linksCtrl.newLink.name;
                });
                var wasFound = !!found;
                if (!wasFound) {
                    found = {
                        name: linksCtrl.newLink.name,
                        totalCount: 0,
                        entities: []
                    };
                }
                var newEntity = toBackendLink(linksCtrl.newLink);
                found.entities.splice(0, 0, newEntity);

                if (!wasFound) {
                    currentLinks().push(found);
                }

                linksCtrl.newLink = newLink();
                linksCtrl.resetNewEntityType();
                linksChanges.push({
                    name: link.name,
                    newValue: newEntity
                });
                linksForm.$setPristine(true);
            }
        };

        function currentLinks() {
            return $scope.state.current.links;
        }

        function newLink() {
            return {
                name: null,
                type: $scope.fullDatabase().types[0],
                value: null
            }
        }

        function toBackendLink(link) {
            return {
                name: link.name,
                id: link.value.id,
                typeId: link.type.id,
                type: link.type.name,
                label: link.value.label,
                isNew: true
            }
        }

    }]);