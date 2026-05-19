package com.chandan.payments.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chandan.payments.service.RedisService;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
public class RedisController {


	private final RedisService redisService;


	// Add a value to a list
	@PostMapping("/list/{key}")
	public void addValueToList(@PathVariable String key, @RequestBody String value) {
		redisService.addValueToList(key, value);
	}


	// Get all values from a list
	@GetMapping("/list/{key}")
	public List<String> getAllValuesFromList(@PathVariable String key) {
		return redisService.getAllValuesFromList(key);
	}


	// Set a value in a hash
	@PostMapping("/hash/{hashName}/{key}")
	public void setValueInHash(@PathVariable String hashName, @PathVariable String key, @RequestBody String value) {
		redisService.setValueInHash(hashName, key, value);
	}


	// Get a value from a hash
	@GetMapping("/hash/{hashName}/{key}")
	public String getValueFromHash(@PathVariable String hashName, @PathVariable String key) {
		return redisService.getValueFromHash(hashName, key);
	}


	// Get all entries from a hash
	@GetMapping("/hash/{hashName}")
	public Map<String, String> getAllEntriesFromHash(@PathVariable String hashName) {
		return redisService.getAllEntriesFromHash(hashName);
	}


	// Set a value in Redis
	@PostMapping("/value/{key}")
	public void setValue(@PathVariable String key, @RequestBody String value) {
		redisService.setValue(key, value);
	}


	// Set a value with expiry
	@PostMapping("/value/{key}/expiry/{timeoutInSecs}")
	public void setValueWithExpiry(@PathVariable String key, @RequestBody String value, @PathVariable long timeoutInSecs) {
		redisService.setValueWithExpiry(key, value, timeoutInSecs);
	}


	// Get a value from Redis
	@GetMapping("/value/{key}")
	public String getValue(@PathVariable String key) {
		return redisService.getValue(key);
	}
}
