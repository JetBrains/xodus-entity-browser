angular.module('xodus').service('EntityTypesService', ['$http', '$q', function($http, $q) {
    var types = null;
    var promise = $http.get('api/types').then(function(data) {
        types = data.data;
    });
    return {
        types: promise,
        all: function() {
            return types;
        }
    };
    //
    //
    //this.all = all;
    //this.byId = byId;
    //
    //function all() {
    //    if (types) {
    //        return $q.when(types);
    //    } else {
    //        return $http.get('api/types').then(function(data) {
    //            types = data.data;
    //            return types;
    //        });
    //    }
    //}
    //
    //function byId(id) {
    //    return all().then(function(data) {
    //        var type = null;
    //        angular.forEach(data, function(item) {
    //            if (item.id == id) {
    //                type = item;
    //            }
    //        });
    //        return type;
    //    });
    //}

}]);
