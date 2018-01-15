angular.module('xodus').factory('navigationService', [
    '$location',
    '$window',
    function ($location, $window) {
        return function (db) {
            var prefix = '/databases/' + db.uuid + '/';

            function blobLink(entity, name) {
                return 'api/dbs/' + db.uuid + '/entities/' + entity.typeId + '-' + entity.id + "/blob/" + name;
            }

            function toType(typeId) {
                return angular.isDefined(typeId) ? $location.path(prefix).search({typeId: typeId}) : toType(0);
            }

            function toEntity(typeId, entityId, edit) {
                if (entityId) {
                    $location.path(prefix + 'entities/' + typeId + '-' + entityId);
                    var params = {
                        typeId: null
                    };
                    if (edit) {
                        params.edit = true;
                    }
                    $location.search(params);
                } else {
                    $location.path(prefix + '/entities/new');
                }
            }

            function forceReload() {
                toType();
                $window.location.reload();
            }

            return {
                toType: toType,
                toEntity: toEntity,
                forceReload: forceReload,
                api: {
                    blobLink: blobLink
                }
            };
        };
    }]
);
