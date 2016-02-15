angular.module('xodus').service('DatabaseService', [
    '$http',
    '$q',
    '$location',
    'NavigationService',
    function ($http, $q, $location, navigation) {
        var summary = null;
        this.getSummary = getSummary;
        this.update = update;
        this.deleteDB = deleteDB;

        function getSummary() {
            if (summary) {
                return $q.when(summary);
            }
            return $http.get('api/db').then(function (data) {
                summary = data.data;
                if (!summary.location || !summary.key) {
                    $location.path('/setup');
                    return $q.reject('setup database first');
                }
                if (!angular.isArray(summary.types) || !summary.types.length) {
                    $location.path('/empty-store');
                    return $q.reject('empty store');
                }
                if (angular.isObject(summary)) {
                    return $q.when(summary);
                }
                return $q.reject('something strange with server');
            });
        }

        function update(db) {
            return $http.post('/api/db', db).then(function (data) {
                navigation.forceReload();
                return data;
            });
        }

        function deleteDB(db) {
            return $http['delete']('/api/db', {data: db, headers: {"Content-Type": "application/json;charset=utf-8"}}).then(function (data) {
                var recent = summary.recent;
                var index = recent.indexOf(db);
                if (index > -1) {
                    recent.splice(index, 1);
                }
                return data;
            });
        }

    }]
);

