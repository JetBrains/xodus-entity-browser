angular.module('xodus').service('NavigationService', [
    '$location',
    function ($location) {
        this.toType = toType;
        this.toEntity = toEntity;
        this.api = {
            blobLink: function (entity, name) {
                return 'api/type/' + entity.typeId + '/entity/' + entity.id + "/blob/" + name;
            }
        };

        function toType(typeId) {
            return typeId ? $location.path('/type/' + typeId) : toType(0);
        }

        function toEntity(typeId, entityId) {
            $location.path('/type/' + typeId + (entityId ? '/entity/' + entityId : '/new'));
        }

    }]
);
