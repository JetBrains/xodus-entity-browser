angular.module('xodus').service('EntityTypeService', [
    '$http',
    function ($http) {
        this.search = search;
        this.bulkDelete = bulkDelete;
        this.newEntityType = newEntityType;

        function search(db, typeId, term, offset, pageSize) {
            return $http.get('api/dbs/' + db.uuid + '/entities', {
                params: {
                    id: typeId,
                    q: term,
                    offset: offset,
                    pageSize: (pageSize ? pageSize : 50)
                }
            }).then(function (response) {
                return response.data;
            });
        }

        function bulkDelete(db, typeId, term) {
            return $http.delete('api/dbs/' + db.uuid + '/entities', {
                params: {
                    q: term,
                    id: typeId
                }
            });
        }

        function newEntityType(db, typeName) {
            return $http.post('api/dbs/' + db.uuid + '/types', {
                name: typeName
            }).then(function (response) {
                return response.data;
            });
        }

    }]);
