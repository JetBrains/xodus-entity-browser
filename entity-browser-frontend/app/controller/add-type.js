angular.module('xodus').controller('AddTypeController', [
    'currentDatabase',
    'EntityTypeService',
    'alert',
    '$route',
    function (currentDatabase, types, alert, $route) {
        var addTypeCtrl = this;
        addTypeCtrl.newTypeName = '';

        addTypeCtrl.addNewEntityType = function () {
            types.newEntityType(currentDatabase.get(), addTypeCtrl.newTypeName)
                .catch(alert.showHttpError)
                .then(function (newTypes) {
                    alert.success('Entity type ' + addTypeCtrl.newTypeName + ' created');
                    currentDatabase.get().types = newTypes;
                    $route.reload();
                });
        };

    }]);