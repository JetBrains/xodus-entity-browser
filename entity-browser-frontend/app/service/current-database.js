angular.module('xodus')
    .service('currentDatabase', [
        function () {
            var db = null;
            var currentDatabase = this;

            currentDatabase.get = function () {
                return db;
            };

            currentDatabase.set = function (tagetDB) {
                db = tagetDB;
            };
        }]);