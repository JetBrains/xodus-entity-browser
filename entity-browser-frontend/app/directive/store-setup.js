angular.module('xodus').directive('storeSetup', ['$http', 'navigationService', 'databaseService',
    function ($http, navigationService, databaseService) {
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
                scope.error = null;
                scope.changeDB = function () {
                    if (scope.database.$valid) {
                        databaseService.update(scope.db).then(function (data) {
                            var onSuccess = scope.onSuccess();
                            if (onSuccess) {
                                onSuccess();
                            }
                            navigationService(data).toType();
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
