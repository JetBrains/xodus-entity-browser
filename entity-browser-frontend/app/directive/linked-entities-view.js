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
                var skip = scope.linksPager().entities.length;

                scope.linkedEntities = scope.linksPager().entities;
                scope.loadMore = loadMore;
                scope.hasMore = hasMore;
                scope.removeWithCallback = removeWithCallback;

                function hasMore() {
                    return notNewLinks().length !== scope.linksPager().totalCount;
                }

                function loadMore() {
                    var entity = scope.entity();
                    entities.linkedEntities(entity.id, scope.linksPager().name, scope.top, skip).then(function (linksPager) {
                        Array.prototype.push.apply(scope.linkedEntities, linksPager.entities);
                        skip = notNewLinks().length;
                    });
                }

                function notNewLinks() {
                    return scope.linkedEntities.filter(function (link) {
                        return !link.isNew;
                    })
                }

                function removeWithCallback(linkedEntity) {
                    if (scope.isEditMode) {
                        var found = scope.linkedEntities.find(function (entity) {
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
