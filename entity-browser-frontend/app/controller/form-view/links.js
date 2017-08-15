angular.module('xodus').controller('LinksController', ['$scope', 'EntitiesService', 'EntityTypeService',
    function ($scope, entities, types) {

        function flatMap(arr, lambda) {
            return Array.prototype.concat.apply([], arr.map(lambda));
        }

        var links = this;

        $scope.uiLinks = flatMap($scope.state.current.links, function (link) {
            return link.entities;
        });

        links.entities = [];
        links.allEntityTypes = types.all();
        links.newLink = newLink();

        links.searchEntities = function (searchTerm) {
            types.search(links.newLink.type.id, searchTerm, 0, 10).then(function (data) {
                links.entities = data.items;
            });
        };

        links.updateEntities = function () {
            links.newLink.value = null;
            links.searchEntities(null);
        };

        links.removeLink = function (linkEntity) {
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

        links.addNewLink = function () {
            var linksForm = $scope.linksForm;
            $scope.makeDirty(linksForm);
            if (linksForm.$valid) {
                var found = $scope.state.current.links.find(function (link) {
                    return link.name === links.newLink.name;
                });
                var wasFound = !!found;
                if (!wasFound) {
                    found = {
                        name: links.newLink.name,
                        totalSize: 0,
                        entities: []
                    };
                }
                var newEntity = toBackendLink(links.newLink);
                found.entities.push(newEntity);
                found.totalSize++;

                if (!wasFound) {
                    $scope.state.current.links.push(found);
                }
                $scope.uiLinks.push(newEntity);

                links.newLink = newLink();
                links.updateEntities();
                linksForm.$setPristine(true);
            }
        };

        function newLink() {
            return {
                name: null,
                type: links.allEntityTypes[0],
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