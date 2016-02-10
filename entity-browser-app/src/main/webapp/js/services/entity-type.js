angular.module('xodus').service('EntityTypeService', [
    '$http',
    '$q',
    '$location',
    'DatabaseService',
    function ($http, $q, $location, database) {
        this.all = all;
        this.byId = byId;
        this.search = search;

        function all() {
            return database.getSummary().then(function (data) {
                return data.types;
            });
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

    }]);
