angular.module('xodus')
    .service('databaseService', [
        '$http',
        '$q',
        '$location',
        'navigationService',
        function ($http, $q, $location, navigation) {
            var service = this;
            service.getDatabases = getDatabases;
            service.getTypes = getTypes;
            service.update = update;
            service.deleteDB = deleteDB;
            service.databases = null;

            function getDatabases() {
                var hubKey = 'jetPassServerDb';
                var youtrackKey = 'teamsysstore';

                if (service.databases) {
                    return $q.when(service.databases);
                }
                return $http.get('api/dbs').then(function (data) {
                    service.databases = data.data.map(function (db) {
                        if (db.key === hubKey) {
                            db.description = 'HUB';
                        } else if (db.key === youtrackKey) {
                            db.description = 'YouTrack';
                        } else {
                            db.description = db.key;
                        }
                        return db;
                    });
                    return service.databases;
                });
            }

            function update(db) {
                return $http.post('/api/dbs', db).then(function (data) {
                    navigation.forceReload();
                    return data;
                });
            }

            function getTypes(db) {
                return $http.get('/api/dbs/' + db.uuid + '/types').then(function (response) {
                    return response.data;
                });
            }

            function deleteDB(db) {
                return $http['delete']('/api/dbs', {data: db}).then(function (data) {
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

