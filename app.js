var express = require('express');
var app = express();
var url = require('url');

var bodyParser = require('body-parser'); 
var Client = require('node-rest-client').Client;
var client = new Client();

app.use(express.static(__dirname + "/public"));
app.use(bodyParser.json());

app.get('/palindromes', function (req, res){  
	console.log('The GET Method of palindromes Arrived');

	var url_parts = url.parse(req.url, true);
	var query = url_parts.query;
	console.log("query.search:" + query.search);
	/*if(query.search == null){
		var errMessage = {information:"Missing the search content"};
		res.json(errMessage);
	}*/
	var targeturl = 'https://api.nasa.gov/patents/content?query=' + req.query.search;

	if(query.limit != null){
		targeturl += '&limit=' + req.query.limit + '&api_key=DEMO_KEY';
	} else {
		targeturl += '&limit=' + 1 + '&api_key=DEMO_KEY';
	}
	
	console.log("targeturl:-- "+ targeturl);
    	                                                   //temperature
	/*client.get("https://api.nasa.gov/patents/content?query=electricity&limit=1&api_key=DEMO_KEY", function(data, response){*/
	client.get(targeturl, function(data, response){

		var receivedNum = data.results.length;
		var totalInnovate = 0;
		for (var i = 0; i < receivedNum; i++){
			totalInnovate += data.results[i].innovator.length;
		}
		var nameList = [ ];
		for (var i = 0; i < receivedNum; i++){//get all the names from the return data
			for (var j = 0; j < data.results[i].innovator.length; j++){
				var innovator = {fname: data.results[i].innovator[j].fname, lname : data.results[i].innovator[j].lname, fullname: data.results[i].innovator[j].fname +  " " + data.results[i].innovator[j].lname};
				nameList.push(innovator);
			}
		}

		var rst = []; //get final result from nameList with function of getPdmcount
		for (var i = 0; i < nameList.length; i++){
			var item = {fullname: nameList[i].fullname, count: getPdmcount(nameList[i].fname, nameList[i].lname)};
			rst.push(item);
		}
		res.json(rst);
	});
});


function getPdmcount(firstName, lastName){
	var firstNameL = firstName.toLowerCase();
	var lastNameL = lastName.toLowerCase();
	var pname = firstNameL + lastNameL;
	var uniqueCount = getUniqueCount(pname);//get the number of distinct character in the FullName
	return getPdmcountByMath(uniqueCount, pname.length);
}

function getPdmcountByMath(uc, tc){//get Palindrome number   uc-#uniqueCharacter  tc-#totalCharacter
	var rst = 1;
	for (var i = 0; i < ((tc % 2 == 0) ? tc/2 : Math.ceil(tc / 2) ); i++){
		rst *= uc;
	}
	return rst;
}

function getUniqueCount(str) {
	var set = new Set(str);
	var charArr = str.split("");
	for (var i = 0; i < charArr.length; i++){
		set.add(charArr[i]);
	}
	return set.size;
}

app.listen(4000, function(){
	console.log("Server running on port 4000");
});
