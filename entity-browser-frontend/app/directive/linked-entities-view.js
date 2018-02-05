angular.module('xodus').directive('linkedEntitiesView', [
    'entitiesService',
    function (entitiesService) {
        return {
            restrict: 'E',
            scope: {
                fullDatabase: '&',
                entity: '&',
                linksPager: '&',
                isEditMode: '='
            },
            replace: true,
            template: require('../templates/entity-links-view.html'),
            link: function (scope) {
                var entities = entitiesService(scope.fullDatabase());
                scope.top = 50;
                scope.skip = 0;

                scope.linkedEntities = scope.linksPager().entities;
                scope.totalCount = scope.linksPager().totalCount;
                scope.loadMore = loadMore;
                scope.hasMore = hasMore;

                function hasMore() {
                    return scope.linkedEntities.length !== scope.totalCount;
                }

                function loadMore() {
                    var entity = scope.entity();
                    var newSkip = scope.skip + 100;
                    entities.linkedEntities(entity.id, scope.linksPager().name, scope.top, newSkip).then(function (linksPager) {
                        scope.linkedEntities = scope.linkedEntities.concat(linksPager.entities);
                        scope.skip = newSkip;
                    });
                }
            }
        };
    }]);
