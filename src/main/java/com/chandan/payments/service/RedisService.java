package com.chandan.payments.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class RedisService {
	private final RedisTemplate<String, String> redisTemplate;
	private final ListOperations<String, String> listOperations;
	private final HashOperations<String, String, String> hashOperations;
	private final ValueOperations<String, String> valueOperations;


	public RedisService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.listOperations = redisTemplate.opsForList();
		this.hashOperations = redisTemplate.opsForHash();
		this.valueOperations = redisTemplate.opsForValue();
	}
	public void addValueToList(String key, String value) {
		listOperations.rightPush(key, value);
	}
	public List<String> getAllValuesFromList(String key) {
		return listOperations.range(key, 0, -1);
	}

	public void setValueInHash(String hashName, String key, String value) {
		hashOperations.put(hashName, key, value);
	}
	public String getValueFromHash(String hashName, String key) {
		return hashOperations.get(hashName, key);
	}
	public Map<String, String> getAllEntriesFromHash(String hashName) {
		return hashOperations.entries(hashName);
	}

	public void setValue(String key, String value) {
		valueOperations.set(key, value);
	}

	//setValue with expiry
	public void setValueWithExpiry(String key, String value, long timeoutInSecs) {
		valueOperations.set(key, value, timeoutInSecs, TimeUnit.SECONDS);
	}

	public String getValue(String key) {
		return valueOperations.get(key);
	}
}
