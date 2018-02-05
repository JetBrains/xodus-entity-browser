angular.module('xodus')
    .service('alert', [
        '$rootScope',
        function ($rootScope) {
            var alert = this;
            alert.warning = showMessage('warn');
            alert.success = showMessage('info');
            alert.error = showMessage('danger');

            alert.showHttpError = function (data) {
                alert.error('Server respond with: ' + data.status + ' - ' + data.statusText);
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