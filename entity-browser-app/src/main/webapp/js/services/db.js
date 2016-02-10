angular.module('xodus').service('DatabaseService', [
    '$http',
    '$q',
    '$location',
    'NavigationService',
    function ($http, $q, $location, navigation) {
        var summary = null;
        this.getSummary = getSummary;
        this.update = update;

        function getSummary() {
            if (summary) {
                return $q.when(summary);
            }
            return $http.get('api/db').then(function (data) {
                summary = data.data;
                if (angular.isObject(summary)) {
                    return summary;
                }
                if (!angular.isArray(summary.types) || summary.types.length) {
                    $location.path('/empty-store');
                    return $q.reject('empty store');
                }
                if (!summary.location || !summary.key) {
                    $location.path('/setup');
                    return $q.reject('setup database first');
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

    }]
);

