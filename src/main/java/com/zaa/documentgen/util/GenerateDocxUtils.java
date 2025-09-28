//package com.zaa.documentgen.util;
//
//import com.aspose.words.*;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.zaa.documentgen.constants.DocxConf;
//import com.zaa.documentgen.model.ModelAttr;
//import com.zaa.documentgen.model.PdfBookModel;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
///**
// * TODO Add class comment here<p/>
// *@version 1.0.0
// *@since 1.0.0
// *@author zhengaiao
// *@history<br/>
// * ver         date      author   desc
// * 1.0.0    2022/4/11      zhengaiao  created<br/>
// *<p/>
// */
//@Slf4j
//public class GenerateDocxUtils {
//
//    /******************************** 展示未用 *****************************************************/
//    /**
//     * 生成 word，传值为json文件路径，返回值布尔
//     * outputStream == null的时候默认在根目录下生成docx文件
//     * @param sourceFile
//     * @param desFile
//     * @throws IOException
//     */
//    public static boolean generateWordFile(String sourceFile, File desFile,String sequence) {
//        try {
//            //1.读取json ，解析赋值,将整个json转map
//            String reader = TextUtil.reader(sourceFile);
//            ObjectMapper om = new ObjectMapper();
//            Map<String, Object> map = om.readValue(reader, HashMap.class);
//            //2.解析文档介绍信息(放在首页大标题下)
//            Map<String, String> mapIndex = parseWordIndex(map);
//            String host = map.get("host").toString();
//            //3.解析生成文档内容
//
//            Map<String, List<Map<String, Object>>> mappAll = new HashMap<>();
//            generateDocContent(mappAll, map, host,sequence);
//            //4.mappAll有序排列
//            DocxConf instance = DocxConf.getInstance();
//            Map<String, List<Map<String, Object>>> newMappAll = sortMapByKey(mappAll);
//            AsposeWordUtil.generateWordFile(instance,newMappAll, mapIndex, desFile);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    /**
//     * 生成pdf 可加水印 。传值json文件路径
//     *
//     * @param file json文件地址
//     * @param pdfFile  默认生成pdf文件
//     * @param sequence 文档筛选 1,2,3
//     * @throws IOException
//     */
//    public static boolean generatePdfFile(String file, File pdfFile, String sequence ) {
//        /** 1.声明变量 */
//        Boolean sign = true ;
//        DocxConf instance = DocxConf.getInstance();
//        Map<String, List<Map<String, Object>>> mappAll = new HashMap<>();
//        try {
//            /** 2.读取json,解析赋值 */
//            String reader = TextUtil.reader(file);
//            ObjectMapper om = new ObjectMapper();
//            Map<String, Object> map = om.readValue(reader, HashMap.class);
//            String host = map.get("host").toString();
//            /**3. 提取索引、正文信息 **/
//            Map<String, String> mapIndex = parseWordIndex(map);
//            generateDocContent(mappAll, map, host,sequence);
//            /**4. 从新排序、渲染生成 **/
//            Map<String, List<Map<String, Object>>> newMappAll = sortMapByKey(mappAll);
//            AsposeWordUtil.generatePdfFile(instance, newMappAll, mapIndex,  pdfFile);
//        } catch (Exception e) {
//            e.printStackTrace();
//            sign = false;
//        }
//        return sign;
//    }
//
//    /******************************** 原来生成逻辑 --  生成文档：不包括 querkStart 的markdown 的 接口文档 *************************************************/
//    /**
//     * 生成成pdf 可加水印，传值JSON文件流
//     *
//     * @param reader swagger导出的json文件流对象
//     * @param pdfFile  默认生成pdf文件
//     * @param sequence  文档筛选 1,2,3
//     * @throws IOException
//     */
//    public static boolean generatePdfFileByJSON(String reader, File pdfFile, String sequence ) {
//        Boolean sign = true ;
//        try {
//            if (TextUtil.isBlank(reader)) {
//                throw new Exception("jsonstr cannot null！");
//            }
//            /** 1.准备原料  **/
//            //配置文件信息
//            DocxConf instance = DocxConf.getInstance();
//            //所有的一级标题集合
//            Map<String, List<Map<String, Object>>> mappAll = new HashMap<>();
//            // json文件转map
//            ObjectMapper om = new ObjectMapper();
//            Map<String, Object> map = om.readValue(reader, HashMap.class);
//            String host = map.get("host").toString();
//            /** 2.解析文档介绍信息 */
//            // 首页信息
//            Map<String, String> mapIndex = parseWordIndex(map);
//            //内容信息
//            generateDocContent(mappAll, map, host,sequence);
//            /**3. mappAll有序排列 ，写入pdf **/
//            Map<String, List<Map<String, Object>>> newMappAll = sortMapByKey(mappAll);
//            AsposeWordUtil.generatePdfFile(instance, newMappAll, mapIndex, pdfFile  );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            sign =  false;
//        }
//        return sign;
//    }
//
//
//
//    /**
//     * 生成 word
//     * 返回值布尔，表示是否成功
//     * @param reader 传值为json文件内容
//     * @param desFile
//     * @param sequence
//     * @return
//     */
//    public static boolean generateWordFileByJSON(String reader, File desFile ,String sequence) {
//        try {
//            if (TextUtil.isBlank(reader)) {
//                throw new Exception("jsonstr cannot null！");
//            }
//            /** 1.声明变量 **/
//            ObjectMapper om = new ObjectMapper();
//            DocxConf instance = DocxConf.getInstance();
//            Map<String, List<Map<String, Object>>> mappAll = new HashMap<>();
//
//            /** 2.解析赋值转map,提取索引、正文信息 **/
//            Map<String, Object> map = om.readValue(reader, HashMap.class);
//            String host = map.get("host").toString();
//            Map<String, String> mapIndex = parseWordIndex(map);
//            generateDocContent(mappAll, map, host, sequence);
//            /**3. 从新排序、渲染生成 **/
//            Map<String, List<Map<String, Object>>> newMappAll = sortMapByKey(mappAll);
//            AsposeWordUtil.generateWordFile(instance, newMappAll, mapIndex, desFile);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//    /********************************正在使用的逻辑 - 只把内容生成个文件返回 *************************************************/
//
//    /**
//     * 生成内容对应的文档
//     *
//     * @param reader swagger导出的json文件流对象
//     * @param file  默认生成pdf文件
//     * @param sequence  文档筛选 1,2,3
//     * @throws IOException
//     */
//    public static boolean generateContentFileByJSON(String reader, File file, String sequence ) {
//        Boolean sign = true ;
//        try {
//            if (TextUtil.isBlank(reader)) {
//                throw new Exception("jsonstr cannot null！");
//            }
//            /** 1.准备原料  **/
//            //配置文件信息
//            DocxConf instance = DocxConf.getInstance();
//            //所有的一级标题集合
//            Map<String, List<Map<String, Object>>> mappAll = new HashMap<>();
//            // json文件转map
//            ObjectMapper om = new ObjectMapper();
//            Map<String, Object> map = om.readValue(reader, HashMap.class);
//            String host = map.get("host").toString();
//            /** 2.解析文档介绍信息 */
//            // 首页信息
//            Map<String, String> mapIndex = parseWordIndex(map);
//            //内容信息
//            generateDocContent(mappAll, map, host,sequence);
//            /**3. mappAll有序排列 ，写入pdf **/
//            Map<String, List<Map<String, Object>>> newMappAll = sortMapByKey(mappAll);
//            AsposeWordUtil.generateSwaggerContentFile(instance, newMappAll, mapIndex, file  );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            sign =  false;
//        }
//        return sign;
//    }
//
//
//
//
//    /**
//     * 一级标题，排序
//     * 完成内容按swagger的tag序号排序  Ep: 1.0用户会话API  2.0联系人API  3.0.............
//     *
//     * @param oriMap
//     * @return
//     */
//    public static Map<String, List<Map<String, Object>>> sortMapByKey(Map<String, List<Map<String, Object>>> oriMap) {
//        if (oriMap == null || oriMap.isEmpty()) {
//            return null;
//        }
//        String c = ".";
//        Map<String, List<Map<String, Object>>> sortedMap = new TreeMap<String, List<Map<String, Object>>>(new Comparator<String>() {
//            @Override
//            public int compare(String o1, String o2)
//            {
//                if (o1.contains(c) && o2.contains(c))
//                {
//                    return  (Double.parseDouble(o1.split(" ")[0]) -  Double.parseDouble(o2.split(" ")[0])) <= 0 ? -1 : 1;
//                } else if (o1.contains(c))
//                {
//                    return -1;
//                } else
//                {
//                    return 1;
//                }
//            }
//        });
//        Iterator<Map.Entry<String, List<Map<String, Object>>>> iterator = oriMap.entrySet().iterator();
//        while (iterator.hasNext()){
//            Map.Entry<String, List<Map<String, Object>>> firstMap = iterator.next();
//            List<Map<String, Object>> subList = firstMap.getValue();
//            List<Map<String, Object>> listResult = listSort(subList);
//            sortedMap.put(firstMap.getKey(),listResult);
//        }
//        return sortedMap;
//    }
//
//    /**
//     * 对二级列表，排序
//     * @param list
//     * @return
//     */
//    public static List<Map<String, Object>> listSort(List<Map<String, Object>> list) {
//        if(!list.isEmpty() && list.size() > 0){
//            Map<String, Object>   firstMap = list.get(0);
//            String name = (String) firstMap.get("name");
//            int order = Integer.parseInt(name.split("\\.")[0]);
//            /** 兼容，之前写接口，没有写 x-order 的情况 **/
//            if(order > 1000){
//                int newOrder = 0 ;
//                for (Map<String, Object> tempMap : list)
//                {
//                    newOrder++;
//                    String subtitle = (String)tempMap.get("name");
//                    int i = subtitle.indexOf(".");
//                    String newSubtitle = subtitle.replace(subtitle.substring(0, i + 1), newOrder + ".");
//                    tempMap.put("name",newSubtitle);
//
//                }
//            }else{
//                //如果，写接口时，x-order 实现Collections接口进行排序
//                Collections.sort(list, new Comparator<Map<String, Object>>()
//                {
//                    @Override
//                    public int compare(Map<String, Object> o1, Map<String, Object> o2)
//                    {
//                        String n1 = (String) o1.get("name");
//                        String n2 = (String) o2.get("name");
//
//                        return Integer.parseInt(n1.split("\\.")[0]) - Integer.parseInt(n2.split("\\.")[0]);
//
//                    }
//                } );
//            }
//
//        }
//        return list;
//
//    }
//    /**
//     * 生成文档正文 ; 逻辑为遍历每一个url地址 ;
//     *
//     * @param mappAll  处理后的结果放在mappAll（tag（controller）  List<二级列表>）
//     * @param map   原元素（json转化的map）
//     * @param host  主机名（生成文档时使用）
//     * @throws IOException
//     */
//    private static void generateDocContent(Map<String, List<Map<String, Object>>> mappAll, Map<String, Object> map, String host , String sequence) throws IOException {
//        /** 1.解析所有的model,在之后解析每个路径path的请求响应的参数 **/
//        Map<String, ModelAttr> definitinMap = parseDefinitions(map);
//        /** 2.解析paths,单个一级标题下的所有内容，每个一级标题下都有一个List，装的是二级及以下的接口内容 **/
//        Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) map.get("paths");
//        if (paths != null) {
//            Iterator<Map.Entry<String, Map<String, Object>>> it = paths.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry<String, Map<String, Object>> path = it.next();
//                // 请求路径
//                String url = path.getKey();
//                Iterator<Map.Entry<String, Object>> it2 = path.getValue().entrySet().iterator();
//                //同一请求路经下，请求方式可能为 get,post,delete,put 这里要做循环处理
//                while (it2.hasNext())
//                {
//                    Map.Entry<String, Object> firstRequest = it2.next();
//                    List<Map<String, Object>> reqList = new ArrayList<>();
//                    List<Map<String, Object>> resList = new ArrayList<>();
//                    Map<String, Object> content = (Map<String, Object>)firstRequest.getValue();
//
//                    /** 2.1 只对指定的controller进行生成，其他跳过；如果是指定生成swagger文件，则没有序号的大标题（如： 测试模块），不再生成 **/
//                    String title = String.valueOf(((List) content.get("tags")).get(0));
//                    if(title.contains(".")){
//
//                        StringBuilder chinese = new StringBuilder();
//                        String patternChinese  = "[\\u4e00-\\u9fa5\\sa-zA-Z]+";
//                        Pattern r = Pattern.compile(patternChinese);
//                        Matcher m = r.matcher(title);
//                        while (m.find()) {
//                            chinese .append(m.group());
//                        }
//                        String chineseInfo = chinese.toString();
//
//                        String tag = title.replaceAll(chineseInfo, "").replaceAll(" ", "");
//                        int index =  tag.indexOf(".");
//                        StringBuilder tagNum = new StringBuilder( tag.substring(0,index));
//                        tagNum.append(".");
//                        tagNum.append(tag.substring(index).replaceAll("\\.", ""));
//                        String seq = tagNum.toString();
//                        title = seq + " " + chineseInfo;
//
//                        if(!sequence.equals("")){
//                            String[] seqList = sequence.split(",");
//                            Boolean isExists= Arrays.asList(seqList).contains(seq);
//                            if (!isExists){
//                                break ;
//                            }
//                        }
//                    }else{
//                        if(!sequence.equals("")){
//                            String[] seqList = sequence.split(",");
//                            Boolean isExists= Arrays.asList(seqList).contains(title);
//                            if (!isExists){
//                                break ;
//                            }
//                        }
//                    }
//
//                    /** 2.2.大标题（每个controller对应一个tags，），每个path路径对应一个二级列表，存放该controller的每个接口 **/
//                    List<Map<String, Object>> list1;
//                    //如果没有大标题放在 其他接口
//                    if (title == null || title.equals(""))
//                    {
//                        list1 = mappAll.get("其他接口：无类说明");
//                        if (list1 == null)
//                        {
//                            list1 = new ArrayList<>();
//                            mappAll.put("其他接口：无类说明", list1);
//                        }
//                    } else
//                    {
//                        //若有大标题
//                        list1 = mappAll.get(title);
//                        if (list1 == null)
//                        {
//                            list1 = new ArrayList<>();
//                            //一级标题 ：二级接口内容
//                            mappAll.put(title, list1);
//                        }
//                    }
//                    /** 2.3 请求方式 **/
//                    String requestMethod = firstRequest.getKey();
//
//                    /** 2.4.小标题 （每个接口的标题。由x-order . summary 构成。  ep: 9. 根据模板名模糊查询模板信息）**/
//                    String tag = String.valueOf(content.get("x-order")) + "." + String.valueOf(content.get("summary"));
//                    /** 2.5.接口描述 **/
//                    String description = content.get("description") == null ? "" :content.get("description").toString();
//                    /** 2.6.请求参数格式，类似于 multipart/form-data **/
//                    String requestForm = "";
//                    List<String> consumes = (List) content.get("consumes");
//                    if (consumes != null && consumes.size() > 0)
//                    {
//                        requestForm = TextUtil.join(consumes, ",");
//                    }
//                    /** 2.7.返回参数格式，类似于 application/json**/
//                    String responseForm = "";
//                    List<String> produces = (List) content.get("produces");
//                    if (produces != null && produces.size() > 0)
//                    {
//                        responseForm = TextUtil.join(produces, ",");
//                    }
//
//                    /**  2.8. 请求体 对应 "请求参数"  **/
//                    List<LinkedHashMap> parameters = (ArrayList) content.get("parameters");
//                    List<Request> requestList = new ArrayList<>();
//                    //请求体解析:
//                    if (parameters != null && parameters.size() > 0)
//                    {
//                        for (int i = 0; i < parameters.size(); i++)
//                        {
//                            //String firstSuffix = i + ".";
//                            Map<String, Object> param = parameters.get(i);
//                            Object in = param.get("in");
//                            /**用来存放参数对象，设置好这五部分（参数名  数据类型  参数类型  是否必填  说明） **/
//                            Map<String, Object> mreqMap = new HashMap<>();
//                            mreqMap.put(DocxConf.PARAM_REQ_TYPE, String.valueOf(in));
//                            mreqMap.put(DocxConf.PARAM_NAME,  String.valueOf(param.get("name")));
//                            mreqMap.put(DocxConf.PARAM_DESC, String.valueOf(param.get("description")));
//                            String type = param.get("type") == null ? "object" : param.get("type").toString();
//                            if (param.get("format") != null)
//                            {
//                                type = TextUtil.concat(type, "(", param.get("format").toString(), ")");
//                            }
//                            mreqMap.put(DocxConf.PARAM_DATA_TYPE, type);
//                            mreqMap.put(DocxConf.PARAM_REQ_ISFILL, param.get("required") != null ? param.get("required").toString().concat("") : "false");
//                            mreqMap.put(DocxConf.PARAM_SUB_LIST, new ArrayList());
//                            reqList.add(mreqMap);
//                            /** 考虑对象参数类型(当类型为body，并且内部有model类型的参数，这里只处理这种类型,要找到该model下的二级参数) */
//                            Object ref = null; ;
//                            if (in != null && "body".equals(in))
//                            {
//                                Map<String, Object> schema = (Map) param.get("schema");
//                                ref = schema.get("$ref");
//                                // 数组情况另外处理
//                                if (schema.get("type") != null && "array".equals(schema.get("type")))
//                                {
//                                    Map<String,Object> items = (Map) schema.get("items");
//
//                                    ref = items.get("$ref");
//                                    type = "array";
//                                    mreqMap.put(DocxConf.PARAM_DATA_TYPE, type);
//
//                                    if(items.get("type") != null ) {
//                                        type = TextUtil.concat(type, "(", items.get("type").toString(), ")");
//                                        mreqMap.put(DocxConf.PARAM_DATA_TYPE, type);
//                                    }
//
//                                }
//                                /** 根据该参数，递归寻找其下一层参数 ，若无下级参数，则不再寻找**/
//                                if (ref != null)
//                                {
//                                    mreqMap.put(DocxConf.PARAM_DATA_TYPE, mreqMap.get(DocxConf.PARAM_DATA_TYPE).toString().concat(":").concat(ref.toString().replaceAll("#/definitions/", "")));
//                                    //获取实体类属性
//                                    //log.info("ref:{}",ref);
//
//                                    ModelAttr modelAttr = definitinMap.get(ref);
//                                    if(Objects.nonNull(modelAttr)){
//                                        List<ModelAttr> properties = modelAttr.getProperties();
//                                        List<Map<String, Object>> maps = addReqParam(in, properties);
//                                        mreqMap.put(DocxConf.PARAM_SUB_LIST, maps);
//                                    }
//                                }
//                            }
//
//                            if (in != null && ("formData".equals(in) || "query".equals(in))){
//                                Map<String,Object> items = (Map) param.get("items");
//                                if(items != null && items.get("type")!= null){
//                                    type = TextUtil.concat(type, "(", items.get("type").toString(), ")");
//                                    mreqMap.put(DocxConf.PARAM_DATA_TYPE, type);
//                                }
//                            }
//                            /**request对象 : 用来生成请求示例**/
//                            Request request = new Request();
//                            request.setName(String.valueOf(param.get("name")));
//                            request.setType(type);
//                            request.setParamType(String.valueOf(in));
//
//                            if (ref != null)
//                            {
//                                request.setType(request.getType() + ":" + ref.toString().replaceAll("#/definitions/", ""));
//                                request.setModelAttr(definitinMap.get(ref));
//                            }
//
//                            // 是否必填
//                            request.setRequire(false);
//                            if (param.get("required") != null)
//                            {
//                                request.setRequire((Boolean) param.get("required"));
//                            }
//                            // 参数说明
//                            request.setRemark(String.valueOf(param.get("description")));
//                            requestList.add(request);
//                        }
//                    }
//
//                    /** 2.9. 返回体 对应 "响应参数"  **/
//                    Map<String, Object> responses = (LinkedHashMap) content.get("responses");
//                    List<Response> responses1 = processResponseCodeList(responses);
//                    // 取出来状态是200时的返回值
//                    Map<String, Object> obj = (Map<String, Object>) responses.get("200");
//                    if (obj != null && obj.get("schema") != null)
//                    {
//                        ModelAttr attr = processResponseModelAttrs(obj, definitinMap);
//                        List<ModelAttr> properties = attr.getProperties();
//                        //反参为空时，作为基础数据类型存入
//                        if (properties == null || properties.size() == 0)
//                        {
//                            Map<String, Object> mresMap = new HashMap<>();
//                            mresMap.put(DocxConf.PARAM_DESC, attr.getDescription() == null ? "" : attr.getDescription());
//                            mresMap.put(DocxConf.PARAM_DATA_TYPE, attr.getType());
//                            mresMap.put(DocxConf.PARAM_NAME, "[无名称]");
//                            mresMap.put(DocxConf.PARAM_SUB_LIST,new ArrayList<>());
//                            resList.add(mresMap);
//                        } else
//                        {
//                            //添加出参
//                            resList = addResParam(properties);
//                        }
//                    }
//                    Map<String,String> reqExam = processRequestParam(requestList);
//                    Map<String, String> resExam = processResponseParam(obj, definitinMap);
//                    description = descFormat(description);
//                    /** 3. 将2中生成的信息装填到列表  **/
//                    addList(list1, reqList, resList, reqExam, resExam , tag, description, host.concat(url), requestMethod, requestForm, responseForm );
//                }
//            }
//        }
//    }
//
//    private static String descFormat(String description)
//    {
//        /** 针对经过测试，发现具有共性的 协议描述文字进行处理，以便适配word的显示**/
//
//        description = description.replaceAll("\t\t\t\t\t\t\t\t\t","\t").replaceAll("\t\t\t\t\t","\t").replaceAll("\t\t\t","\t")
//                .replaceAll("\t\n\t\n" ,"\n").replaceAll("\t\t\t\t\t","\t")
//                .replaceAll("\n\t\n\t","\n\t").replaceAll("\n\n\n\n","\n\n")
//                .replaceAll("\n\n\t\n\n","\n\n").replaceAll("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n","\t\n")
//                .replaceAll("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n","\t\n")
//                .replaceAll("\n\n\t\t","\n\t").replaceAll("~~~\n","")
//                .replaceAll("\n\n\n\n","\n").replaceAll("\n\n\n","\n").replaceAll("\n\n","\n")
//                .replaceAll("###","").replaceAll("~~~","");
//        return description;
//    }
//
//    /**
//     * 将一级标题下的每一个二级所含有的内容，装箱
//     *
//     * @param list
//     * @param reqList
//     * @param resList
//     * @param reqExam
//     * @param param
//     */
//    public static void addList(List<Map<String, Object>> list, List<Map<String, Object>> reqList, List<Map<String, Object>> resList, Map<String,String> reqExam,  Map<String,String> resExam,  String... param) {
//        Map<String, Object> map = new HashMap<>();
//        map.put(DocxConf.INTERFACE_REQ, reqList);
//        map.put(DocxConf.INTERFACE_RES, resList);
//        map.put(DocxConf.INTERFACE_REQ_EXAMPLE,reqExam );
//        map.put(DocxConf.INTERFACE_RES_EXAMPLE, resExam);
//        map.put(DocxConf.INTERFACE_NAME, param[0]);
//        map.put(DocxConf.INTERFACE_DESC, param[1]);
//        map.put(DocxConf.INTERFACE_URL, param[2]);
//        map.put(DocxConf.INTERFACE_METHOD, param[3]);
//        map.put(DocxConf.INTERFACE_TYPE, param[4]);
//        map.put(DocxConf.INTERFACE_TYPE_RES, param[5]);
//        list.add(map);
//    }
//    /**
//     * 处理返回值,返回为data和status 两个列表的形式(返回示例)
//     *
//     * @param responseObj
//     *
//     */
//    private static Map<String, String> processResponseParam(Map<String, Object> responseObj, Map<String, ModelAttr> definitinMap) throws JsonProcessingException {
//        Map<String, String> response = new HashMap<>(8);
//        if (responseObj != null && responseObj.get("schema") != null) {
//            Map<String, Object> schema = (Map<String, Object>) responseObj.get("schema");
//            String type = (String) schema.get("type");
//            String ref = null;
//            // 数组
//            if ("array".equals(type)) {
//                Map<String, Object> items = (Map<String, Object>) schema.get("items");
//                if (items != null && items.get("$ref") != null) {
//                    ref = (String) items.get("$ref");
//                }
//            }
//            // 对象
//            if (schema.get("$ref") != null) {
//                ref = (String) schema.get("$ref");
//            }
//
//            if (TextUtil.isNotEmpty(ref)) {
//                ModelAttr modelAttr = definitinMap.get(ref);
//
//                if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
//                    int size = modelAttr.getProperties().size();
//                    List<String> standNames = modelAttr.getProperties().stream().map(k -> k.getName()).collect(Collectors.toList());
//                    if(size == 2 &&  standNames.contains("data") &&  standNames.contains("status")){
//                        for (ModelAttr subModelAttr : modelAttr.getProperties()) {
//                            Map<String, Object> responseMap = new HashMap<>(8);
//                            responseMap.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
//                            String res = JsonUtils.writeJsonStr(responseMap);
//                            String resJson = JsonUtils.validJson(res);
//                            response.put(subModelAttr.getName(),resJson);
//                        }
//                    }else{
//                        Map<String, Object> responseMap = new HashMap<>(8);
//                        for (ModelAttr subModelAttr : modelAttr.getProperties()) {
//                            responseMap.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
//                        }
//                        String res = JsonUtils.writeJsonStr(responseMap);
//                        String resJson = JsonUtils.validJson(res);
//                        response.put("response",resJson);
//                    }
//                }
//            }
//        }
//        //responseMap.get("data").var
//
//        return response;
//    }
//
//    /**
//     * 处理返回（属性列表）
//     *
//     * @param responseObj
//     * @param definitinMap
//     *
//     */
//    private static ModelAttr processResponseModelAttrs(Map<String, Object> responseObj, Map<String, ModelAttr> definitinMap) {
//        Map<String, Object> schema = (Map<String, Object>) responseObj.get("schema");
//        String type = (String) schema.get("type");
//        String ref = null;
//        //数组
//        if ("array".equals(type)) {
//            Map<String, Object> items = (Map<String, Object>) schema.get("items");
//            if (items != null && items.get("$ref") != null) {
//                ref = (String) items.get("$ref");
//            }
//        }
//        //对象
//        if (schema.get("$ref") != null) {
//            ref = (String) schema.get("$ref");
//        }
//
//        //其他类型
//        ModelAttr modelAttr = new ModelAttr();
//        modelAttr.setType(TextUtil.defaultIfBlank(type, ""));
//        if (!TextUtil.isBlank(ref) && definitinMap.get(ref) != null) {
//            modelAttr = definitinMap.get(ref);
//        }
//        return modelAttr;
//    }
//
//    /**
//     * 封装请求体 ： 准备数据： 【请求示例】：
//     *
//     * @param list
//     *
//     */
//    private static Map<String,String> processRequestParam(List<Request> list) throws IOException {
//        Map<String, Object> headerMap = new LinkedHashMap<>();
//        Map<String, Object> queryMap = new LinkedHashMap<>();
//        Map<String, Object> jsonMap = new LinkedHashMap<>();
//        Map<String, Object> pathMap = new LinkedHashMap<>();
//        if (list != null && list.size() > 0) {
//            for (Request request : list) {
//                String name = request.getName();
//                String paramType = request.getParamType();
//                Object value = getValue(request.getType(), request.getModelAttr());
//                switch (paramType) {
//                case "header":
//                    headerMap.put(name, value);
//                    break;
//                case "query":
//                    queryMap.put(name, value);
//                    break;
//                case "body":
//                    //TODO 根据content-type序列化成不同格式，目前只用了json
//                    jsonMap.put(name, value);
//                    break;
//                case "path":
//                    pathMap.put(name,value);
//                    break;
//                default:
//                    break;
//
//                }
//            }
//        }
//        String query = "";
//        String header = "";
//        String path = "";
//        String body = "";
//        if (!queryMap.isEmpty()) {
//            query += getUrlParamsByMap(queryMap);
//        }
//        if (!headerMap.isEmpty()) {
//            header +=  getHeaderByMap(headerMap);
//        }
//        if (!pathMap.isEmpty()) {
//            path +=  getPathByMap(pathMap);
//        }
//        if (!jsonMap.isEmpty()) {
//            if (jsonMap.size() == 1) {
//                for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
//                    body +=  JsonUtils.writeJsonStr(entry.getValue()) ;
//                }
//            } else {
//                body +=  JsonUtils.writeJsonStr(jsonMap) ;
//            }
//        }
//        if(!body.equals("")){
//            body = JsonUtils.validJson(body);
//        }
//        Map<String,String> resMap = new LinkedHashMap<>();
//        //目前没有header这种请求
//        if(!header.equals("")){
//            resMap.put("header",header);
//        }
//        if(!path.equals("")){
//            resMap.put("path",path);
//        }
//        if(!query.equals("")) {
//            resMap.put("query", query);
//        }
//        if(!body.equals(""))
//        {
//            resMap.put("body", body);
//        }
//        return resMap;
//    }
//
//    /**
//     * 路径参数
//     *
//     * @param pathMap
//     * @return
//     */
//    private static String getPathByMap(Map<String, Object> pathMap)
//    {
//        if (pathMap == null ||pathMap.isEmpty()) {
//            return "";
//        }
//        StringBuffer sb = new StringBuffer();
//        sb.append("");
//        for (Map.Entry<String, Object> entry : pathMap.entrySet()) {
//            sb.append(entry.getKey() + "=" + entry.getValue());
//            sb.append(", ");
//        }
//        String s = sb.toString();
//        if (s.endsWith(", ")) {
//            s = TextUtil.substringBeforeLast(s, ", ");
//        }
//        return s;
//    }
//
//    /**
//     * 将map转换成url(query请求方式)
//     */
//    public static String getUrlParamsByMap(Map<String, Object> map) {
//        if (map == null || map.isEmpty()) {
//            return "";
//        }
//        StringBuffer sb = new StringBuffer();
//        sb.append("?");
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            sb.append(entry.getKey() + "=" + entry.getValue());
//            sb.append("&");
//        }
//        String s = sb.toString();
//        if (s.endsWith("&")) {
//            s = TextUtil.substringBeforeLast(s, "&");
//        }
//        return s;
//    }
//
//    /**
//     * 将map转换成header
//     */
//    public static String getHeaderByMap(Map<String, Object> map) {
//        if (map == null || map.isEmpty()) {
//            return "";
//        }
//        StringBuffer sb = new StringBuffer();
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            sb.append("--header '");
//            sb.append(entry.getKey() + ":" + entry.getValue());
//            sb.append("'");
//        }
//        return sb.toString();
//    }
//
//    /**
//     * 例子中，字段的默认值
//     *
//     * @param type      类型
//     * @param modelAttr 引用的类型
//     *
//     */
//    private static Object getValue(String type, ModelAttr modelAttr) {
//        int pos;
//        if ((pos = type.indexOf(":")) != -1) {
//            type = type.substring(0, pos);
//        }
//        switch (type) {
//        case "string":
//            return "string";
//        case "string(date-time)":
//            return "2020/01/01 00:00:00";
//        case "integer":
//        case "integer(int64)":
//        case "integer(int32)":
//            return 0;
//        case "number":
//            return 0.0;
//        case "boolean":
//            return true;
//        case "file":
//            return "(binary)";
//        case "array(integer)":
//            List<Integer> integerArray = new ArrayList();
//            integerArray.add(0);
//            integerArray.add(0);
//            return integerArray;
//        case "array(object)":
//            List<Object> objectArray = new ArrayList();
//            return objectArray;
//        case "array(string)":
//            List<String> strArray = new ArrayList();
//            strArray.add("XX");
//            strArray.add("XX");
//            return strArray;
//        case "array":
//            List list = new ArrayList();
//            Map<String, Object> map = new LinkedHashMap<>();
//            if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
//                for (ModelAttr subModelAttr : modelAttr.getProperties()) {
//                    map.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
//                }
//            }
//            list.add(map);
//            return list;
//        case "object":
//            map = new LinkedHashMap<>();
//            if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
//                for (ModelAttr subModelAttr : modelAttr.getProperties()) {
//                    map.put(subModelAttr.getName(), getValue(subModelAttr.getType(), subModelAttr));
//                }
//            }
//            return map;
//        default:
//            return null;
//        }
//    }
//
//    /**
//     * 处理返回码列表
//     *
//     * @param responses 全部状态码返回对象
//     *
//     */
//    private static List<Response> processResponseCodeList(Map<String, Object> responses) {
//        List<Response> responseList = new ArrayList<>();
//        Iterator<Map.Entry<String, Object>> resIt = responses.entrySet().iterator();
//        while (resIt.hasNext()) {
//            Map.Entry<String, Object> entry = resIt.next();
//            Response response = new Response();
//            // 状态码 200 201 401 403 404 这样
//            response.setName(entry.getKey());
//            LinkedHashMap<String, Object> statusCodeInfo = (LinkedHashMap) entry.getValue();
//            response.setDescription(String.valueOf(statusCodeInfo.get("description")));
//            Object schema = statusCodeInfo.get("schema");
//            if (schema != null) {
//                Object originalRef = ((LinkedHashMap) schema).get("originalRef");
//                response.setRemark(originalRef == null ? "" : originalRef.toString());
//            }
//            responseList.add(response);
//        }
//        return responseList;
//    }
//
//    private static List<Map<String, Object>> addReqParam(Object in, List<ModelAttr> properties) {
//        List<Map<String, Object>> reqSubList = new ArrayList<>();
//        if (properties != null && properties.size() > 0) {
//            for (int j = 0; j < properties.size(); j++) {
//                ModelAttr attr = properties.get(j);
//                Map<String, Object> mreqChild = new HashMap<>();
//                /*  String secondSuffix = TextUtil.addBlank(TextUtil.concat(firstSuffix, j + "."), 2);*/
//                mreqChild.put(DocxConf.PARAM_NAME, attr.getName());
//                mreqChild.put(DocxConf.PARAM_REQ_TYPE, String.valueOf(in));
//                mreqChild.put(DocxConf.PARAM_DATA_TYPE, attr.getType());
//                mreqChild.put(DocxConf.PARAM_REQ_ISFILL, attr.getRequire());
//                mreqChild.put(DocxConf.PARAM_DESC, attr.getDescription() == null ? "" : attr.getDescription());
//                List<ModelAttr> properties1 = attr.getProperties();
//                List<Map<String, Object>> maps = addReqParam(in , properties1);
//                mreqChild.put(DocxConf.PARAM_SUB_LIST,maps);
//                reqSubList.add(mreqChild);
//            }
//        }
//        return reqSubList;
//    }
//
//    private static List<Map<String, Object>>  addResParam(List<ModelAttr> properties) {
//        List<Map<String, Object>> resSubList = new ArrayList<>();
//        if (properties != null && properties.size() > 0) {
//            for (int j = 0; j < properties.size(); j++) {
//                ModelAttr attr = properties.get(j);
//                Map<String, Object> mresChild = new HashMap<>();
//                if(attr.getProperties().size() > 0){
//                    mresChild.put(DocxConf.PARAM_SUB_LIST,attr.getProperties());
//                }else{
//                    mresChild.put(DocxConf.PARAM_SUB_LIST,new ArrayList<>());
//                }
//
//                mresChild.put(DocxConf.PARAM_NAME, attr.getName());
//                mresChild.put(DocxConf.PARAM_DATA_TYPE, attr.getType());
//                mresChild.put(DocxConf.PARAM_DESC, attr.getDescription() == null ? "" : attr.getDescription());
//
//                List<ModelAttr> properties1 = attr.getProperties();
//                List<Map<String, Object>> maps = addResParam( properties1);
//                mresChild.put(DocxConf.PARAM_SUB_LIST,maps);
//                resSubList.add(mresChild);
//            }
//        }
//        return  resSubList;
//    }
//
//    /**
//     * 提取首页信息：swagger的版本，文档描述，版本，标题，联系人名称，地址，邮箱，创建时间
//     * @param map
//     * @return
//     */
//    private static Map<String, String> parseWordIndex(Map<String, Object> map) {
//
//        Map<String, String> mapIndex = new HashMap<>();
//        DocxConf instance = DocxConf.getInstance();
//
//        String swagger = map.get("swagger").toString();
//        Map<String, Object> info = (Map<String, Object>) map.get("info");
//        String description = (String) info.get("description");
//        String version = (String) info.get("version");
//        String title = (String) info.get("title");
//        String name = instance.getName();
//        String url = instance.getUrl();
//        String email = instance.getEmail();
//        String time = DocxConf.getInstance().getDocxTimeValue() == null ? DateUtils.now(null) : DocxConf.getInstance().getDocxTimeValue();
//
//        if(!instance.getVersionNum().equals("")){
//            version = instance.getVersionNum();
//        }
//
//        mapIndex.put(DocxConf.INDEX_TITLE, title);
//        mapIndex.put(DocxConf.INDEX_DESC, description);
//        mapIndex.put(DocxConf.INDEX_VERSIONSWAGGER, swagger);
//        mapIndex.put(DocxConf.INDEX_VERSIONDOCX, version);
//        mapIndex.put(DocxConf.INDEX_NAME, name);
//        mapIndex.put(DocxConf.INDEX_URL, url);
//        mapIndex.put(DocxConf.INDEX_EMAIL, email);
//        mapIndex.put(DocxConf.INDEX_TIME, time);
//
//        return mapIndex;
//    }
//
//    /**
//     * 解析Definition ：这里面主要放各种Model.(VO,DTO,Entity)
//     *
//     * @param map
//     *
//     */
//    private static Map<String, ModelAttr> parseDefinitions(Map<String, Object> map) {
//        Map<String, Map<String, Object>> definitions = (Map<String, Map<String, Object>>) map.get("definitions");
//        Map<String, ModelAttr> definitinMap = new HashMap<>(256);
//        if (definitions != null) {
//            Iterator<String> modelNameIt = definitions.keySet().iterator();
//            while (modelNameIt.hasNext()) {
//                String modeName = modelNameIt.next();
//                getAndPutModelAttr(definitions, definitinMap, modeName);
//            }
//        }
//        return definitinMap;
//    }
//
//
//    /**
//     * 递归生成ModelAttr
//     * 对$ref类型设置具体属性
//     */
//    private static ModelAttr getAndPutModelAttr(Map<String, Map<String, Object>> swaggerMap, Map<String, ModelAttr> resMap, String modeName) {
//        ModelAttr modeAttr;
//        if ((modeAttr = resMap.get("#/definitions/" + modeName)) == null) {
//            modeAttr = new ModelAttr();
//            resMap.put("#/definitions/" + modeName, modeAttr);
//        } else if (modeAttr.isCompleted()) {
//            return resMap.get("#/definitions/" + modeName);
//        }
//        //这里 解析model的属性信息 properties
//        Map<String, Object> modeProperties = (Map<String, Object>) swaggerMap.get(modeName).get("properties");
//        if (modeProperties == null) {
//            return null;
//        }
//        //这里 解析model的属性信息 required ： 必填参数名
//        List<String> requiredProperties = (List<String>) swaggerMap.get(modeName).get("required");
//
//        Iterator<Map.Entry<String, Object>> mIt = modeProperties.entrySet().iterator();
//
//        List<ModelAttr> attrList = new ArrayList<>();
//        //解析 properties属性
//        while (mIt.hasNext()) {
//            Map.Entry<String, Object> mEntry = mIt.next();
//            Map<String, Object> attrInfoMap = (Map<String, Object>) mEntry.getValue();
//            ModelAttr child = new ModelAttr();
//            String name = mEntry.getKey();
//            child.setName(name);
//
//            /**解析model的属性值的 type与format 字段 ----> 设置 响应参数 的 数据类型 **/
//            //(1) 如果属性有type对应的值，直接设置
//            child.setType((String) attrInfoMap.get("type"));
//            //(2) 如果属性没有type，类型设置为object
//            String finalType = "object";
//            if (child.getType() == null || child.getType().equals("")) {
//                child.setType(finalType);
//            }
//            //（3）如果属性除了有type也有format，重新设置类型格式 为integer(int64):  ep："type" ：integer" ,"format": "int64"
//            if (attrInfoMap.get("format") != null) {
//                child.setType(child.getType() + "(" + attrInfoMap.get("format") + ")");
//            }
//            //（4）如果属性是一个model，类型设置为 object:Model的样式
//            //$ref为请求和响应或其他Model里某属性的引用参数，引用的也是一个Model , items是如果是数组类型，他存放的数据可能为model,则下一级的内部元素的属性类型也得标识
//            Object ref = attrInfoMap.get("$ref");
//            Object items = attrInfoMap.get("items");
//            if (ref != null || (items != null && (ref = ((Map) items).get("$ref")) != null)) {
//                String refName = ref.toString();
//                //截取 #/definitions/ 后面的
//                String clsName = refName.substring(14);
//                modeAttr.setCompleted(true);
//                ModelAttr refModel = getAndPutModelAttr(swaggerMap, resMap, clsName);
//                if (refModel != null)
//                {
//                    child.setProperties(refModel.getProperties());
//                }
//                child.setType(child.getType() + ":" + clsName);
//            }else if (items != null && (ref = ((Map) items).get("$ref")) == null){
//                child.setType(child.getType() +  "(" + ((Map) items).get("type") + ")");
//            }
//            child.setDescription((String) attrInfoMap.get("description"));
//            if(!CollectionUtils.isEmpty(requiredProperties) && requiredProperties.contains(name)){
//                child.setRequire(true);
//            }
//            attrList.add(child);
//        }
//        Object title = swaggerMap.get(modeName).get("title");
//        Object description = swaggerMap.get(modeName).get("description");
//        Object type = swaggerMap.get(modeName).get("type");
//        modeAttr.setClassName(title == null ? "" : title.toString());
//        modeAttr.setType(type == null ? "" : type.toString());
//        modeAttr.setDescription(description == null ? "" : description.toString());
//        modeAttr.setProperties(attrList);
//        return modeAttr;
//    }
//
//    /**
//     * 获得文档所有标题
//     * @return
//     */
//    public static List<PdfBookModel> getAllTitle(Document originDoc){
//
//        //用来存放所有标题，一级标题集合
//        List<PdfBookModel> data = new ArrayList<>();
//        try
//        {
//
//            // 后面需要用这个对象去获取当前段落所在的页码
//            LayoutCollector layoutCollector = new LayoutCollector(originDoc);
//            // 需要获取所有的section，要不然部分word提取目录不完整
//            Section[] sections = originDoc.getSections().toArray();
//            // 只获得正文的标题
//            List<Paragraph> paragraphs = Arrays.asList(sections[1].getBody().getParagraphs().toArray());
//
//            for (Paragraph p : paragraphs)
//            {
//                /** 层级 **/
//                int level = p.getParagraphFormat().getOutlineLevel();
//                if (level < 0 || level > 1) {
//                    continue;
//                }
//
//                /** 页码*/
//                int pageIndex = layoutCollector.getEndPageIndex(p);
//
//                /** 标题内容 **/
//                String text = "";
//                for (Run run : p.getRuns())
//                {
//                    text += run.getText();
//                }
//                if (text == "")
//                {
//                    continue;
//                }
//
//                /** 装填标签Model **/
//                PdfBookModel model = new PdfBookModel();
//                model.setLevel(level)  ;
//                model.setTitle( text ) ;
//                model.setPage(pageIndex);
//
//                /** 装填标签Model **/
//                if (level == 0)
//                {
//                    data.add(model);
//                }
//                else
//                {
//                    getChildTitle(data.get(data.size() - 1), model);
//                }
//            }
//
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        return data;
//    }
//
//    private static void getChildTitle(PdfBookModel parent, PdfBookModel child)
//    {
//        if (parent.getLevel() + 1 == child.getLevel())
//        {
//            parent.getChild().add(child);
//        }
//        else
//        {
//            getChildTitle(parent.getChild().get(parent.getChild().size() - 1) ,child);
//        }
//    }
//
//
//}
