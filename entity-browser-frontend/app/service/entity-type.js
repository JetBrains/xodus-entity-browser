angular.module('xodus').service('EntityTypeService', [
    '$http',
    '$q',
    '$location',
    'DatabaseService',
    function ($http, $q, $location, database) {
        this.all = all;
        this.byId = byId;
        this.search = search;
        this.bulkDelete = bulkDelete;

        function all() {
            return database.loadedAppState.current.types;
        }

        function byId(id) {
            var type = null;§
            angular.forEach(all(), function (item) {
                if (item.id == id) {
                    type = item;
                }
            });
            return type;
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

        function bulkDelete(typeId, term) {
            return $http.delete('api/jobs/type/' + typeId + '/entities', {
                params: {
                    q: term
                }
            }).then(function (response) {
                return response.data;
            });
        }

    }]);
