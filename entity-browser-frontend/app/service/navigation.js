import FileSaver from 'file-saver';

angular.module('xodus').factory('navigationService', [
    '$location',
    '$window',
    '$http',
    function ($location, $window, $http) {
        return function (db) {
            var prefix = 'databases/' + db.uuid + '/';

            function toType(typeId) {
                return angular.isDefined(typeId) ? $location.path(prefix).search({typeId: typeId.toString()}) : toType(0);
            }

            function toEntity(typeId, entityId, edit) {
                if (entityId) {
                    $location.path(prefix + 'entities/' + entityId);
                    var params = {
                        typeId: null
                    };
                    if (edit) {
                        params.edit = true;
                    }
                    $location.search(params);
                } else {
                    $location.path(prefix + 'entities/new').search({
                        typeId: typeId.toString()
                    });
                }
            }

            function forceReload() {
                toType();
                $window.location.reload();
            }

            function blobLink(entity, name) {
                return 'api/dbs/' + db.uuid + '/entities/' + entity.id + "/blob/" + name.name;
            }

            function blobStringLink(entity, name) {
                return 'api/dbs/' + db.uuid + '/entities/' + entity.id + "/blobString/" + name.name;
            }


            function downloadBlob(entity, blob) {
                return $http.get(blobLink(entity, blob)).then(function(response) {
                    var file = new Blob([response.data], { type: 'application/octet.stream' });
                    return FileSaver.saveAs(file, blob.name);
                });
            }

            function downloadBlobString(entity, blob) {
                return $http.get(blobStringLink(entity, blob)).then(function(response) {
                    var file = new Blob([response.data], { type: 'application/octet.stream' });
                    return FileSaver.saveAs(file, blob.name);
                });
            }

            return {
                toType: toType,
                toEntity: toEntity,
                forceReload: forceReload,
                downloadBlob: downloadBlob,
                downloadBlobString: downloadBlobString,
            };
        };
    }]
);
