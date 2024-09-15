package com.example.demo.base;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.util.BaseDataTransformer;

/**
 * Base Domain Service
 */
@Service
public abstract class BaseDomainService {

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <T>
	 * 
	 * @param target 目標物件
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件
	 */
	public <T> T transformEntityToData(Object target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <S,    T>
	 * 
	 * @param target 目標物件列表
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件列表
	 */
	public <S, T> List<T> transformEntityToData(List<S> target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

}
