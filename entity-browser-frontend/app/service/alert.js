angular.module('xodus')
    .service('alert', [
        '$rootScope',
        '$q',
        function ($rootScope, $q) {
            var alert = this;
            alert.warning = showMessage('warn');
            alert.success = showMessage('info');
            alert.error = showMessage('danger');

            alert.showHttpError = function (response) {
                if ((response.data || {}).errorMessage) {
                    alert.error(response.data.errorMessage);
                } else {
                    alert.error('Server respond with: ' + response.status + ' - ' + response.statusText);
                }
                return $q.reject(response);
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