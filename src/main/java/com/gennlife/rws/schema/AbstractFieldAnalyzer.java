package com.gennlife.rws.schema;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public abstract class AbstractFieldAnalyzer {
	// 列信息,key 为中文列名
	protected HashMap<String, JSONObject> uiFields = new HashMap<String, JSONObject>();
	// 索引中列名列表，key索引列名
	protected HashMap<String, JSONObject> indexfields = new HashMap<String, JSONObject>();

	public abstract HashMap<String, JSONObject> getIndexFieldsMap();

	/**
	 * 获取fieldName对应的json格式的详细信息
	 * 
	 * @param fieldName
	 *            索引列名
	 * 
	 * @return
	 * 
	 * 		返回此列对应的json格式的详细信息 若返回 null 表示检索列不存在或者没有搜索关键词
	 * 
	 */
	protected abstract JSONObject getFieldInfo(String fieldName, boolean isUIField);

	/**
	 * 获取索引中使用的列名
	 * 
	 * @param fieldUIName
	 *            UI所用的列名
	 * 
	 * @return
	 * 
	 * 		返回索引中所用的列名 若返回 null 表示检索列不存在或者没有搜索关键词
	 * 
	 */
	public abstract String getFieldName(String fieldUIName, boolean isUIField);

	/**
	 * 获取索引中使用的列名
	 * 
	 * @param fieldCnName
	 *            列中文名,UI搜素所用列名
	 * 
	 * @return
	 * 
	 * 		返回此列的权重 若无，返回 0
	 * 
	 */
	public abstract Long getFieldBoost(String fieldUIName, boolean isUIField);

	/**
	 * 获取索引中使用的列名
	 * 
	 * @param fieldUIName
	 *            UI 搜素所用列名
	 * 
	 * @return
	 * 
	 * 		返回此列的数据类型 date string long double 若返回 0 表示检索列不存在
	 * 
	 */
	public abstract String getFieldDataType(String fieldUIName, boolean isUIField);
	
	
	
	/**
	 * 获取指定列的日期格式
	 * 
	 * @param fieldUIName
	 *            UI 搜素所用列名
	 * 
	 * @return
	 * 
	 * 		
	 * 
	 */
	public abstract String getDateFormat(String fieldName,boolean isUIField);
	
	/**
	 * 获取指定列是否分词
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 * 		true : 分词   
	 *      false：不分词
	 * 
	 */
	public abstract boolean getFieldSegFlag(String fieldName, boolean isUIField);
	
	
	/**
	 * 获取指定列是否在搜索时做同义词替换
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 * 		true : 搜索时需做同义词  
	 *      false：不需要同义词
	 * 
	 */
	public abstract boolean getFieldNeedSynFlag(String fieldName, boolean isUIField);
	
	
	
	/**
	 * 获取指定列是否需要嵌套对象的列
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 * 		true : 嵌套对象字段
	 *      false：非嵌套对象字段
	 * 
	 */
	public abstract boolean getFieldNeedNestedFlag(String fieldName, boolean isUIField);
	
	
	
	
	/**
	 * 获取指定key 完整列名   组名.组名.key   
	 * 
	 * @param fieldName
	 *            列名key  最后一层
	 * @return
	 * 
	 * 
	 */
	public abstract String getFieldFullName(String key);
	
	
	
	
	/**
	 * 获取指定key 完整列名   组名.组名.key   
	 * 
	 * @param fieldName
	 *            列名key  最后一层
	 * @return
	 * 
	 * 
	 */
	public abstract HashSet<String> getRelevantGenes();
	
	
	
	/**
	 * 获取指定列的嵌套对象的path
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 *  嵌套对象的path
	 */
	public abstract String getNestedPath(String fieldName, boolean isUIField);


	/**
	 * 获取指定列所属表
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 *  嵌套对象的path
	 */
	public abstract String getTableName(String fieldName, boolean isUIField);

	
	/**
	 * 获取指定列所属父表
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 *  嵌套对象的path
	 */
	public abstract String getParentTableName(String fieldName, boolean isUIField);
	
	
	public abstract String getRealFieldName(String fieldName, boolean isUIField);

	public HashMap<Integer, HashSet<String>> getAllHightLightFiledsMap() {
		// TODO Auto-generated method stub
		return null;
	}

	public HashSet<String> getGroupNameSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getGroupName(String indexFieldName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public abstract String getParentDocTypeName(String indexFieldName);
	
	public  abstract String getCurrentDocTypeName(String indexFieldName);
	
	
	public  abstract String getPatientPSNFieldName();

	public  abstract String getVisitPSNFieldName();
	
	
	public  abstract Set<String> getPackagesGroupNames();

	public abstract boolean isPackagedField(String fieldPath);

	public abstract boolean isPackagedGroup(String groupPath);

	public abstract JSONObject getFieldMapobj();
}
