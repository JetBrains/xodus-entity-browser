angular.module('xodus').service('DatabaseService', [
    '$http',
    '$q',
    '$location',
    'NavigationService',
    function ($http, $q, $location, navigation) {
        var service = this;
        service.getAppState = getAppState;
        service.update = update;
        service.deleteDB = deleteDB;
        service.loadedAppState = null;


        function getAppState() {
            if (service.loadedAppState) {
                return $q.when(service.loadedAppState);
            }
            return $http.get('api/db').then(function (data) {
                service.loadedAppState = data.data;
                return service.loadedAppState;
            });
        }

        function update(db) {
            return $http.post('/api/db', db).then(function (data) {
                navigation.forceReload();
                return data;
            });
        }

        function deleteDB(db) {
            return $http['delete']('/api/db', {data: db}).then(function (data) {
                var recent = service.loadedAppState.recent;
                var index = recent.indexOf(db);
                if (index > -1) {
                    recent.splice(index, 1);
                }
                return data;
            });
        }

    }]
);

