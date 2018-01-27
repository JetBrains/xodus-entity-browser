angular.module('xodus')
    .service('databaseService', [
        '$http',
        '$q',
        '$route',
        function ($http, $q, $route) {
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
                return $http.post('/api/dbs', db).then(function (newDB) {
                    service.databases.push(newDB);
                    return newDB;
                });
            }

            function getTypes(db) {
                return $http.get('/api/dbs/' + db.uuid + '/types').then(function (response) {
                    return response.data;
                });
            }

            function deleteDB(db) {
                return $http['delete']('/api/dbs/' + db.uuid).then(function (data) {
                    var index = service.databases.indexOf(db);
                    if (index > -1) {
                        service.databases.splice(index, 1);
                    }
                    return data;
                });
            }

        }]
    );

