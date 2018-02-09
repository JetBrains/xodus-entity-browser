angular.module('xodus').directive('linkedEntitiesView', [
    'entitiesService',
    function (entitiesService) {
        return {
            restrict: 'E',
            scope: {
                fullDatabase: '&',
                entity: '&',
                linksPager: '&',
                isEditMode: '=',
                onRemove: '&'
            },
            replace: true,
            template: require('../templates/entity-links-view.html'),
            link: function (scope) {
                var entities = entitiesService(scope.fullDatabase());
                scope.top = 50;
                scope.skip = 0;

                scope.linkedEntities = scope.linksPager().entities;
                scope.loadMore = loadMore;
                scope.hasMore = hasMore;
                scope.removeWithCallback = removeWithCallback;

                function hasMore() {
                    return scope.linkedEntities.length !== scope.linksPager().totalCount;
                }

                function loadMore() {
                    var entity = scope.entity();
                    var newSkip = scope.skip + 100;
                    entities.linkedEntities(entity.id, scope.linksPager().name, scope.top, newSkip).then(function (linksPager) {
                        scope.linkedEntities = scope.linkedEntities.concat(linksPager.entities);
                        scope.skip = newSkip;
                    });
                }

                function removeWithCallback(linkedEntity) {
                    if (scope.isEditMode) {
                        var found = scope.linksPager().entities.find(function (entity) {
                            return entity.id === linkedEntity.id;
                        });
                        if (found) {
                            found.isDeleted = true;
                            found.isNew = false;
                            if (scope.onRemove()) {
                                scope.onRemove()(linkedEntity);
                            }
                        }
                    }
                }
            }
        };
    }]);
