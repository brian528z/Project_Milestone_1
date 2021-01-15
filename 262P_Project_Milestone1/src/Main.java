import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.json.XML;

/*
 * Tae Hyun Lee
 * Project Milestone 1
 * 
 * INSTRUCTIONS: 
 * 
 * REQUIRED ARGUMENTS : 
 * args[0] = path of xml file. ex) "src/test.xml"
 * args[1] = choice of method to execute. (1~5)
 * args[2] = specific key path/key thats is optional to certain methods (2 and 3)
 */

public class Main {
	public static void main(String[] args) throws IOException {
		
		String link = "";
		int choice = 0;
		String line = "";
		String input = "";
		
		
		if(args.length < 2) {
			System.err.println("INVALID INPUT");
		}
		else {
			link = args[0];
			choice = Integer.parseInt(args[1]);
		}
		
		//Read input file from command line
		File file = new File(link);
		
		if(!file.exists()) {
			System.err.println("ERROR : FILE NOT FOUND");
		}
		else {
			Scanner s = new Scanner(file);
			
			while (s.hasNextLine()) {
				line = s.nextLine();
				input += line;
			}
			s.close();
			
			/*
			 * 1. read xml to json object
			 * output : output.json
			 */
			if (choice == 1) {
				JSONObject json = XML.toJSONObject(input);
				
				FileWriter output = new FileWriter("src/output.json");
				output.write(json.toString(4));
				output.close();
				System.out.println("RESULT : output.json");
			}
			/*
			 * 2. extract sub-object with certain path as argument using pointer
			 * output : output2.json
			 */
			else if (choice == 2) {
				if(args.length == 3) {
					String query = args[2];
					
					JSONObject json = XML.toJSONObject(input);
					JSONPointer pointer = new JSONPointer(query);
					
					JSONObject sub = (JSONObject) pointer.queryFrom(json);
					FileWriter output2 = new FileWriter("src/output2.json");
					output2.write(sub.toString(4));
					output2.close();
					System.out.println("RESULT : output2.json");
				}
				else {
					System.err.println("ERROR : INPUT QUERY STRING REQUIRED");
				}
			}
			/*
			 * 3. check if key path exists; if exists-> write sub-object to disk. if not -> blank output
			 * output : output3.json
			 */
			else if (choice == 3) {
				if(args.length ==3) {
					String key = args[2];
					JSONObject json = XML.toJSONObject(input);
					
					if(checkKey(json, key)) {
						System.out.println(key + " FOUND");
						System.out.println("RESULT : output3.json");
					}
					else {
						System.out.println(key + " NOT FOUND");
					}
				}
				else {
					System.err.println("ERROR : INPUT QUERY STRING REQUIRED");
				}
			}
			/*
			 * 4. add prefix "sw262_" to all keys
			 * output : output4.json
			 */
			else if(choice==4) {
				String addToKey = "swe262_";
				
				JSONObject json = XML.toJSONObject(input);
				JSONObject newObject = addPrefix(json, addToKey);
				
				FileWriter output4 = new FileWriter("src/output4.json");
				output4.write(newObject.toString(4));
				output4.close();
				System.out.println("RESULT : output4.json");
				
			}
			/*
			 * 5. replace sub-object with another JSON object
			 * object to replace and replacing object can be edited in code.
			 * customObj = new object
			 * customKeyPath = object path to replace
			 * 
			 * does not require input command line.
			 * output : output5.json
			 */
			else if(choice==5) {
				JSONObject json = XML.toJSONObject(input);
				
				String customObj = "{\"name\":\"test_name\",\"test\":\"working\",\"age\":\"1234\"}";
				String customKeyPath = "/catalog/book/1";
				JSONPointer p = new JSONPointer(customKeyPath);
				
				JSONObject s1 = (JSONObject) p.queryFrom(json);
				
				String orig = json.toString();
				String erase = s1.toString();
				
				String finalresult = orig.replace(erase, customObj);
				
				json = new JSONObject(finalresult);
				
				FileWriter output5 = new FileWriter("src/output5.json");
				output5.write(json.toString(4));
				output5.close();
				System.out.println("RESULT : output5.json");
			}
			else {
				System.err.println("ERROR : INVALID CHOICE:");
			}
		}
	}
	
	
	//method to iterate through json to find if key exists in nested arrays/objects
	//writes the corresponding value of the key that is found to output3.json file
	public static boolean checkKey(JSONObject json, String key) throws IOException {
		Iterator<String> it = json.keys();
		while(it.hasNext()) {
			String temp = (String) it.next();
			boolean found = json.has(key);
			
			if (found) {
				FileWriter output3 = new FileWriter("src/output3.json");
				
				if(json.optJSONArray(key) != null) {
					output3.write(json.getJSONArray(key).toString(4));
				}
				else if(json.optJSONObject(key) != null) {
					output3.write(json.getJSONObject(key).toString(4));
				}
				output3.close();
				return true;
			}
			if(json.optJSONArray(temp) != null) {
				JSONArray ja = json.getJSONArray(temp);
				for (int i = 0; i < ja.length();i++) {
					return checkKey(ja.getJSONObject(i), key);
				}
			}
			if(json.optJSONObject(temp) != null) {
				return checkKey(json.getJSONObject(temp), key);
			}
		}
		return false;
	}
	
	//method to iterate through all keys (nested and arrays included) to add the prefix to all the keys
	public static JSONObject addPrefix(JSONObject json, String key) {
		Iterator<String> it2 = json.keys();
		ArrayList<String> keyList = new ArrayList<String>();
		
		while (it2.hasNext()) {
			keyList.add(it2.next().toString());
		}
		
		for (int k = 0; k < keyList.size(); k++) {
			
			String temp = keyList.get(k);
			json.put(key + temp,  json.get(temp));
			json.remove(temp);
			
			if(json.optJSONArray(key + temp) != null) {
				JSONArray ja = json.getJSONArray(key + temp);
				for (int i = 0; i < ja.length(); i++) {
					JSONObject sub = new JSONObject(json.toString().replace(ja.getJSONObject(i).toString(), "\"\""));
					JSONObject arr = new JSONObject(sub.toString().replace("\"\"",addPrefix(ja.getJSONObject(i), key).toString()));
					json = arr;
				}
			}
			if(json.optJSONObject(key + temp ) != null) {
				JSONObject sub = new JSONObject(json.toString().replace(json.getJSONObject(key + temp).toString(), "\"\""));
				
				return new JSONObject(sub.toString().replace("\"\"", addPrefix(json.getJSONObject(key + temp), key).toString()));
			}
		}
		return json;
		
	}
}
