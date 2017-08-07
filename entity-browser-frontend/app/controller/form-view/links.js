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

        links.removeLink = function (link) {
            var found = $scope.find($scope.uiLinks, link);
            if (found) {
                var index = $scope.uiLinks.indexOf(found);
                $scope.uiLinks.splice(index, 1);
            }
        };

        links.addNewLink = function () {
            var linksForm = $scope.linksForm;
            $scope.makeDirty(linksForm);
            if (linksForm.$valid) {
                var founded = $scope.state.current.links.find(function (link) {
                    return link.name === links.newLink.name;
                });
                if (!founded) {
                    founded = {
                        totalSize: 0,
                        entities: []
                    };
                }
                founded.push(toBackendLink(links.newLink));
                founded.totalSize++;

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