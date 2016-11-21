angular.module('xodus').directive('storeSetup', ['$http', 'NavigationService', 'DatabaseService',
    function ($http, navigation, databases) {
        return {
            restrict: 'E',
            scope: {
                showButtons: '=',
                onSuccess: '&',
                location: '=',
                key: '='
            },
            replace: true,
            template: require('../templates/store-setup.html'),
            link: function (scope) {
                scope.db = {
                    location: angular.isDefined(scope.location) ? scope.location : null,
                    key: angular.isDefined(scope.key) ? scope.key : null
                };
                if (!scope.db.location || !scope.db.key) {
                    scope.db = databases.loadedAppState.current;
                }
                scope.error = null;
                scope.changeDB = function () {
                    if (scope.database.$valid) {
                        databases.update(scope.db).then(function (data) {
                            scope.db.location = data.location;
                            scope.db.key = data.key;
                            var onSuccess = scope.onSuccess();
                            if (onSuccess) {
                                onSuccess();
                            }
                            navigation.toType();
                        }, function () {
                            scope.error = 'Error while changing database. Changes not applied.';
                        });
                    }
                };
                scope.closeError = function () {
                    scope.error = null;
                };
                scope.$on('applyDBChange', scope.changeDB);

                var hubKey = 'jetPassServerDb';
                var youtrackKey = 'teamsysstore';

                scope.predefinedKeys = [
                    {name: 'Hub', key: hubKey},
                    {name: 'YouTrack', key: youtrackKey}
                ];
                scope.applyKey = function (item) {
                    scope.db.key = item.key;
                };

            }
        };
    }]
);
