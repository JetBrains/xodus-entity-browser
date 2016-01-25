angular.module('xodus').service('EntityTypeService', [
    '$http',
    '$q',
    '$location',
    function ($http, $q, $location) {
        var types = null;
        this.all = all;
        this.byId = byId;
        this.search = search;
        this.remove = remove;

        function all() {
            if (types) {
                return $q.when(types);
            } else {
                return $http.get('api/types').then(function (data) {
                    types = data.data;
                    if (angular.isArray(types) && types.length) {
                        return types;
                    }
                    $location.path('/empty-store');
                });
            }
        }

        function byId(id) {
            return all().then(function (data) {
                var type = null;
                angular.forEach(data, function (item) {
                    if (item.id == id) {
                        type = item;
                    }
                });
                return type;
            });
        }

        function search(typeId, term, offset, pageSize) {
            return $http.get('api/type/' + typeId + '/entities', {
                params: {
                    q: term,
                    offset: offset,
                    pageSize: (pageSize ? pageSize : 50)
                }
            }).then(function (response) {
                return response.data;
            });
        }

        function remove(typeId, entityId) {
            return $http['delete']('api/type/' + typeId + '/entity/' + entityId).then(function (response) {
                return response.data;
            });
        }
    }]);
