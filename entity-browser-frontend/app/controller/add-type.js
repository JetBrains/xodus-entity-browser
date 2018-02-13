angular.module('xodus').controller('AddTypeController', [
    'currentDatabase',
    'EntityTypeService',
    'alert',
    '$route',
    function (currentDatabase, types, alert, $route) {
        var addTypeCtrl = this;
        addTypeCtrl.newTypeName = null;
        addTypeCtrl.error = null;

        addTypeCtrl.addNewEntityType = function () {
            addTypeCtrl.error = null;
            if (addTypeCtrl.newTypeName) {
                types.newEntityType(currentDatabase.get(), addTypeCtrl.newTypeName)
                    .catch(alert.showHttpError)
                    .then(function (newTypes) {
                        alert.success('Entity type ' + addTypeCtrl.newTypeName + ' created');
                        currentDatabase.get().types = newTypes;
                        $route.reload();
                    });
            } else {
                addTypeCtrl.error = '\u2718 - required field';
            }
        };
    }]);