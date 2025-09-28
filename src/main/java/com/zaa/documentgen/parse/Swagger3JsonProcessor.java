package com.zaa.documentgen.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.model.ModelAttr;
import com.zaa.documentgen.model.Request;
import com.zaa.documentgen.model.Response;
import com.zaa.documentgen.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Parser for OpenAPI 3.0 documents, mirroring SwaggerJsonProcessor contract.
 */
@Slf4j
public class Swagger3JsonProcessor {

    public static Map<String, String> parseDocHome(Map<String, Object> map) {
        Map<String, String> mapIndex = new HashMap<>();
        DocxConf instance = DocxConf.getInstance();

        String openapi = String.valueOf(map.get("openapi"));
        Map<String, Object> info = (Map<String, Object>) map.get("info");
        String description = info == null ? "" : String.valueOf(info.getOrDefault("description", ""));
        String version = info == null ? "" : String.valueOf(info.getOrDefault("version", ""));
        String title = info == null ? "" : String.valueOf(info.getOrDefault("title", ""));
        String name = instance.getName();
        String url = instance.getUrl();
        String email = instance.getEmail();
        String time = DocxConf.getInstance().getDocxTimeValue() == null ? DateUtils.now(null)
                : DocxConf.getInstance().getDocxTimeValue();

        if (!instance.getVersionNum().equals("")) {
            version = instance.getVersionNum();
        }

        mapIndex.put(DocxConf.HOME_TITLE, title);
        mapIndex.put(DocxConf.HOME_DESC, description);
        mapIndex.put(DocxConf.HOME_VERSIONSWAGGER, openapi);
        mapIndex.put(DocxConf.HOME_VERSIONDOCX, version);
        mapIndex.put(DocxConf.HOME_NAME, name);
        mapIndex.put(DocxConf.HOME_URL, url);
        mapIndex.put(DocxConf.HOME_EMAIL, email);
        mapIndex.put(DocxConf.HOME_TIME, time);

        return mapIndex;
    }

    public static Map<String, ModelAttr> parseDefinitions(Map<String, Object> map) {
        // OpenAPI 3 uses components.schemas instead of definitions
        Map<String, Object> components = (Map<String, Object>) map.get("components");
        Map<String, Map<String, Object>> schemas = components == null ? null
                : (Map<String, Map<String, Object>>) components.get("schemas");
        Map<String, ModelAttr> definitinMap = new HashMap<>(256);
        if (schemas != null) {
            Iterator<String> modelNameIt = schemas.keySet().iterator();
            while (modelNameIt.hasNext()) {
                String modeName = modelNameIt.next();
                parseModelAttr(schemas, definitinMap, modeName);
            }
        }
        return definitinMap;
    }

