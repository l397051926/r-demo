package com.gennlife.rws.schema;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.packagingservice.arithmetic.utils.FileUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import static com.gennlife.darren.controlflow.exception.Force.force;
import static com.gennlife.darren.controlflow.exception.Force.forcible;
import static java.util.stream.Collectors.toSet;

/**
 * es 创建索引
 *
 * @author 安西平
 * @version 2.0.0
 */

public class EmrFieldAnalyzer extends AbstractFieldAnalyzer {
	private JSONObject fieldProObj = null;

	HashMap<String, String> nlpFiledsfullNameMap = new HashMap<>();
	HashSet<String> relevantGene = new HashSet<String>();

	HashMap<Integer, HashSet<String>> allHightLightFiledsMap = new HashMap<>();

	HashMap<String,HashSet<String>> talbeRelations = new HashMap<>();
	

	JSONObject additionalInfo;
	
	//数据是包结构的 组名
	Set<String> packagesGroupNames  = null;

	public EmrFieldAnalyzer(String FieldPropertyfilePath) {
		analyzer(FieldPropertyfilePath);
	}
	@Override
	public JSONObject getFieldMapobj() {
		return fieldProObj;
	}

	@Override
	public HashMap<String, JSONObject> getIndexFieldsMap() {
		return indexfields;
	}

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
	@Override
	protected JSONObject getFieldInfo(String fieldName, boolean isUIField) {

		if (isUIField) {
			if (uiFields.containsKey(fieldName)) {
				JSONObject obj = uiFields.get(fieldName);
				return obj;
			}
		} else {
			if (indexfields.containsKey(fieldName)) {
				JSONObject obj = indexfields.get(fieldName);
				return obj;
			}
		}

		return null;
	}

	@Override
	public String getFieldName(String fieldName, boolean isUIField) {
		JSONObject fieldInfo = getFieldInfo(fieldName, isUIField);
		if (fieldInfo != null && fieldInfo.containsKey("index_field_name")) {
			return fieldInfo.getString("index_field_name");
		}
		return null;
	}

	/**
	 * 获取指定列名的权重
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 * 		返回此列的权重 若返回 0 表示检索列不存在
	 * 
	 */
	@Override
	public Long getFieldBoost(String fieldName, boolean isUIField) {
		JSONObject fieldInfo = getFieldInfo(fieldName, isUIField);
		if (fieldInfo != null && fieldInfo.containsKey("boost")) {
			return fieldInfo.getLong("boost");
		}
		return 0L;
	}

	/**
	 * 获取指定列名的数据类型
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 * 		返回此列的数据类型 date string long double 若返回 null 表示检索列不存在
	 * 
	 */
	@Override
	public String getFieldDataType(String fieldName, boolean isUIField) {
		JSONObject fieldInfo = getFieldInfo(fieldName, isUIField);
		if (fieldInfo != null && fieldInfo.containsKey("field_type")) {
			return fieldInfo.getString("field_type");
		}
		return null;
	}

	@Override
	public String getDateFormat(String fieldName, boolean isUIField) {

		JSONObject fieldInfo = getFieldInfo(fieldName, isUIField);
		if (fieldInfo != null && fieldInfo.containsKey("date_format")) {
			return fieldInfo.getString("date_format");
		}
		return null;
	}

	/**
	 * 获取指定列是否分词
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 * 		true : 分词 false：不分词
	 * 
	 */
	@Override
	public boolean getFieldSegFlag(String fieldName, boolean isUIField) {
		JSONObject fieldInfo = getFieldInfo(fieldName, isUIField);
		if (fieldInfo != null && fieldInfo.containsKey("segment_flag")) {
			return fieldInfo.getBoolean("segment_flag");
		}
		return false;
	}

	/**
	 * 获取指定列是否在搜索时做同义词替换
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 * 		true : 搜索时需做同义词 false：不需要同义词
	 * 
	 */
	@Override
	public boolean getFieldNeedSynFlag(String fieldName, boolean isUIField) {
		return false;
	}

	/**
	 * 获取指定列是否需要嵌套对象的列
	 * 
	 * @param fieldName
	 *            列名 isUIField field是否是UI列名
	 * @return
	 * 
	 * 		true : 嵌套对象字段 false：非嵌套对象字段
	 * 
	 */
	public boolean getFieldNeedNestedFlag(String fieldName, boolean isUIField) {

		JSONObject fieldInfo = getFieldInfo(fieldName, isUIField);
		if (fieldInfo != null && fieldInfo.containsKey("nlp_flag")) {
			return fieldInfo.getBoolean("nlp_flag");
		}
		return false;

	}

