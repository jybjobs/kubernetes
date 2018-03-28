package com.k8s.api.demo.utils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Set;

public class YamlUtils {

    public static <T> Object parseYamlFile(String path,Class<T> type) throws Exception{
        Yaml yaml = new Yaml();
        T object= yaml.loadAs(new FileInputStream(path), type);
        return object;
    }

    public static <T> Object parseYamlContent(String yamlContent,Class<T> type) throws Exception{
        Yaml yaml = new Yaml();
        T object= yaml.loadAs(yamlContent, type);
        return object;
    }

    public static String parseObjToYaml(Object obj){
        Yaml yaml = new Yaml();
        return yaml.dump(obj);
    }

    public static String parseMapToYaml(HashMap obj){
        Yaml yaml = new Yaml();
        return yaml.dumpAsMap(obj);
    }
    private static Log logger = LogFactory.getLog(YamlUtils.class);

    protected static ObjectMapper YAML_MAPPER = null;

    protected static ObjectMapper YAML_ROS_MAPPER = null;

    public static ObjectMapper getRosMapper(){
        if(YAML_ROS_MAPPER==null){
            YAML_ROS_MAPPER = new ObjectMapper(new YAMLFactory());
            YAML_ROS_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            YAML_ROS_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        }
        return YAML_ROS_MAPPER;
    }

    public static ObjectMapper getMapper(){
        if(YAML_MAPPER==null){
            YAML_MAPPER = new ObjectMapper(new YAMLFactory());
            YAML_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        }
        return YAML_MAPPER;
    }

    public static <T> T toObject(String content, Class<T> clazz,boolean validate) {
        if(StringUtils.isEmpty(content)){
            return null;
        }
        T result = null;
        try {
            result = getMapper().readValue(new StringReader(content), clazz);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            System.out.println("转换成"+clazz.getName()+"对象错误");
        }
//		Yaml yaml = new Yaml();
//		T result = (T) yaml.loadAs(content,clazz);

        if(validate){
            ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
            Validator validator = vf.getValidator();
            Set<ConstraintViolation<T>> set = validator.validate(result);
            StringBuffer sb = new StringBuffer();
            boolean hasError = false;
            for (ConstraintViolation<T> constraintViolation : set) {
                sb.append(constraintViolation.getPropertyPath()+":"+constraintViolation.getMessage());
                sb.append(";");
                hasError = true;
            }
            if(hasError){
                System.out.println(sb.toString());
            }
        }

        return result;
    }

    public static <T> T toObject(String content, Class<T> clazz) {
        return toObject(content,clazz,true);
    }

    public static String to2StringRemoveNull(Object o) {
        try {
            return getRosMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            System.out.println(o.getClass().getName()+"转换成yaml失败");
        }
        return null;
    }

    public static String to2String(Object o) {
//		Yaml yaml = new Yaml();
//		return yaml.dump(o);
        try {
            return getMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            System.out.println(o.getClass().getName()+"转换成yaml失败");
        }
        return null;
    }
    public static void main(String[] args) throws Exception{


        JSONObject map= (JSONObject)YamlUtils.parseYamlFile("D:\\file\\test.yaml",JSONObject.class);
        System.out.println(map.toJSONString());
    }
}
