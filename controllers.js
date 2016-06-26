var myApp = angular.module('myApp', []);

myApp.controller('AppCtrl', function($scope, $http){
	console.log("Message from Controller");

	$scope.name = "MEAN Stack";
 
	//$scope.searchContent = "electricity";// for the developmening

	$scope.searchOut = function(){
		console.log("searchOut is called");
 
		var url = '/palindromes?search='+  $scope.searchContent;
		                                              //temperature
		//"https://api.nasa.gov/patents/content?query=electricity&limit=1&api_key=DEMO_KEY"
		if ($scope.limit != null){
			console.log("limit---" + $scope.limit);
			url +='&limit=' + $scope.limit;
		} 
		
		console.log(url);
		$http.get(url).success(function(response){//can't be ''
			console.log("console get the data for the request method");
			$scope.returnData = response;
		});
	};
});
