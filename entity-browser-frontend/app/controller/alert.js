angular.module('xodus')
    .controller('AlertCtrl', [
        '$scope',
        '$timeout',
        function ($scope, $timeout) {
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
                if (args.timeout) {
                    $timeout(alertCtrl.close, 3000);
                }
            });
        }
    ]);