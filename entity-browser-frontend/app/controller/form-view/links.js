angular.module('xodus').controller('LinksController', ['$scope', 'EntitiesService', 'EntityTypeService',
    function($scope, entities, types) {
        var links = this;

        $scope.uiLinks = $scope.state.current.links;

        links.allEntityTypes = [];
        links.entities = [];
        types.all().then(function(data) {
            links.allEntityTypes = data;
            links.newLink = newLink();
        });

        links.searchEntities = function(searchTerm) {
            types.search(links.newLink.type.id, searchTerm, 0, 10).then(function(data) {
                links.entities = data.items;
            });
        };

        links.updateEntities = function() {
            links.newLink.value = null;
            links.searchEntities(null);
        };

        links.removeLink = function(link) {
            var found = $scope.find($scope.uiLinks, link);
            if (found) {
                var index = $scope.uiLinks.indexOf(found);
                $scope.uiLinks.splice(index, 1);
            }
        };

        links.addNewLink = function() {
            var linksForm = $scope.linksForm;
            $scope.makeDirty(linksForm);
            if (linksForm.$valid) {
                var founded = findSame(links.newLink.name);
                if (founded) {
                    linksForm.name.$setValidity("duplicated", false);
                    return;
                }
                $scope.state.current.links.push(toBackendLink(links.newLink));
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
            var founded = null;
            angular.forEach($scope.uiLinks, function(link) {
                if (link.name == name) {
                    founded = link;
                }
            });
            return founded;
        }

    }]);