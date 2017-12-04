angular.module('xodus').controller('LinksController', ['$scope', 'entitiesService', 'EntityTypeService',
    function ($scope, entitiesService, types) {

        function flatMap(arr, lambda) {
            return Array.prototype.concat.apply([], arr.map(lambda));
        }

        var linksCtrl = this;
        var entities = entitiesService($scope.fullDatabase());

        $scope.uiLinks = flatMap($scope.state.current.links, function (link) {
            return link.entities;
        });

        linksCtrl.entities = [];
        linksCtrl.allEntityTypes = $scope.fullDatabase();
        linksCtrl.newLink = newLink();

        linksCtrl.searchEntities = function (searchTerm) {
            types.search($scope.fullDatabase(), linksCtrl.newLink.type.id, searchTerm, 0, 10).then(function (data) {
                linksCtrl.entities = data.items;
            });
        };

        linksCtrl.updateEntities = function () {
            linksCtrl.newLink.value = null;
            linksCtrl.searchEntities(null);
        };

        linksCtrl.removeLink = function (linkEntity) {
            var foundEntity = $scope.find($scope.uiLinks, linkEntity);
            if (foundEntity) {
                var index1 = $scope.uiLinks.indexOf(foundEntity);
                if (index1 >= 0) {
                    $scope.uiLinks.splice(index1, 1);
                }
            }

            var foundLink = $scope.state.current.links.find(function(l) {
                return l.name === linkEntity.name;
            });
            if (foundLink && foundLink.entities) {
                var index2 = foundLink.entities.indexOf(linkEntity);
                if (index2 >= 0) {
                    foundLink.entities.splice(index2, 1);
                }
            }
        };

        linksCtrl.addNewLink = function () {
            var linksForm = $scope.linksForm;
            $scope.makeDirty(linksForm);
            if (linksForm.$valid) {
                var found = $scope.state.current.links.find(function (link) {
                    return link.name === linksCtrl.newLink.name;
                });
                var wasFound = !!found;
                if (!wasFound) {
                    found = {
                        name: linksCtrl.newLink.name,
                        totalSize: 0,
                        entities: []
                    };
                }
                var newEntity = toBackendLink(linksCtrl.newLink);
                found.entities.push(newEntity);
                found.totalSize++;

                if (!wasFound) {
                    $scope.state.current.links.push(found);
                }
                $scope.uiLinks.push(newEntity);

                linksCtrl.newLink = newLink();
                linksCtrl.updateEntities();
                linksForm.$setPristine(true);
            }
        };

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
                typeId: link.type.id,
                type: link.type.name,
                entityId: link.value.id,
                label: link.value.label
            }
        }

        function findSame(name) {
            return $scope.uiLinks.find(function (link) {
                return link.name === name;
            });
        }

    }]);