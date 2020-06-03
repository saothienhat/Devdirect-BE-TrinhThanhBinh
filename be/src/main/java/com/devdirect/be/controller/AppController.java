package com.devdirect.be.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

/**
 * @author Trinh Thanh Binh
 *
 */
@RestController
@RequestMapping("/api/app")
public class AppController {
	
	@PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String analyzeApis(@RequestParam(name = "apis") String apis) {
		final String TAG = "/";
		JsonObject result = new JsonObject();
		List<String> inputData = new ArrayList<String>();
		if(apis != null && !apis.isEmpty()) {
			String[] split = apis.replaceAll("\"", "").replaceAll("[\\[\\]]", "").split(",");
			inputData = Arrays.asList(split);
		}
		
		// Map<project, Map<them, TreeMap<method, _count>>>
		Map<String, Map<String, HashMap<String, Integer>>> map = new HashMap<String, Map<String, HashMap<String, Integer>>>();
		for (String api : inputData) {
			String[] apiArr = api.split(TAG);
			if (apiArr != null && apiArr.length != 3) {
				result.addProperty("result", "");
				return result.toString();
			}

			String project = apiArr[0];
			String theme = apiArr[1];
			String method = apiArr[2];

			if (!map.containsKey(project))
				map.put(project, new HashMap<String, HashMap<String, Integer>>());
			Map<String, HashMap<String, Integer>> childMap = map.get(project);
			if (!childMap.containsKey(theme))
				childMap.put(theme, new HashMap<String, Integer>());
			Integer count = childMap.get(theme).get(method);
			childMap.get(theme).put(method, (count == null) ? 1 : count + 1);
		}
		
		final String KEY_COUNT = "_count";
		JsonObject respond = new JsonObject();
		for (Map.Entry<String, Map<String, HashMap<String, Integer>>> entry : map.entrySet()) {
			JsonObject projectJson = new JsonObject();
			int projectCount = 0;
			String projectKey = entry.getKey();
			Map<String, HashMap<String, Integer>> projectValue = entry.getValue();
			
			for (Map.Entry<String, HashMap<String, Integer>> themeEntry : projectValue.entrySet()) {
				int themeCount = 0;
				JsonObject themeJson = new JsonObject();
				String themeKey = themeEntry.getKey();
				HashMap<String, Integer> themeValue = themeEntry.getValue();
				
				for (Map.Entry<String, Integer> methodEntry : themeValue.entrySet()) {
					String methodKey = methodEntry.getKey();
					Integer methodCount = methodEntry.getValue();
					themeJson.addProperty(methodKey, methodCount);
					themeCount += methodCount.intValue();
				}
				
				themeJson.addProperty(KEY_COUNT, themeCount);
				projectJson.add(themeKey, themeJson);
				projectCount += themeCount;
			}

			projectJson.addProperty(KEY_COUNT, projectCount);			
			respond.add(projectKey, projectJson);
		}
		
		result.add("result", respond);
		return result.toString();
	}
	

}