    private static ModelAttr parseModelAttr(Map<String, Map<String, Object>> swaggerMap, Map<String, ModelAttr> resMap,
            String modeName) {
        ModelAttr modeAttr;
        if ((modeAttr = resMap.get("#/components/schemas/" + modeName)) == null) {
            modeAttr = new ModelAttr();
            resMap.put("#/components/schemas/" + modeName, modeAttr);
        } else if (modeAttr.isCompleted()) {
            return resMap.get("#/components/schemas/" + modeName);
        }

        Map<String, Object> schema = swaggerMap.get(modeName);
        if (schema == null) {
            return null;
        }
        Object title = schema.get("title") == null ?
                (modeName.contains("/") ? modeName.substring(modeName.lastIndexOf('/') + 1) : modeName)
                : schema.get("title");
        Object description = schema.get("description");
        String stype = schema.get("type") == null ? "" :  schema.get("type") .toString();
        if (schema.get("format") != null) {
            stype = stype + "(" + schema.get("format").toString() + ")";
        }

        Map<String, Object> propertiesMap = (Map<String, Object>) schema.get("properties");
        List enumList = (List) schema.get("enum");
        // If no properties, still persist primitive/array/object metadata for $ref usage
        if (propertiesMap == null && enumList != null && enumList.size()>0) {
            Object defaultValue = enumList.get(0);
            modeAttr.setDefaultValue(defaultValue);
            modeAttr.setCompleted(true);
        }


        List<ModelAttr> attrList = new ArrayList<>();
        List<String> requiredProperties = (List<String>) schema.get("required");
        if(propertiesMap != null && !propertiesMap.isEmpty()){
            Iterator<Map.Entry<String, Object>> mIt = propertiesMap.entrySet().iterator();
            while (mIt.hasNext()) {
                Map.Entry<String, Object> mEntry = mIt.next();
                String name = mEntry.getKey();
                Map<String, Object> attrInfoMap = (Map<String, Object>) mEntry.getValue();

                ModelAttr child = new ModelAttr();
                child.setName(name);

                // type / format
                String type = (String) attrInfoMap.get("type");
                if (type != null) {
                    child.setType(type);
                    if (attrInfoMap.get("format") != null) {
                        child.setType(child.getType() + "(" + attrInfoMap.get("format") + ")");
                    }
                }


                // $ref or allOf with $ref
                if (child.getType() == null || child.getType().equals("")) {
                    String refName = resolveRefFromSchema(attrInfoMap);
                    if (refName != null) {

                        String childTypeName = (refName.contains("/") ? refName.substring(refName.lastIndexOf('/') + 1) : refName);
                        child.setType(childTypeName);
                        child.setCompleted(true);
                        modeAttr.setCompleted(true);
                        ModelAttr refModel = parseModelAttr(swaggerMap, resMap, refName);
                        if (refModel != null) {
                            child.setProperties(refModel.getProperties());
                        }
                    }
                }

                if ("array".equals(type)) {
                    Object items = attrInfoMap.get("items");
                    if (items instanceof Map) {
                        Map itemsMap = (Map) items;
                        String refName = resolveRefFromSchema(itemsMap);
                        if (refName != null) {
                            String childTypeName = (refName.contains("/") ? refName.substring(refName.lastIndexOf('/') + 1) : refName);
                            child.setType(childTypeName);
                            child.setCompleted(true);
                            modeAttr.setCompleted(true);
                            ModelAttr refModel = parseModelAttr(swaggerMap, resMap, refName);
                            if (refModel != null) {
                                child.setProperties(refModel.getProperties());
                            }
                        } else if (itemsMap.get("type") != null) {
                            String itemType = String.valueOf(itemsMap.get("type"));
                            child.setType(child.getType() + ":" + itemType);
                            if (itemsMap.get("format") != null) {
                                child.setType(child.getType() + "(" + itemsMap.get("format").toString() + ")");
                            }
                        }
                    }
                }
                Object defaultValue = attrInfoMap.get("default");
                child.setDefaultValue(defaultValue);
                child.setDescription((String) attrInfoMap.get("description"));
                if (!CollectionUtils.isEmpty(requiredProperties) && requiredProperties.contains(name)) {
                    child.setRequire(true);
                }
                attrList.add(child);
            }
        }


        modeAttr.setClassName(title == null ? "" : title.toString());
        modeAttr.setType(stype == null ? "" : stype.toString());
        modeAttr.setDescription(description == null ? "" : description.toString());
        modeAttr.setProperties(attrList);
        return modeAttr;
    }

    /**
     * Resolve a schema's referenced component name supporting $ref and allOf with
     * $ref.
     */
    private static String resolveRefFromSchema(Map<String, Object> schema) {
        if (schema == null)
            return null;
        Object directRef = schema.get("$ref");
        if (directRef != null) {
            return String.valueOf(directRef).replace("#/components/schemas/", "");
        }
        Object allOf = schema.get("allOf");
        if (allOf instanceof List) {
            List list = (List) allOf;
            for (Object item : list) {
                if (item instanceof Map) {
                    Object itemRef = ((Map) item).get("$ref");
                    if (itemRef != null) {
                        return String.valueOf(itemRef).replace("#/components/schemas/", "");
                    }
                }
            }
        }
        return null;
    }

    public static Map<String, Object> parseTags(Map<String, Object> jsonMap) {
        Map<String, Object> tagsMap = new HashMap<>();

        Map<String, Double> nameToOrderMap = new LinkedHashMap<>();
        Map<String, Double> descToOrderMap = new LinkedHashMap<>();
        Map<String, String> nameToDescMap = new HashMap<>();
        List<Map<String, String>> tags = (List<Map<String, String>>) jsonMap.get("tags");
        Boolean hasOrder = false;
        if (tags != null) {
            for (Map<String, String> tag : tags) {
                Object orderVal = tag.get("x-order");
                String description = tag.get("description");
                String name = tag.get("name");
                if (orderVal == null) {
                    Object extensions = tag.get("extensions");
                    if (extensions instanceof Map) {
                        Object extOrder = ((Map<?, ?>) extensions).get("x-order");
                        if (extOrder != null) {
                            orderVal = extOrder;
                        }
                    }
                }
                hasOrder = (orderVal != null);
                double order = orderVal == null ? Double.MAX_VALUE : Double.parseDouble(String.valueOf(orderVal));
                nameToOrderMap.put(name, order);
                if(!StringUtils.isEmpty(description)){
                    descToOrderMap.put(description, order);
                    nameToDescMap.put(name, description);

                }
            }
        }
        tagsMap.put("hasOrder", hasOrder);
        tagsMap.put("nameToOrderMap", nameToOrderMap);
        if(!descToOrderMap.isEmpty()){
            tagsMap.put("descToOrderMap", descToOrderMap);
        }
        if(!nameToDescMap.isEmpty()){
            tagsMap.put("nameToDescMap", nameToDescMap);
        }
        return tagsMap;
    }

