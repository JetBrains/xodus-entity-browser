angular.module('xodus')
    .service('databaseService', [
        '$http',
        '$q',
        'alert',
        function ($http, $q, alert) {
            var service = this;
            service.getDatabases = getDatabases;
            service.getTypes = getTypes;
            service.add = add;
            service.deleteDB = deleteDB;
            service.startOrStop = startOrStop;
            service.databases = null;
            service.readonly = null;

            function getDatabases() {
                var hubKey = 'jetPassServerDb';
                var youtrackKey = 'teamsysstore';

                if (service.databases) {
                    return $q.when(service.databases);
                }
                return $http.get('api/dbs').then(function (data) {
                    service.readonly = data.data.readonly;
                    service.databases = data.data.dbs.map(function (db) {
                        db.readonly = db.readonly || service.readonly;

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

            function add(db) {
                return $http.post('api/dbs', db).then(function (response) {
                    service.databases.push(response.data);
                    return response.data;
                }).catch(alert.showHttpError);
            }

            function startOrStop(db) {
                var isStart = db.opened;
                return $http.post('api/dbs/' + db.uuid, db, {
                    params: {
                        op: isStart ? "start" : "stop"
                    }
                }).then(function (response) {
                    var msg = isStart ? 'started' : 'stopped';
                    if (isStart !== response.data.opened) {
                        alert.error('Database cannot be ' + msg);
                    } else {
                        alert.success('Database is ' + msg);
                    }
                    var oldDb = service.databases.find(function (oldDb) {
                        return oldDb.uuid === db.uuid;
                    });
                    angular.extend(oldDb, response.data);
                    return response;
                }).catch(function (response) {
                    alert.showHttpError(response);
                    db.opened = !db.opened; // revert model
                });
            }

            function getTypes(db) {
                return $http.get('api/dbs/' + db.uuid + '/types').then(function (response) {
                    return response.data;
                });
            }

            function deleteDB(db) {
                return $http['delete']('api/dbs/' + db.uuid).then(function (data) {
                    var index = service.databases.indexOf(db);
                    if (index > -1) {
                        service.databases.splice(index, 1);
                    }
                    return data;
                });
            }

        }]
    );