	/**
	 * 列配置文件解析
	 * 
	 * @param filePath
	 *            列配置文件的路径
	 * 
	 * @return
	 */
	private void analyzer(String filePath) {

		String fieldPro = null;

		fieldPro = forcible(() -> force(() -> FileUtil.readString(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filePath))))));

		if (fieldPro == null || fieldPro.isEmpty()) {
			return;
		}

		this.fieldProObj = JSONObject.parseObject(fieldPro);
		
		if (fieldProObj.containsKey("_additional")) {
			this.additionalInfo = (JSONObject)fieldProObj.remove("_additional");
		}
		
		if (fieldProObj.containsKey("_packageGroupNams")) {
			JSONArray array = (JSONArray)fieldProObj.remove("_packageGroupNams");
			packagesGroupNames = array.stream().map(String.class::cast).collect(toSet());
		}

		if (fieldProObj.containsKey("talbe_relations")) {
			JSONObject relations = fieldProObj.getJSONObject("talbe_relations");
			//Set<Entry<>>
			 Set<Map.Entry<String, Object>> set = relations.entrySet();
			 
			 set.forEach(e -> {
				 String key = e.getKey();
				 JSONArray value = (JSONArray)e.getValue();
				 HashSet<String> child = new HashSet<String>();
				 value.forEach((v)->{child.add((String)v);});
				 talbeRelations.put(key, child);
			 });
			 
		}
		analyzerSub(this.fieldProObj);

	}

	private void analyzerSub(JSONObject obj) {
		
		if (obj.containsKey("index_field_name")) {

			String indexFieldName = obj.getString("index_field_name");
			String uiFieldName = obj.getString("ui_field_name");
			
			
			if (obj.containsKey("isPackage") && obj.getBooleanValue("isPackage") && obj.containsKey("group_name")) {
				packagesGroupNames.add(obj.getString("group_name"));
			}

			if (indexFieldName.contains("Clinically_relevant_gene_variants.")) {
				int pos = indexFieldName.lastIndexOf(".");
				String geneName = indexFieldName.substring(pos + 1);
				relevantGene.add(geneName);
			}

//			if (indexFieldName.contains("visits.")) {
//
//				obj.put("nested_path", "visits");
//			}

			indexfields.put(indexFieldName, obj);

			if (obj.containsKey("field_type") && obj.containsKey("show_search_classify")) {
				String fieldType = obj.getString("field_type");

				JSONArray showSearchTypeArr = obj.getJSONArray("show_search_classify");

				if ((fieldType.equals("text") || fieldType.equals("keyword"))) {

					for (int k = 0; k < showSearchTypeArr.size(); ++k) {
						int showSearchType = showSearchTypeArr.getIntValue(k);
						if (allHightLightFiledsMap.containsKey(showSearchType)) {
							HashSet<String> set = allHightLightFiledsMap.get(showSearchType);
							set.add(indexFieldName);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(indexFieldName);
							allHightLightFiledsMap.put(showSearchType, set);
						}
					}

				}
			}

			char[] charArr = indexFieldName.toCharArray();
			ArrayList<Integer> posList = new ArrayList<Integer>();
			for (int i = 0; i < charArr.length; ++i) {
				if (charArr[i] == '.') {
					posList.add(i);
				}
			}
			int size = posList.size();
			String key = "";
			if (size >= 3) {
				key = indexFieldName.substring(0, posList.get(1));
			} else if (size == 2) {
				key = indexFieldName.substring(0, posList.get(1));
			} else if (size == 1) {
				key = indexFieldName.substring(0, posList.get(0));
			} else {
				key = indexFieldName;
			}
			//fieldAndGroupNameMap.put(indexFieldName, key);
			//groupNameSet.add(key);

			if (obj.containsKey("nlp_flag")) {
				if (obj.getBoolean("nlp_flag")) {
					int pos = indexFieldName.lastIndexOf(".");
					key = indexFieldName;

					if (pos > 0) {
						key = indexFieldName.substring(pos + 1);
					}
					nlpFiledsfullNameMap.put(key, indexFieldName);

				}
			}

			if (obj.containsKey("nested_flag")) {
				boolean needNested = obj.getBoolean("nested_flag");

				if (needNested) {
					int p = indexFieldName.lastIndexOf(".");
					if (p > 0) {
						String nestedPath = indexFieldName.substring(0, p);
						obj.put("nested_path", nestedPath);

					}
				}

			}

			if (obj.containsKey("ui_field_name")) {
				this.uiFields.put(obj.getString("ui_field_name"), obj);
			}
		} else {

			Set<String> keySet = obj.keySet();

			for (String field : keySet) {
				if ("_id".equals(field) || "talbe_relations".equals(field)) {
					continue;
				}
				if(field.contains("24h_urinary_protein")){
					int i=1;
				}

				JSONObject fieldObj = obj.getJSONObject(field);
				analyzerSub(fieldObj);
			}
		}

	}

	@Override
	public String getFieldFullName(String key) {

		if (nlpFiledsfullNameMap.containsKey(key)) {
			return nlpFiledsfullNameMap.get(key);
		}

		return null;
	}

	@Override
	public HashSet<String> getRelevantGenes() {
		return relevantGene;
	}

	@Override
	public String getNestedPath(String fieldName, boolean isUIField) {
		JSONObject fieldInfo = getFieldInfo(fieldName, isUIField);
		if (fieldInfo != null && fieldInfo.containsKey("nested_path")) {
			return fieldInfo.getString("nested_path");
		}

		return null;
	}

	@Override
	public HashMap<Integer, HashSet<String>> getAllHightLightFiledsMap() {
		return allHightLightFiledsMap;
	}

	@Override
	public HashSet<String> getGroupNameSet() {
		return null;
	}

	@Override
	public String getGroupName(String indexFieldName) {

		return null;
	}

	@Override
	public String getTableName(String fieldName, boolean isUIField) {
		String key = "tableName";
		JSONObject fieldInfo = getFieldInfo(fieldName, isUIField);
		if (fieldInfo != null && fieldInfo.containsKey(key)) {
			return fieldInfo.getString(key);
		}
		return null;
	}

	@Override
	public String getParentTableName(String fieldName, boolean isUIField) {
		String key = "parent";
		JSONObject fieldInfo = getFieldInfo(fieldName, isUIField);
		if (fieldInfo != null && fieldInfo.containsKey(key)) {
			return fieldInfo.getString(key);
		}
		return null;
	}
	
	
	@Override
	public String getRealFieldName(String fieldName, boolean isUIField) {
		
		
		String tableName = getTableName(fieldName, isUIField);
		String indexfieldName = getFieldName(fieldName, isUIField);
		if (tableName == null || indexfieldName == null) {
			return null;
		}
		String[] tmp = indexfieldName.split("\\.");
		String realFieldName = tableName + "." + tmp[tmp.length-1];
		
		
		return realFieldName;
	}
	
	@Override
	public String getParentDocTypeName(String indexFieldName) {
		String key = "parentDocTypeName";
		JSONObject fieldInfo = getFieldInfo(indexFieldName, false);
		if (fieldInfo != null && fieldInfo.containsKey(key)) {
			return fieldInfo.getString(key);
		}
		return null;
	}
	
	@Override
	public String getCurrentDocTypeName(String indexFieldName) {
		String key = "currentDocTypeName";
		JSONObject fieldInfo = getFieldInfo(indexFieldName, false);
		if (fieldInfo != null && fieldInfo.containsKey(key)) {
			return fieldInfo.getString(key);
		}
		return null;
	}

	@Override
	public String getPatientPSNFieldName() {
		
		if (additionalInfo != null && additionalInfo.containsKey("P_PSN")) {
			return additionalInfo.getString("P_PSN");
		}
		return null;
	}

	@Override
	public String getVisitPSNFieldName() {
		
		if (additionalInfo != null && additionalInfo.containsKey("V_PSN")) {
			return additionalInfo.getString("V_PSN");
		}
		return null;
	}

	@Override
	public Set<String> getPackagesGroupNames() {
		return packagesGroupNames;
	}

	@Override
	public boolean isPackagedField(String fieldPath) {
		return isPackagedGroup(fieldPath.substring(0, fieldPath.lastIndexOf('.')));
	}

	@Override
	public boolean isPackagedGroup(String groupPath) {
		final KeyPath path = KeyPath.compile(groupPath);
		return packagesGroupNames.contains(("visits".equals(path.first()) ? path.keyPathByRemovingFirst() : path).toString());
	}

}
