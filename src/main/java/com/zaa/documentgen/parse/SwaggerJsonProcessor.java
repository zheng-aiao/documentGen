package com.zaa.documentgen.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zaa.documentgen.constants.DocxConf;
import com.zaa.documentgen.model.ModelAttr;
import com.zaa.documentgen.model.Request;
import com.zaa.documentgen.model.Response;
import com.zaa.documentgen.util.DateUtils;
import com.zaa.documentgen.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class SwaggerJsonProcessor {

    /**
     * 提取首页信息： swagger的版本，文档描述，版本，标题，联系人名称，地址，邮箱，创建时间
     *
     * @param map
     * @return
     */
    public static Map<String, String> parseDocHome(Map<String, Object> map) {

        Map<String, String> mapIndex = new HashMap<>();
        DocxConf instance = DocxConf.getInstance();

        String swagger = map.get("swagger").toString();
        Map<String, Object> info = (Map<String, Object>) map.get("info");
        String description = info.get("description") == null ? "" : info.get("description").toString();
        String version =info.get("version") == null ? "" : info.get("version").toString();
        String title = info.get("title") == null ? "" : info.get("title").toString();
        String name = instance.getName();
        String url = instance.getUrl();
        String email = instance.getEmail();
        String time = DocxConf.getInstance().getDocxTimeValue() == null ? DateUtils.now(null)
                : DocxConf.getInstance().getDocxTimeValue();


        mapIndex.put(DocxConf.HOME_TITLE, title);
        mapIndex.put(DocxConf.HOME_DESC, description);
        mapIndex.put(DocxConf.HOME_VERSIONSWAGGER, swagger);
        mapIndex.put(DocxConf.HOME_VERSIONDOCX, version);
        mapIndex.put(DocxConf.HOME_NAME, name);
        mapIndex.put(DocxConf.HOME_URL, url);
        mapIndex.put(DocxConf.HOME_EMAIL, email);
        mapIndex.put(DocxConf.HOME_TIME, time);

        return mapIndex;
    }

    /**
     * 解析Definition ：构建各种Model.(VO,DTO,Entity)
     *
     * @param map
     * @return
     */
    public static Map<String, ModelAttr> parseDefinitions(Map<String, Object> map) {
        Map<String, Map<String, Object>> definitions = (Map<String, Map<String, Object>>) map.get("definitions");
        Map<String, ModelAttr> definitinMap = new HashMap<>(256);
        if (definitions != null) {
            Iterator<String> modelNameIt = definitions.keySet().iterator();
            while (modelNameIt.hasNext()) {
                String modeName = modelNameIt.next();
                parseModelAttr(definitions, definitinMap, modeName);
            }
        }
        return definitinMap;
    }

    /**
     * 解析Model: 递归生成ModelAttr，对$ref类型设置具体属性
     *
     * @param swaggerMap
     * @param resMap
     * @param modeName
     * @return
     */
    private static ModelAttr parseModelAttr(Map<String, Map<String, Object>> swaggerMap, Map<String, ModelAttr> resMap,
            String modeName) {
        ModelAttr modeAttr;
        if ((modeAttr = resMap.get("#/definitions/" + modeName)) == null) {
            modeAttr = new ModelAttr();
            resMap.put("#/definitions/" + modeName, modeAttr);
        } else if (modeAttr.isCompleted()) {
            return resMap.get("#/definitions/" + modeName);
        }
        // 这里 解析model的属性信息 properties
        Map<String, Object> modeProperties = (Map<String, Object>) swaggerMap.get(modeName).get("properties");
        if (modeProperties == null) {
            return null;
        }
        // 1. 解析model的 required属性 - 必填参数
        List<String> requiredProperties = (List<String>) swaggerMap.get(modeName).get("required");

        // 2. 解析model的 properties属性 - 属性信息：
        // 属性名称、类型（object/integer/integer(int32)/integer(int64)/array/string/Model）、 说明等
        List<ModelAttr> attrList = new ArrayList<>();
        Iterator<Map.Entry<String, Object>> mIt = modeProperties.entrySet().iterator();
        while (mIt.hasNext()) {

            Map.Entry<String, Object> mEntry = mIt.next();
            String name = mEntry.getKey(); // 属性名
            Map<String, Object> attrInfoMap = (Map<String, Object>) mEntry.getValue(); // type、format、description、enum、$ref、originalRef、items

            ModelAttr child = new ModelAttr();
            // (1) 名称
            child.setName(name);

            // (2) 类型： 解析model的属性值的 type与format 字段 ----> 设置 响应参数 的 数据类型

            // a. 如果属性有type对应的值，直接设置； 如果属性除了有type，也有format; 重新设置类型格式 为integer(int64):
            // ep："type" ：integer" ,"format": "int64"
            /**
             * "startDate": {
             * "type": "integer",
             * "format": "int64",
             * "description": "有效期始"
             * }，
             * "assessmentType": {
             * "type": "string",
             * "description": "评估法类型",
             * "enum": [
             * "LEC",
             * "LS",
             * "MES"
             * ]
             * },
             * "alias": {
             * "type": "array",
             * "description": "危化品别名",
             * "items": {
             * "type": "string"
             * }
             * },
             */
            child.setType((String) attrInfoMap.get("type"));
            if (attrInfoMap.get("format") != null) {
                child.setType(child.getType() + "(" + attrInfoMap.get("format") + ")");
            }
            // b. 如果属性没有type，则是一个model ; 类型设置为 object:Model的样式
            /**
             * 如果该Model里某属性的引用参数也是一个Model ，则解析在 $ref, originalRef 中获得所需值
             * "lecDto": {
             * "description": "lec评价法参数值",
             * "$ref": "#/definitions/LecDto",
             * "originalRef": "LecDto"
             * }
             */
            if (child.getType() == null || child.getType().equals("")) {
                String clsName = attrInfoMap.get("originalRef").toString();
                child.setType(clsName);
                modeAttr.setCompleted(true);
                ModelAttr refModel = parseModelAttr(swaggerMap, resMap, clsName);
                if (refModel != null) {
                    child.setProperties(refModel.getProperties());
                }
            }
            // c. 如果属性类型是array , 则要判断数组元素是一个model，还是普通类型 。
            /**
             * 如果该Model里某属性的引用参数也是一个Array[Model\Model] 数组，则解析在 items里的 $ref, originalRef
             * 中获得所需值
             * "checkMaintenanceRecords": {
             * "type": "array",
             * "description": "校核维护记录",
             * "items": {
             * "$ref": "#/definitions/CheckMaintenanceRecord",
             * "originalRef": "CheckMaintenanceRecord"
             * }
             * }
             * "alias": {
             * "type": "array",
             * "description": "危化品别名",
             * "items": {
             * "type": "string"
             * }
             * },
             * "ydata": {
             * "type": "array",
             * "items": {
             * "type": "integer",
             * "format": "int32"
             * }
             * },
             */
            if (child.getType().equals("array")) {
                Object items = attrInfoMap.get("items");
                if (items != null && (((Map) items).get("$ref")) != null) {
                    String clsName = ((Map) items).get("originalRef").toString();
                    child.setType(clsName);
                    modeAttr.setCompleted(true);
                    ModelAttr refModel = parseModelAttr(swaggerMap, resMap, clsName);
                    if (refModel != null) {
                        child.setProperties(refModel.getProperties());
                    }
                }
                if (items != null && (((Map) items).get("$ref")) == null && (((Map) items).get("type")) == null) {
                    // 普通类型数组 ep: array：integer、 array：string
                    child.setType(child.getType() + ":" + ((Map) items).get("type"));
                    if (attrInfoMap.get("format") != null) {
                        child.setType(child.getType() + "(" + ((Map) items).get("format").toString() + ")");
                    }
                }
            }

            // (3) 描述说明
            child.setDescription((String) attrInfoMap.get("description"));

            if (!CollectionUtils.isEmpty(requiredProperties) && requiredProperties.contains(name)) {
                child.setRequire(true);
            }
            attrList.add(child);
        }

        // 3. 解析model的 title
        Object title = swaggerMap.get(modeName).get("title");
        Object description = swaggerMap.get(modeName).get("description");
        Object type = swaggerMap.get(modeName).get("type");
        modeAttr.setClassName(title == null ? "" : title.toString());
        modeAttr.setType(type == null ? "" : type.toString());
        modeAttr.setDescription(description == null ? "" : description.toString());
        modeAttr.setProperties(attrList);
        return modeAttr;
    }

    public static Map<String, Object> parseTags(Map<String, Object> jsonMap) {
        Map<String, Object> tagsMap = new HashMap<>();
        List<Map<String, String>> tags = (List<Map<String, String>>) jsonMap.get("tags");
        for (Map<String, String> tag : tags) {
            Object orderVal = tag.get("x-order");
            if (orderVal == null) {
                Object extensions = tag.get("extensions");
                if (extensions instanceof Map) {
                    Object extOrder = ((Map<?, ?>) extensions).get("x-order");
                    if (extOrder != null) {
                        orderVal = extOrder;
                    }
                }
            }
            double order = orderVal == null ? Double.MAX_VALUE : Double.parseDouble(String.valueOf(orderVal));
            tagsMap.put(tag.get("name").toString(), order);
        }
        return tagsMap;
    }

    /**
     * 解析path
     *
     * @param map          全部内容
     * @param definitinMap 解析后的Model
     * @return
     */
    public static Map<String, List<Map<String, Object>>> parsePaths(Map<String, Object> map,
            Map<String, Object> tagsMap, Map<String, ModelAttr> definitinMap) throws JsonProcessingException {

        Map<String, List<Map<String, Object>>> mapAll = new LinkedHashMap<>();
        List<String> tagSortedKeys = tagsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(v -> ((Number) v).doubleValue())))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        for (String tagSortedKey : tagSortedKeys) {
            List<Map<String, Object>> interfaceList = new ArrayList<>();
            tagSortedKey = tagSortedKey.replaceFirst("^\\s*[\\d.]+\\s*", "");
            mapAll.put(tagSortedKey, interfaceList);
        }

        Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) map.get("paths");
        if (paths != null) {
            Iterator<Map.Entry<String, Map<String, Object>>> it = paths.entrySet().iterator();
            while (it.hasNext()) {
                // 2.1 某一请求路径 ep: "/nuas/api/v1/Gphone": {}
                Map.Entry<String, Map<String, Object>> path = it.next();

                String url = path.getKey();
                Iterator<Map.Entry<String, Object>> it2 = path.getValue().entrySet().iterator();
                // log.info("swagger2 url:{}", url);
                // 2.2 同一请求路经下，请求方式可能为 get,post,delete,put 这里要区分生成
                while (it2.hasNext()) {
                    Map.Entry<String, Object> firstRequest = it2.next();
                    String requestMethod = firstRequest.getKey();
                    Map<String, Object> content = (Map<String, Object>) firstRequest.getValue();

                    /**
                     * 解析每一个 请求路径的内容。内容包含：
                     * tags： 接口分组
                     * summary：接口标题
                     * description：接口描述
                     * consumes：请求体格式
                     * produces：响应体格式
                     * parameters：请求参数
                     * responses：响应参数
                     * deprecated：是否弃用
                     * x-order：接口顺序
                     * security：安全认证
                     * operationId：接口唯一标识
                     * externalDocs：外部文档
                     */

                    // 是否弃用, 弃用接口不在生成文档
                    Boolean deprecated = content.get("deprecated") == null ? false
                            : Boolean.parseBoolean(content.get("deprecated").toString());
                    if (deprecated) {
                        continue;
                    }

                    // 接口分组
                    List<String> tags = (List) content.get("tags");

                    // 接口顺序（兼容 extensions.x-order）
                    Object orderVal = content.get("x-order");
                    if (orderVal == null && content.get("extensions") instanceof Map) {
                        Object extOrder = ((Map) content.get("extensions")).get("x-order");
                        if (extOrder != null)
                            orderVal = extOrder;
                    }
                    double order = Double.parseDouble(orderVal == null ? "0" : orderVal.toString());

                    // 接口标题
                    String summary = content.get("summary") == null ? "" : content.get("summary").toString();

                    // 接口描述
                    String description = content.get("description") == null ? ""
                            : content.get("description").toString();
                    description = SwaggerParserUtils.descFormat(description);

                    // 请求体格式，ep: application/json, multipart/form-data
                    List<String> consumeList = (List) content.get("consumes");
                    String consumes = CollectionUtils.isEmpty(consumeList) ? "application/x-www-form-urlencoded"
                            : TextUtil.join(consumeList, ",");

                    // 响应体格式，ep: application/json
                    List<String> produceList = (List) content.get("produces");
                    String produces = CollectionUtils.isEmpty(produceList) ? "" : TextUtil.join(produceList, ",");

                    // 请求参数： 名称 ， 参数说明，请求类型， 是否必须， 数据类型， 示例值schema
                    List<LinkedHashMap> parameters = (ArrayList) content.get("parameters");
                    List<Request> requestList = SwaggerParserUtils.processRequestParamList(parameters, definitinMap);

                    // 响应参数
                    Map<String, Object> responses = (LinkedHashMap) content.get("responses");
                    Map<String, Object> obj = (Map<String, Object>) responses.get("200");

                    // 全部响应状态码
                    List<Response> responseCodeList = SwaggerParserUtils.processResponseCodeList(responses);

                    // 请求示例
                    Map<String, String> reqExam = SwaggerParserUtils.processRequestParam(requestList);

                    // 响应示例
                    Map<String, String> resExam = SwaggerParserUtils.processResponseParam(obj, definitinMap);

                    // 2.3 组装解析后 map
                    Map<String, Object> mapInterface = new HashMap<>();

                    // 请求参数
                    List<Map<String, Object>> reqList = new ArrayList<>();
                    for (int i = 0; i < requestList.size(); i++) {
                        Map<String, Object> reqMap = new HashMap<>();

                        Request request = requestList.get(i);
                        String paramType = request.getParamType();
                        // 普通数据类型
                        reqMap.put(DocxConf.PARAM_NAME, request.getName());
                        reqMap.put(DocxConf.PARAM_REQ_TYPE, String.valueOf(paramType));
                        reqMap.put(DocxConf.PARAM_DATA_TYPE, request.getType());
                        reqMap.put(DocxConf.PARAM_REQ_ISFILL, request.getRequire());
                        reqMap.put(DocxConf.PARAM_DESC,
                                request.getDescription() == null ? "" : request.getDescription());

                        /** 根据该参数，递归寻找其下一层参数 ，若无下级参数，则不再寻找 **/
                        // 复杂数据类型
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

                    // 响应参数
                    if (obj != null && obj.get("schema") != null) {
                        ModelAttr attr = SwaggerParserUtils.processResponseModelAttrs(obj, definitinMap);
                        List<ModelAttr> properties = attr.getProperties();
                        if (!CollectionUtils.isEmpty(properties)) {
                            // 添加出参
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

                    for (String tag : tags) {
                        tag = tag.replaceFirst("^\\s*[\\d.]+\\s*", "");
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

        // 对每个 List 按 "score" 字段升序排序
        mapAll.forEach((key, list) -> {
            list.sort((o1, o2) -> Double.compare(
                    ((Number) o1.getOrDefault(DocxConf.INTERFACE_ORDER, Double.MAX_VALUE)).doubleValue(),
                    ((Number) o2.getOrDefault(DocxConf.INTERFACE_ORDER, Double.MAX_VALUE)).doubleValue()));
        });

        return mapAll;
    }
}
