package com.zaa.documentgen.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.model.ModelAttr;
import com.zaa.documentgen.model.Request;
import com.zaa.documentgen.model.Response;
import com.zaa.documentgen.util.JsonUtils;
import com.zaa.documentgen.util.TextUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class SwaggerParserUtils {

    /**
     * 接口描述字段 - 显示格式处理
     *
     * @param description
     * @return
     */
    public static String descFormat(String description) {
        /** 针对经过测试，发现具有共性的 协议描述文字进行处理，以便适配word的显示 **/

        description = description.replaceAll("\t\t\t\t\t\t\t\t\t", "\t").replaceAll("\t\t\t\t\t", "\t")
                .replaceAll("\t\t\t", "\t")
                .replaceAll("\t\n\t\n", "\n").replaceAll("\t\t\t\t\t", "\t")
                .replaceAll("\n\t\n\t", "\n\t").replaceAll("\n\n\n\n", "\n\n")
                .replaceAll("\n\n\t\n\n", "\n\n")
                .replaceAll("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n", "\t\n")
                .replaceAll(
                        "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n",
                        "\t\n")
                .replaceAll("\n\n\t\t", "\n\t").replaceAll("~~~\n", "")
                .replaceAll("\n\n\n\n", "\n").replaceAll("\n\n\n", "\n").replaceAll("\n\n", "\n")
                .replaceAll("###", "").replaceAll("~~~", "");
        return description;
    }

    /**
     * 请求参数解析 - 处理每个请求参数，获取其名称、类型、是否必填、说明、默认值、子参数等信息
     *
     * @param in
     * @param properties
     * @return
     */
    public static List<Map<String, Object>> addReqParam(Object in, List<ModelAttr> properties) {
        List<Map<String, Object>> reqSubList = new ArrayList<>();
        if (properties != null && properties.size() > 0) {
            for (int j = 0; j < properties.size(); j++) {
                ModelAttr attr = properties.get(j);
                Map<String, Object> mreqChild = new HashMap<>();
                mreqChild.put(DocxConf.PARAM_NAME, attr.getName());
                mreqChild.put(DocxConf.PARAM_REQ_TYPE, String.valueOf(in));
                mreqChild.put(DocxConf.PARAM_DATA_TYPE, attr.getType());
                mreqChild.put(DocxConf.PARAM_REQ_ISFILL, attr.getRequire());
                mreqChild.put(DocxConf.PARAM_DESC, attr.getDescription() == null ? "" : attr.getDescription());
                List<ModelAttr> properties1 = attr.getProperties();
                List<Map<String, Object>> maps = addReqParam(in, properties1);
                mreqChild.put(DocxConf.PARAM_SUB_LIST, maps);
                reqSubList.add(mreqChild);
            }
        }
        return reqSubList;
    }

    /**
     * 响应参数解析 - 处理每个响应参数，获取其名称、类型、说明、子参数等信息
     *
     * @param properties
     * @return
     */
    public static List<Map<String, Object>> addResParam(List<ModelAttr> properties) {

        List<Map<String, Object>> resSubList = new ArrayList<>();
        if (properties != null && properties.size() > 0) {
            for (int j = 0; j < properties.size(); j++) {
                ModelAttr attr = properties.get(j);
                Map<String, Object> mresChild = new HashMap<>();
                if (attr.getProperties().size() > 0) {
                    mresChild.put(DocxConf.PARAM_SUB_LIST, attr.getProperties());
                } else {
                    mresChild.put(DocxConf.PARAM_SUB_LIST, new ArrayList<>());
                }

                mresChild.put(DocxConf.PARAM_NAME, attr.getName());
                mresChild.put(DocxConf.PARAM_DATA_TYPE, attr.getType());
                mresChild.put(DocxConf.PARAM_DESC, attr.getDescription() == null ? "" : attr.getDescription());
                mresChild.put(DocxConf.PARAM_REQ_ISFILL, attr.getRequire());

                List<ModelAttr> properties1 = attr.getProperties();
                List<Map<String, Object>> maps = addResParam(properties1);
                mresChild.put(DocxConf.PARAM_SUB_LIST, maps);
                resSubList.add(mresChild);
            }
        }
        return resSubList;
    }

    /**
     * 请求参数解析 - 处理每个请求参数，获取其名称、类型、是否必填、说明、默认值、子参数等信息
     *
     * @param parameters
     * @param definitinMap
     * @return
     */
    public static List<Request> processRequestParamList(List<LinkedHashMap> parameters,
            Map<String, ModelAttr> definitinMap) {
        List<Request> requestList = new ArrayList<>();
        if (parameters != null && parameters.size() > 0) {
            for (int i = 0; i < parameters.size(); i++) {
                Map<String, Object> param = parameters.get(i);

                String paramIn = param.get("in").toString();
                String paramName = param.get("name").toString();
                String paramDescription = param.get("description") == null ? "" : param.get("description").toString();
                String paramRequired = param.get("required").toString();
                String paramType = param.get("type") == null ? "" : param.get("type").toString();
                Object paramDefault = param.get("default");

                // 数据类型处理
                Object ref = null;
                if (paramIn.equals("query")) {
                    // query参数 有两种数据类型
                    // - 普通类型 ep：integer(int64)、string、integer(int32)、boolean
                    // - 数组类型 ep: array:integer(int32) 、array:string、array:file

                    if (paramType.equals("array")) {
                        Map<String, Object> items = (Map) param.get("items");
                        String paramItemType = items.get("type") == null ? "" : items.get("type").toString();
                        if (items.get("format") != null) {
                            paramItemType = TextUtil.concat(paramItemType, "(", items.get("format").toString(), ")");
                        }
                        paramType = paramType + "(" + paramItemType + ")";
                    } else {
                        if (param.get("format") != null) {
                            paramType = TextUtil.concat(paramType, "(", param.get("format").toString(), ")");
                        }
                    }
                } else if (paramIn.equals("path")) {
                    // path 一般只有基本数据类型 ep：integer(int64)、string、integer(int32)、boolean
                    if (param.get("format") != null) {
                        paramType = TextUtil.concat(paramType, "(", param.get("format").toString(), ")");
                    }
                } else if (paramIn.equals("formData")) {
                    // formData一般有两种数据类型（1）file (2) array:file
                    if (paramType.equals("array")) {
                        Map<String, Object> items = (Map) param.get("items");
                        String paramItemType = items.get("type") == null ? "" : items.get("type").toString();
                        paramType = paramType + ":" + paramItemType;
                    }
                } else if (paramIn.equals("body")) {
                    // 一般为post请求体， 以引用实体为准
                    Map<String, Object> schema = (Map) param.get("schema");
                    String schemaType = schema.get("type") == null ? "" : schema.get("type").toString();

                    // 自定义类 ep：WorkOrderFeedback
                    if (StringUtils.isEmpty(schemaType)) {
                        paramType = schema.get("originalRef") == null ? "" : schema.get("originalRef").toString();
                        ref = schema.get("$ref");
                    } else {
                        if (schema.get("type").equals("array")) {
                            // 数组类型
                            paramType = "array";
                            Map<String, Object> items = schema.get("items") != null ? (Map) schema.get("items") : null;

                            if (items != null) {
                                // 数组元素为普通类型： array(string) 、array(integer)
                                if (items.get("type") != null) {
                                    paramType = TextUtil.concat(paramType, "(", items.get("type").toString(), ")");
                                }

                                // 数组元素位 model 自定义类型 ep: array(WorkOrder)
                                if (items.get("$ref") != null) {
                                    ref = items.get("$ref");
                                    paramType = TextUtil.concat(paramType, ":", items.get("originalRef").toString());
                                }
                            }
                        } else {
                            // 普通类型 ep:string\integer\boolean\number\integer(int32)\string(binary)
                            paramType = schema.get("type").toString();
                            if (schema.get("format") != null) {
                                paramType = TextUtil.concat(paramType, "(", schema.get("format").toString(), ")");
                            }
                        }
                    }
                }

                /** request对象 : 用来生成请求示例 **/
                Request request = new Request();
                request.setName(paramName);
                request.setType(paramType);
                request.setParamType(paramIn);
                request.setDescription(paramDescription);
                request.setDefaultValue(paramDefault);
                request.setRequire(paramRequired);
                if (ref != null) {
                    request.setModelAttr(definitinMap.get(ref));
                }

                requestList.add(request);
            }
        }
        return requestList;
    }

    /**
     * OpenAPI 3.0 - parameters parsing (path/query/header/cookie)
     */
    public static List<Request> processRequestParamListV3(List<LinkedHashMap> parameters,
            Map<String, ModelAttr> definitinMap) {
        List<Request> requestList = new ArrayList<>();
        if (parameters != null && parameters.size() > 0) {
            for (int i = 0; i < parameters.size(); i++) {
                Map<String, Object> param = parameters.get(i);

                String paramIn = String.valueOf(param.get("in"));
                String paramName = String.valueOf(param.get("name"));
                String paramDescription = param.get("description") == null ? "" : param.get("description").toString();
                String paramRequired = String.valueOf(param.getOrDefault("required", false));
                Object defaultValue = param.get("example");
                // schema
                String paramType = "";
                ModelAttr attachModelAttr = null;
                Map<String, Object> schema = (Map<String, Object>) param.get("schema");
                if (schema != null) {
                    // Prefer $ref/allOf resolution first
                    String ref = resolveRefFromSchemaV3(schema);
                    if (ref != null) {
                        attachModelAttr = definitinMap.get(ref);
                        // Use the model name for display when possible
                        String modelName = ref.contains("/") ? ref.substring(ref.lastIndexOf('/') + 1) : ref;
                        paramType = modelName;

                    } else {
                        paramDescription = schema.get("description") == null ? "" : schema.get("description").toString();
                        defaultValue = schema.get("default");
                        String type = (String) schema.get("type");
                        if (type != null) {
                            paramType = type;
                            if (schema.get("format") != null) {
                                paramType = TextUtil.concat(paramType, "(", schema.get("format").toString(), ")");
                            }
                            if ("array".equals(type)) {
                                Map<String, Object> items = (Map<String, Object>) schema.get("items");
                                if (items != null) {
                                    // handle items $ref/allOf or primitive types
                                    String itemsRef = resolveRefFromSchemaV3(items);
                                    if (itemsRef != null) {
                                        attachModelAttr = definitinMap.get(itemsRef);
                                        String modelName = itemsRef.contains("/") ? itemsRef.substring(itemsRef.lastIndexOf('/') + 1) : itemsRef;
                                        paramType = "array:" + modelName;
                                    } else if (items.get("type") != null) {
                                        String itemType = String.valueOf(items.get("type"));
                                        paramType = TextUtil.concat(paramType, "(", itemType, ")");
                                    }
                                }
                            }
                        }
                    }
                }

                Request request = new Request();
                request.setName(paramName);
                request.setType(paramType);
                request.setParamType(paramIn);
                request.setDescription(paramDescription);
                request.setDefaultValue(defaultValue);
                request.setRequire(paramRequired);
                if (attachModelAttr != null) {
                    request.setModelAttr(attachModelAttr);
                    if(attachModelAttr.getDefaultValue() != null){
                        request.setDefaultValue(defaultValue);
                    }
                }
                requestList.add(request);
            }
        }
        return requestList;
    }

    /**
     * OpenAPI 3.0 - requestBody schema to Request list
     */
    public static List<Request> processRequestBodySchema(Map<String, Object> schema,
            Map<String, ModelAttr> definitinMap,String consumes) {
        List<Request> requestList = new ArrayList<>();
        if (schema == null) {
            return requestList;
        }
        // handle $ref or object/array
        String ref = resolveRefFromSchemaV3(schema);
        if (ref != null) {
            ModelAttr modelAttr = definitinMap.get(ref);
            if (modelAttr != null) {
                Request request = new Request();
                // String modelName = ref.contains("/") ? ref.substring(ref.lastIndexOf('/') + 1) : ref;
                request.setName("body");
                request.setType("object");
                request.setParamType("body");
                request.setModelAttr(modelAttr);
                request.setRequire("true");
                requestList.add(request);
            }
        } else if ("object".equals(schema.get("type"))) {
            // expand object properties
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String name = entry.getKey();
                    Map<String, Object> prop = (Map<String, Object>) entry.getValue();
                    String propRef = resolveRefFromSchemaV3(prop);
                    Request request = new Request();
                    request.setName(name);
                    request.setRequire("false");
                    request.setParamType(consumes.contains("multipart/form-data") ?"query" : "body");
                    String type = propRef == null ? (String) prop.get("type") : null;
                    if (type != null) {
                        if ("array".equals(type)) {
                            Map<String, Object> items = (Map<String, Object>) prop.get("items");
                            if (items != null) {
                                String itemsRef = resolveRefFromSchemaV3(items);
                                if (itemsRef != null) {
                                    request.setType("array:");
                                    request.setModelAttr(definitinMap.get(itemsRef));
                                } else if (items.get("type") != null) {
                                    request.setType("array(" + items.get("type") + ")");
                                }
                            }
                        } else {
                            request.setType(type + (prop.get("format") != null ? "(" + prop.get("format") + ")" : ""));
                            request.setDescription(prop.get("description") != null ? prop.get("description").toString() : "");
                            request.setRequire(
                                    String.valueOf(schema.getOrDefault("nullable", false)).equals("false") ? "true" : "false");
                        }
                    } else if (propRef != null) {
                        request.setType("object");
                        request.setModelAttr(definitinMap.get(propRef));
                    }
                    requestList.add(request);
                }
            }
        } else if ("array".equals(schema.get("type"))) {
            Map<String, Object> items = (Map<String, Object>) schema.get("items");
            Request request = new Request();
            request.setName("body");
            request.setRequire("false");
            request.setParamType("body");
            if (items != null) {
                String itemsRef = resolveRefFromSchemaV3(items);
                if (itemsRef != null) {
                    request.setType("array:");
                    request.setModelAttr(definitinMap.get(itemsRef));
                } else if (items.get("type") != null) {
                    request.setType("array(" + items.get("type") + ")");
                }
            }
            requestList.add(request);
        }
        // primitive/binary body types, e.g. { type: string, format: binary }
        else {
            Object typeObj = schema.get("type");
            if (typeObj != null) {
                String t = String.valueOf(typeObj);
                String fmt = schema.get("format") == null ? null : String.valueOf(schema.get("format"));
                Request request = new Request();
                request.setName("body");
                request.setParamType("body");
                request.setType(fmt == null ? t : t + "(" + fmt + ")");
                request.setRequire(
                        String.valueOf(schema.getOrDefault("nullable", false)).equals("false") ? "true" : "false");
                requestList.add(request);
            }
        }
        return requestList;
    }

    /** Resolve $ref from schema supporting allOf in OpenAPI 3. */
    private static String resolveRefFromSchemaV3(Map<String, Object> schema) {
        if (schema == null)
            return null;
        Object directRef = schema.get("$ref");
        if (directRef != null) {
            return String.valueOf(directRef);
        }
        Object allOf = schema.get("allOf");
        if (allOf instanceof List) {
            List list = (List) allOf;
            for (Object item : list) {
                if (item instanceof Map) {
                    Object itemRef = ((Map) item).get("$ref");
                    if (itemRef != null) {
                        return String.valueOf(itemRef);
                    }
                }
            }
        }
        return null;
    }

    /**
     * OpenAPI 3.0 - response codes
     */
    public static List<Response> processResponseCodeListV3(Map<String, Object> responses) {
        if (responses == null)
            return Collections.emptyList();
        List<Response> responseList = new ArrayList<>();
        Iterator<Map.Entry<String, Object>> resIt = responses.entrySet().iterator();
        while (resIt.hasNext()) {
            Map.Entry<String, Object> entry = resIt.next();
            Response response = new Response();
            response.setName(entry.getKey());
            LinkedHashMap<String, Object> statusCodeInfo = (LinkedHashMap) entry.getValue();
            response.setDescription(String.valueOf(statusCodeInfo.get("description")));
            responseList.add(response);
        }
        return responseList;
    }

    /**
     * OpenAPI 3.0 - response example
     */
    public static Map<String, String> processResponseParamV3(Map<String, Object> okObj,
            Map<String, ModelAttr> definitinMap) throws JsonProcessingException {
        Map<String, String> response = new HashMap<>(8);
        if (okObj == null)
            return response;
        Map<String, Object> schema = (Map<String, Object>) okObj.get("schema");
        if (schema == null)
            return response;
        String ref = null;
        String type = (String) schema.get("type");
        if ("array".equals(type)) {
            Map<String, Object> items = (Map<String, Object>) schema.get("items");
            if (items != null && items.get("$ref") != null) {
                ref = String.valueOf(items.get("$ref"));
            }
        }
        if (schema.get("$ref") != null) {
            ref = String.valueOf(schema.get("$ref"));
        }
        if (TextUtil.isNotEmpty(ref)) {
            ModelAttr modelAttr = definitinMap.get(ref);
            if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
                Map<String, Object> responseMap = new HashMap<>(8);
                for (ModelAttr subModelAttr : modelAttr.getProperties()) {
                    responseMap.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
                }
                String res = com.zaa.documentgen.util.JsonUtils.writeJsonStr(responseMap);
                String resJson = com.zaa.documentgen.util.JsonUtils.validJson(res);
                response.put("response", resJson);
            }
        }
        return response;
    }

    /**
     * OpenAPI 3.0 - resolve response schema to ModelAttr
     */
    public static ModelAttr processResponseModelAttrsV3(Map<String, Object> okObj,
            Map<String, ModelAttr> definitinMap) {
        Map<String, Object> schema = (Map<String, Object>) okObj.get("schema");
        if (schema == null) {
            return new ModelAttr();
        }
        String type = schema.get("type") == null ? "" : schema.get("type").toString();
        String ref = null;
        if ("array".equals(type)) {
            Map<String, Object> items = (Map<String, Object>) schema.get("items");
            if (items != null && items.get("$ref") != null) {
                ref = String.valueOf(items.get("$ref"));
            }
        }
        if (schema.get("$ref") != null) {
            ref = String.valueOf(schema.get("$ref"));
        }
        ModelAttr modelAttr = new ModelAttr();
        if (TextUtil.isNotEmpty(ref) && definitinMap.get(ref) != null) {
            modelAttr = definitinMap.get(ref);
        }
        return modelAttr;
    }

    /**
     * 响应状态码
     *
     * @param responses
     * @return
     */
    public static List<Response> processResponseCodeList(Map<String, Object> responses) {
        List<Response> responseList = new ArrayList<>();
        Iterator<Map.Entry<String, Object>> resIt = responses.entrySet().iterator();
        while (resIt.hasNext()) {
            Map.Entry<String, Object> entry = resIt.next();
            Response response = new Response();
            // 状态码 200 201 401 403 404 这样
            response.setName(entry.getKey());
            LinkedHashMap<String, Object> statusCodeInfo = (LinkedHashMap) entry.getValue();
            response.setDescription(String.valueOf(statusCodeInfo.get("description")));
            Object schema = statusCodeInfo.get("schema");
            if (schema != null) {
                Object originalRef = ((LinkedHashMap) schema).get("originalRef");
                response.setRemark(originalRef == null ? "" : originalRef.toString());
            }
            responseList.add(response);
        }
        return responseList;
    }

    /**
     * 响应参数
     *
     * @param responseObj
     * @param definitinMap
     * @return
     */
    public static ModelAttr processResponseModelAttrs(Map<String, Object> responseObj,
            Map<String, ModelAttr> definitinMap) {
        Map<String, Object> schema = (Map<String, Object>) responseObj.get("schema");
        String type = schema.get("type") == null ? "" : schema.get("type").toString();
        String ref = null;
        // 数组
        if ("array".equals(type)) {
            Map<String, Object> items = (Map<String, Object>) schema.get("items");
            if (items != null && items.get("$ref") != null) {
                ref = (String) items.get("$ref");
                type += items.get("originalRef") == null ? "" : ":" + items.get("originalRef").toString();
            }
        }
        // 对象
        if (schema.get("$ref") != null) {
            ref = (String) schema.get("$ref");
            type += schema.get("originalRef") == null ? "" : ":" + schema.get("originalRef").toString();

        }

        // 其他类型
        ModelAttr modelAttr = new ModelAttr();
        if (!TextUtil.isBlank(ref) && definitinMap.get(ref) != null) {
            modelAttr = definitinMap.get(ref);
        }
        return modelAttr;
    }

    /**
     * 请求示例
     *
     * @param list
     */
    public static Map<String, String> processRequestParam(List<Request> list) throws JsonProcessingException {
        Map<String, Object> headerMap = new LinkedHashMap<>();
        Map<String, Object> queryMap = new LinkedHashMap<>();
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        Map<String, Object> pathMap = new LinkedHashMap<>();
        if (list != null && list.size() > 0) {
            for (Request request : list) {
                String name = request.getName();
                String paramType = request.getParamType();
                Object defaultValue = request.getDefaultValue();
                Object value = Objects.isNull(defaultValue) ? getValue(request.getType(), request.getModelAttr())
                        : defaultValue;
                switch (paramType) {
                    case "header":
                        headerMap.put(name, value);
                        break;
                    case "query":
                        queryMap.put(name, value);
                        break;
                    case "body":
                        // TODO 根据content-type序列化成不同格式，目前只用了json
                        jsonMap.put(name, value);
                        break;
                    case "path":
                        pathMap.put(name, value);
                        break;
                    default:
                        break;

                }
            }
        }
        String query = "";
        String header = "";
        String path = "";
        String body = "";
        if (!queryMap.isEmpty()) {
            query += getUrlParamsByMap(queryMap);
        }
        if (!headerMap.isEmpty()) {
            header += getHeaderByMap(headerMap);
        }
        if (!pathMap.isEmpty()) {
            path += getPathByMap(pathMap);
        }
        if (!jsonMap.isEmpty()) {
            if (jsonMap.size() == 1) {
                for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                    body += JsonUtils.writeJsonStr(entry.getValue());
                }
            } else {
                body += JsonUtils.writeJsonStr(jsonMap);
            }
        }
        if (!body.equals("")) {
            body = JsonUtils.validJson(body);
        }
        Map<String, String> resMap = new LinkedHashMap<>();
        // 目前没有header这种请求
        if (!header.equals("")) {
            resMap.put("header", header);
        }
        if (!path.equals("")) {
            resMap.put("path", path);
        }
        if (!query.equals("")) {
            resMap.put("query", query);
        }
        if (!body.equals("")) {
            resMap.put("body", body);
        }
        return resMap;
    }

    /**
     * 请求示例 - path请求方式 - 生成路径参数
     *
     * @param pathMap
     * @return
     */
    private static String getPathByMap(Map<String, Object> pathMap) {
        if (pathMap == null || pathMap.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("");
        for (Map.Entry<String, Object> entry : pathMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String && StringUtils.isEmpty(value.toString())) {
                value = "\"\"";
            }
            sb.append(entry.getKey() + "=" + value.toString());
            sb.append(", ");
        }
        String s = sb.toString();
        if (s.endsWith(", ")) {
            s = TextUtil.substringBeforeLast(s, ", ");
        }
        return s;
    }

    /**
     * 请求示例 - query请求方式 - 生成url参数
     *
     * @param map
     * @return
     */
    private static String getUrlParamsByMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("?");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (Objects.isNull(value)) {
                value = "\"\"";
            }
            if (value instanceof String && StringUtils.isEmpty(value.toString())) {
                value = "\"\"";
            }
            sb.append(entry.getKey() + "=" + value.toString());
            sb.append("&");
        }
        String s = sb.toString();
        if (s.endsWith("&")) {
            s = TextUtil.substringBeforeLast(s, "&");
        }
        return s;
    }

    /**
     * 请求示例 -header请求方式 - 生成header参数
     *
     * @param map
     * @return
     */
    private static String getHeaderByMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String && StringUtils.isEmpty(value.toString())) {
                value = "''";
            }
            sb.append("--header '");
            sb.append(entry.getKey() + ":" + value.toString());
            sb.append("'");
        }
        return sb.toString();
    }

    /**
     * 响应示例
     *
     * @param responseObj
     */
    public static Map<String, String> processResponseParam(Map<String, Object> responseObj,
            Map<String, ModelAttr> definitinMap) throws JsonProcessingException {
        Map<String, String> response = new HashMap<>(8);
        if (responseObj != null && responseObj.get("schema") != null) {
            Map<String, Object> schema = (Map<String, Object>) responseObj.get("schema");
            String type = (String) schema.get("type");
            String ref = null;
            // 数组
            if ("array".equals(type)) {
                Map<String, Object> items = (Map<String, Object>) schema.get("items");
                if (items != null && items.get("$ref") != null) {
                    ref = (String) items.get("$ref");
                }
            }
            // 对象
            if (schema.get("$ref") != null) {
                ref = (String) schema.get("$ref");
            }

            if (TextUtil.isNotEmpty(ref)) {
                ModelAttr modelAttr = definitinMap.get(ref);

                if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {

                    Map<String, Object> responseMap = new HashMap<>(8);
                    for (ModelAttr subModelAttr : modelAttr.getProperties()) {
                        responseMap.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
                    }
                    String res = JsonUtils.writeJsonStr(responseMap);
                    String resJson = JsonUtils.validJson(res);
                    response.put("response", resJson);
                }
            }
        }
        return response;
    }

    /**
     * 请求示例和响应示例 - 设置默认值
     *
     * @param type      类型
     * @param modelAttr 引用的类型
     */
    public static Object getValue(String type, ModelAttr modelAttr) {

        // 含有model的只有两种类型 object:Model .array:Model。 取前缀处理
        if(StringUtils.isEmpty(type)) {
            return "";
        }
        int pos;
        if ((pos = type.indexOf(":")) != -1) {
            type = type.substring(0, pos + 1);
        }

        Map<String, Object> map = new LinkedHashMap<>();
        switch (type) {
            case "string":
                return "";
            case "string(date-time)":
                return "2020/01/01 00:00:00";
            case "string(binary)":
                return "";
            case "string(uuid)":
                return "1b7d9089afc14512ac787e57f2eb6aa8";
            case "integer":
            case "integer(int64)":
            case "integer(int32)":
                return 0;
            case "number":
            case "number(double)":
                return 0.0;
            case "boolean":
                return true;
            case "file":
                return "(binary)";
            case "array(file)":
                List fileList = new ArrayList();
                return fileList;
            case "array(integer)":
            case "array(integer(int32))":
            case "array(integer(int64))":
                List<Integer> integerArray = new ArrayList();
                integerArray.add(0);
                integerArray.add(0);
                return integerArray;
            case "array(number)":
            case "array(number(double))":
                List<Double> doubleArray = new ArrayList();
                doubleArray.add(0.0);
                doubleArray.add(0.0);
                return doubleArray;
            case "array(string)":
                List<String> strArray = new ArrayList();
                strArray.add("");
                strArray.add("");
                return strArray;
            case "array(object)":
                List objList = new ArrayList();
                objList.add(map);
                return objList;
            case "array":
                List list = new ArrayList();
                return list;
            case "object":
                map = new LinkedHashMap<>();
                return map;
            case "array:":
                List modelList = new ArrayList();
                map = new LinkedHashMap<>();
                if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
                    for (ModelAttr subModelAttr : modelAttr.getProperties()) {
                        map.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
                    }
                }
                if (!map.isEmpty()) {
                    modelList.add(map);
                }
                return modelList;
            default:
                map = new LinkedHashMap<>();
                if (modelAttr != null) {
                    if(!CollectionUtils.isEmpty(modelAttr.getProperties())){
                        for (ModelAttr subModelAttr : modelAttr.getProperties()) {
                            map.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
                        }
                        return map;
                    }
                    if(!Objects.isNull(modelAttr.getDefaultValue())){
                        return modelAttr.getDefaultValue();
                    }
                }
                return null;
        }
    }

}
