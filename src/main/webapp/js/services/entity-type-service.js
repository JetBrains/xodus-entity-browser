angular.module('xodus').service('EntityTypeService', ['$http', '$q', function($http, $q) {
    var types = null;
    this.all = all;
    this.byId = byId;
    this.search = search;

    function all() {
        if (types) {
            return $q.when(types);
        } else {
            return $http.get('api/types').then(function(data) {
                types = data.data;
                return types;
            });
        }
    }

    function byId(id) {
        return all().then(function(data) {
            var type = null;
            angular.forEach(data, function(item) {
                if (item.id == id) {
                    type = item;
                }
            });
            return type;
        });
    }

    function search(typeId, term, offset) {
        return $http.get('api/type/' + typeId + '/entities', {
            params: {
                q: term,
                offset: offset,
                pageSize: 50
            }
        }).then(function(response) {
            return response.data;
        });
    }

}]);
