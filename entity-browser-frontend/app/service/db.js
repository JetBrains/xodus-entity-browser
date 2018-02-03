angular.module('xodus')
    .service('databaseService', [
        '$http',
        '$q',
        'alert',
        function ($http, $q, alert) {
            var service = this;
            service.getDatabases = getDatabases;
            service.getTypes = getTypes;
            service.update = update;
            service.deleteDB = deleteDB;
            service.startOrStop = startOrStop;
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

            function startOrStop(db, isStart) {
                return $http.post('/api/dbs/' + db.uuid, db, {
                    params: {
                        op: isStart ? "start" : "stop"
                    }
                }).then(function (response) {
                    var msg = isStart ? 'started' : 'stopped';
                    if (isStart !== response.data.opened) {
                        alert.error('Database is not ' + msg);
                    } else {
                        alert.success('Database is ' + msg);
                    }
                    var oldDb = service.databases.find(function (oldDb) {
                        return oldDb.uuid === db.uuid;
                    });
                    angular.extend(oldDb, response.data);
                    return response;
                }, function () {
                    var msg = isStart ? 'started' : 'stopped';
                    alert.error('Database is not ' + msg);
                }).catch(alert.showHttpError);
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