    public static Map<String, List<Map<String, Object>>> parsePaths(Map<String, Object> map,
            Map<String, Object> tagsMap, Map<String, ModelAttr> definitinMap) throws JsonProcessingException {
        Map<String, List<Map<String, Object>>> mapAll = new LinkedHashMap<>();

        // 获取tags
        Boolean orderType = (Boolean) tagsMap.getOrDefault("hasOrder", false);
        Map<String,String> nameToDescMap = (Map) tagsMap.getOrDefault("nameToDescMap", new HashMap<>());
        Map<String,Object> nameToOrderMap = (Map) tagsMap.getOrDefault("nameToOrderMap", new HashMap<>());
        Map<String,Object> descToOrderMap = (Map) tagsMap.getOrDefault("descToOrderMap", new HashMap<>());
        if(!descToOrderMap.isEmpty()){
            nameToOrderMap =descToOrderMap;
        }

        List<String> tagSortedKeys = orderType ?
                nameToOrderMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(v -> ((Number) v).doubleValue())))
                        .map(Map.Entry::getKey).collect(Collectors.toList())
                :
                nameToOrderMap.keySet().stream().collect(Collectors.toList());


        // 按照tags 新增map
        for (String tagSortedKey : tagSortedKeys) {
            List<Map<String, Object>> interfaceList = new ArrayList<>();
            tagSortedKey = tagSortedKey.replaceFirst("^\\s*[\\d.]+\\s*", "");
            mapAll.put(tagSortedKey, interfaceList);
        }

        Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) map.get("paths");
        if (paths != null) {
            Iterator<Map.Entry<String, Map<String, Object>>> it = paths.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Map<String, Object>> path = it.next();
                String url = path.getKey();
                // log.info("swagger3 url:{}", url);
                Iterator<Map.Entry<String, Object>> it2 = path.getValue().entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry<String, Object> firstRequest = it2.next();
                    String requestMethod = firstRequest.getKey();
                    Map<String, Object> content = (Map<String, Object>) firstRequest.getValue();

                    Boolean deprecated = content.get("deprecated") == null ? false
                            : Boolean.parseBoolean(content.get("deprecated").toString());
                    if (deprecated) {
                        continue;
                    }

                    List<String> tags = (List) content.get("tags");
                    // 接口顺序（兼容 extensions.x-order）
                    Object orderVal = content.get("x-order");
                    if (orderVal == null && content.get("extensions") instanceof Map) {
                        Object extOrder = ((Map) content.get("extensions")).get("x-order");
                        if (extOrder != null)
                            orderVal = extOrder;
                    }
                    double order = Double.parseDouble(orderVal == null ? "0" : orderVal.toString());

                    String summary = content.get("summary") == null ? "" : content.get("summary").toString();
                    String description = content.get("description") == null ? ""
                            : content.get("description").toString();
                    description = SwaggerParserUtils.descFormat(description);

                    // requestBody (OpenAPI 3)
                    String consumes = "";
                    Map<String, Object> requestBody = (Map<String, Object>) content.get("requestBody");
                    List<Request> requestList = new ArrayList<>();
                    if (requestBody != null) {
                        Map<String, Object> contentMap = (Map<String, Object>) requestBody.get("content");
                        if (contentMap != null && !contentMap.isEmpty()) {
                            consumes = String.join(",", contentMap.keySet());
                            // Prefer application/json schema when available
                            Map.Entry<String, Object> chosen = contentMap.containsKey("application/json")
                                    ? new AbstractMap.SimpleEntry<>("application/json",
                                            contentMap.get("application/json"))
                                    : contentMap.entrySet().iterator().next();
                            Map<String, Object> mediaTypeObj = (Map<String, Object>) chosen.getValue();
                            Map<String, Object> schema = (Map<String, Object>) mediaTypeObj.get("schema");
                            if (schema != null) {
                                requestList = SwaggerParserUtils.processRequestBodySchema(schema, definitinMap,consumes);
                            }
                        }
                    }

                    // parameters (path/query/header/cookie)
                    List<LinkedHashMap> parameters = (List) content.get("parameters");
                    if (parameters != null) {
                        requestList.addAll(SwaggerParserUtils.processRequestParamListV3(parameters, definitinMap));
                    }

                    // responses
                    String produces = "";
                    Map<String, Object> responses = (LinkedHashMap) content.get("responses");
                    Map<String, Object> okObj = null;
                    if (responses != null) {
                        // collect media types
                        Map<String, Object> okResp = (Map<String, Object>) responses.get("200");
                        if (okResp != null) {
                            Map<String, Object> okContent = (Map<String, Object>) okResp.get("content");
                            if (okContent != null && !okContent.isEmpty()) {
                                produces = String.join(",", okContent.keySet());
                                // pick application/json if present
                                Map.Entry<String, Object> chosen = okContent.containsKey("application/json")
                                        ? new AbstractMap.SimpleEntry<>("application/json",
                                                okContent.get("application/json"))
                                        : okContent.entrySet().iterator().next();
                                okObj = (Map<String, Object>) chosen.getValue();
                            }
                        }
                    }

                    List<Response> responseCodeList = SwaggerParserUtils.processResponseCodeListV3(responses);

                    Map<String, String> reqExam = SwaggerParserUtils.processRequestParam(requestList);

                    Map<String, String> resExam = SwaggerParserUtils.processResponseParamV3(okObj, definitinMap);

                    Map<String, Object> mapInterface = new HashMap<>();

                    List<Map<String, Object>> reqList = new ArrayList<>();
                    for (int i = 0; i < requestList.size(); i++) {
                        Map<String, Object> reqMap = new HashMap<>();
                        Request request = requestList.get(i);
                        String paramType = request.getParamType();
                        reqMap.put(DocxConf.PARAM_NAME, request.getName());
                        reqMap.put(DocxConf.PARAM_REQ_TYPE, String.valueOf(paramType));
                        reqMap.put(DocxConf.PARAM_DATA_TYPE, request.getType());
                        reqMap.put(DocxConf.PARAM_REQ_ISFILL, request.getRequire());
                        reqMap.put(DocxConf.PARAM_DESC,
                                request.getDescription() == null ? "" : request.getDescription());
                        if (request.getModelAttr() != null) {
                            ModelAttr modelAttr = request.getModelAttr();
                            if (Objects.nonNull(modelAttr)) {
                                List<ModelAttr> properties = modelAttr.getProperties();
                                List<Map<String, Object>> reqSubList = SwaggerParserUtils.addReqParam(paramType,
                                        properties);
                                reqMap.put(DocxConf.PARAM_SUB_LIST, reqSubList);
                            }
                        }
                        reqList.add(reqMap);
                    }
                    mapInterface.put(DocxConf.INTERFACE_REQ, reqList);

                    if (okObj != null) {
                        ModelAttr attr = SwaggerParserUtils.processResponseModelAttrsV3(okObj, definitinMap);
                        List<ModelAttr> properties = attr.getProperties();
                        if (!CollectionUtils.isEmpty(properties)) {
                            List<Map<String, Object>> resList = SwaggerParserUtils.addResParam(properties);
                            mapInterface.put(DocxConf.INTERFACE_RES, resList);
                        }
                    }

                    mapInterface.put(DocxConf.INTERFACE_REQ_EXAMPLE, reqExam);
                    mapInterface.put(DocxConf.INTERFACE_RES_EXAMPLE, resExam);
                    mapInterface.put(DocxConf.INTERFACE_NAME, summary);
                    mapInterface.put(DocxConf.INTERFACE_DESC, description);
                    mapInterface.put(DocxConf.INTERFACE_URL, url);
                    mapInterface.put(DocxConf.INTERFACE_METHOD, requestMethod);
                    mapInterface.put(DocxConf.INTERFACE_TYPE, consumes);
                    mapInterface.put(DocxConf.INTERFACE_TYPE_RES, produces);
                    mapInterface.put(DocxConf.INTERFACE_ORDER, order);

                    if (tags != null) {
                        for (String tag : tags) {
                            tag = tag.replaceFirst("^\\s*[\\d.]+\\s*", "");
                            tag = nameToDescMap.getOrDefault(tag, tag);
                            if (mapAll.get(tag) == null) {
                                List<Map<String, Object>> interfaceList = new ArrayList<>();
                                interfaceList.add(mapInterface);
                                mapAll.put(tag, interfaceList);
                            } else {
                                mapAll.get(tag).add(mapInterface);
                            }
                        }
                    }
                }
            }
        }

        mapAll.forEach((key, list) -> {
            list.sort((o1, o2) -> Double.compare(
                    ((Number) o1.getOrDefault(DocxConf.INTERFACE_ORDER, Double.MAX_VALUE)).doubleValue(),
                    ((Number) o2.getOrDefault(DocxConf.INTERFACE_ORDER, Double.MAX_VALUE)).doubleValue()));
        });

        return mapAll;
    }
}
