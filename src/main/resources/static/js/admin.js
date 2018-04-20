var adminApp = angular.module('admin', []);

adminApp.controller('AdminController', function($scope, $http, $window) {

    $scope.form = {};

    $scope.submitForm = function() {

        $http({
            method: 'POST',
            url: '/login',
            data: JSON.stringify($scope.form),
            headers : { 'Content-Type': 'application/json' }
        }).then(function successCallback(response) {
            $scope.form = response.data;
            if (response.data.success) {
                var landingUrl = $window.location.host + "/admin";               
                landingUrl = 'https://'+landingUrl;
                console.log("URL "+landingUrl);
                $window.location.href = landingUrl;
            } else {
            }
        }, function errorCallback(data) {
            console.log(data.data);
        });
    }
});
