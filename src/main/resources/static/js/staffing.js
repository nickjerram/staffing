var staffingApp = angular.module('staffing', ['ngSanitize']);

staffingApp.controller('FormController', function($scope, $http) {

    $scope.preferences = ["Yes","DontMind","No"];

    $scope.confirmEmail = function() {
        var result = $scope.form.email!=$scope.form.confirmEmail;
        console.log('$scope.form.email '+$scope.form.email);
        console.log('$scope.form.confirmEmail '+$scope.form.confirmEmail);
        return result;
    }

    $scope.areasValid = function() {
        var result = $scope.form.areas.some(function(area) {
            return !(area.preference==='No');
        });

        return result;
    }

    $scope.areaSelect = function() {
        $scope.form.areasValid = $scope.areasValid();
    }

    $scope.submitForm = function() {

        $http({
            method: 'POST',
            url: '/form/submit',
            data: JSON.stringify($scope.form),
            headers : { 'Content-Type': 'application/json' }
        }).then(function successCallback(response) {
            $scope.form = response.data;
            $scope.response = response.data.response;
        }, function errorCallback(data) {
            console.log(data.data);
        });
    }
    $http.get('/form/json').
        then(function(response) {
            $scope.form = response.data;
            $scope.response = response.data.response;
        });

});
