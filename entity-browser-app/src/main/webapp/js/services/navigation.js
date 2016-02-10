angular.module('xodus').service('NavigationService', [
    '$location',
    '$window',
    function ($location, $window) {
        this.toType = toType;
        this.toEntity = toEntity;
        this.forceReload = forceReload;
        this.api = {
            blobLink: function (entity, name) {
                return 'api/type/' + entity.typeId + '/entity/' + entity.id + "/blob/" + name;
            }
        };

        function toType(typeId) {
            return angular.isDefined(typeId) ? $location.path('/type/' + typeId) : toType(0);
        }

        function toEntity(typeId, entityId) {
            $location.path('/type/' + typeId + (angular.isDefined(entityId) ? '/entity/' + entityId : '/new'));
        }
        function forceReload() {
            toType();
            $window.location.reload();
        }

    }]
);
