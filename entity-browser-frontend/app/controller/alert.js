angular.module('xodus')
    .controller('AlertCtrl', [
        '$scope',
        '$timeout',
        '$window',
        function ($scope, $timeout, $window) {
            var alertCtrl = this;
            alertCtrl.closed = true;
            alertCtrl.type = 'error';
            alertCtrl.message = 'error';
            alertCtrl.close = function () {
                alertCtrl.closed = true;
            };
            $scope.$on('show-message', function (event, args) {
                alertCtrl.type = args.type;
                alertCtrl.message = args.message;
                alertCtrl.closed = false;
                alertCtrl.top = $window.scrollY + 20;
                if (args.timeout) {
                    $timeout(alertCtrl.close, 3000);
                }
            });
        }
    ]);