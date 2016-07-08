package lxd;

import java.io.*;
import java.util.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

 
//import org.springframework.web.bind.annotation.RequestParam;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
 
@Path("/")
public class RESTService {
  
	//1 building     2  consuming  3 combine together
	@GET
	@Path("/palindromes") 
	@Produces(MediaType.APPLICATION_JSON) 
	public Response getPalindromes(@QueryParam("search") String search, @QueryParam("limit") String limit)  {
		 
		if (search == null || search.equals("")){
			return Response.status(200).entity("Failed: the request parameter of search is missing").build();
		}
		String url = "https://api.nasa.gov/patents/content?query=" + search;
		
		String defaultLimit = "1";
		if (limit != null && !limit.equals("")){
			int numLimit = Integer.valueOf(limit);
			if (numLimit <= 0 || numLimit > 5){
				return Response.status(200).entity("Failed: the request parameter of limit should between 1 and 5").build();
			}
			url += "&limit=" + limit;
		} else {
			url += "&limit="+ defaultLimit;
		}
		
		url += "&api_key=DEMO_KEY";
		
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		
		ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
		if (response.getStatus() != 200){
			throw new RuntimeException("Failed: HTTP error code : " + response.getStatus());
		}
		
		String returnedData = response.getEntity(String.class);
	 
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(returnedData);
		} catch (ParseException e) {
			System.out.println("JSONParser get Exception");
			e.printStackTrace();
		}
		
		if (obj == null) {
			throw new RuntimeException("JSONParser get Exception");
		}
		JSONObject jobj = (JSONObject) obj;
		if (jobj instanceof JSONObject) {
		} else {
			throw new RuntimeException("The return data is not JSONObject");
		}
		
		Object countObj = jobj.get("count");//get count element
		int count = 0;
		if (countObj != null){
			 count = ((Long) jobj.get("count")).intValue();
		}
		
		Object arrObj = jobj.get("results");//get results element
		JSONArray resultArray = null;
		if (arrObj != null){
			resultArray = (JSONArray) jobj.get("results");
		}
		
		List<String> innovatorsNames = null;//add all the name of innovators to innovatorsName
		if (count > 0 && resultArray != null && count == resultArray.size()){
			innovatorsNames = getAllIntsName(resultArray);
		} else {
			throw new RuntimeException("The number of count != size of resultArray");
		}
		
		//get all of the namePalindrom item into the List<namePalindrom>, and sort it by amount
		List<NamePalindrom> tempRst = null;
		if (innovatorsNames != null && innovatorsNames.size() != 0){
			tempRst = getInterResult(innovatorsNames);
		} else {
			throw new RuntimeException("The intermediary NamePalindrom Class get Exception ");   
		}
		
		JSONArray rst = new JSONArray();	//get final result from sorted tempRst
		if (tempRst != null){
			rst = getFinal(tempRst);
		} else {
			throw new RuntimeException("The final JSONArray get Exception");
		}
		
		return Response.status(200).entity(rst.toString()).build();
	}
	
	JSONArray getFinal(List<NamePalindrom> tempRst){//get final result from sorted tempRst
		JSONArray rst = new JSONArray();
		for (NamePalindrom nameP : tempRst){
			JSONObject oneRecord = new JSONObject();
			oneRecord.put("name", nameP.name);
			oneRecord.put("count", nameP.amount);
			rst.add(oneRecord);
		}
		return rst;
	}
	
	//get all of the namePalindrom item into the List<namePalindrom>, and sort it by amount
	List<NamePalindrom> getInterResult(List<String> innovatorsNames) {
		List<NamePalindrom> tempRst = new ArrayList<>();
		for (int i = 0; i < innovatorsNames.size(); i++) {
			String name = innovatorsNames.get(i);
			String[] fullName = name.split(" ");
			StringBuilder sb = new StringBuilder();
			for (String half : fullName) {
				sb.append(half);
			}
			String processingName = sb.toString().toLowerCase();
			tempRst.add(new NamePalindrom(innovatorsNames.get(i), getPalindromCount(processingName)));
		}

		Collections.sort(tempRst, new Comparator<NamePalindrom>() { // sorted by  the  amount
			public int compare(NamePalindrom a, NamePalindrom b) {
				return (int) (b.amount - a.amount);
			}
		});
		return tempRst;
	}
	
	List<String> getAllIntsName (JSONArray resultArray){//add all the name of innovators to innovatorsName
		List<String> innovatorsNames = new ArrayList<>();
		for (int i = 0; i < resultArray.size(); i++){ 
			JSONObject result = (JSONObject) resultArray.get(i);
			JSONArray innovatorsArr= (JSONArray) result.get("innovator");
			
			for (int j = 0; j < innovatorsArr.size(); j++){
				JSONObject author = (JSONObject) innovatorsArr.get(j);
				String lname = (String) author.get("lname");
				String fname = (String) author.get("fname");
				innovatorsNames.add(lname+ " " + fname);
			}
		}
		return innovatorsNames;
	}
	
	
	long getPalindromCount(String str){//calculate the count from a give name
		if (str == null || str.length() == 0){
			return 0;
		}
		long rst = 1;
		
		char[] cs = str.toCharArray();
		Set<Character> set = new HashSet<>();
		for(char c : cs){
			set.add(c);
		}
		int dist = set.size();
		int len = str.length();
		for (int i = 0; i < ((len % 2 == 0)? len /2 : len/2 + 1); i++){
			rst *= dist;
		}
		return rst;
	}
}
