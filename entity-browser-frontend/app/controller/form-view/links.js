angular.module('xodus').controller('LinksController', ['$scope', 'entitiesService', 'EntityTypeService',
    function ($scope, entitiesService, types) {
        var linksCtrl = this;

        $scope.uiLinks = $scope.state.current.links;

        linksCtrl.entities = [];
        linksCtrl.allEntityTypes = $scope.fullDatabase().types;
        linksCtrl.newLink = newLink();

        linksCtrl.searchEntities = function (q) {
            types.search($scope.fullDatabase(), linksCtrl.newLink.type.id, q, 0, 10).then(function (data) {
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
                        totalCount: 0,
                        entities: []
                    };
                }
                var newEntity = toBackendLink(linksCtrl.newLink);
                found.entities.push(newEntity);
                found.totalCount++;

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
                id: link.value.id,
                typeId: link.type.id,
                type: link.type.name,
                label: link.value.label
            }
        }

    }]);