angular.module('xodus')
    .service('alert', [
        '$rootScope',
        function ($rootScope) {
            var alert = this;
            alert.warning = showMessage('warn');
            alert.success = showMessage('info');
            alert.error = showMessage('danger');

            alert.showHttpError = function (data) {
                var message = data;
                alert.error(message);
            };

            function showMessage(type) {
                return function (message) {
                    $rootScope.$broadcast('show-message', {
                        type: type,
                        message: message,
                        timeout: true
                    });
                }
            }

        }]);