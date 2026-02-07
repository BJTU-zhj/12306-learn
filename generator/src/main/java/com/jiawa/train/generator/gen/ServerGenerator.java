package com.jiawa.train.generator.gen;

import com.jiawa.train.generator.util.FreemarkerUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ServerGenerator {

    static String servicePath = "[module-all]\\src\\main\\java\\com\\jiawa\\train\\[module]\\service\\";
    static String pomPath = "generator\\pom.xml";
    static {
        new File(servicePath).mkdirs();
    }

    public static void main(String [] args) throws Exception {
       String generatorPath =getGeneratorPath();

       //获取模块名字
        String moduleName = generatorPath.replace("src/main/resources/generator-config-","");
        moduleName=moduleName.replace(".xml","");
        System.out.println("模块名字："+moduleName);
        servicePath = servicePath.replace("[module-all]","train-"+moduleName);
        servicePath = servicePath.replace("[module]",moduleName);
        System.out.println("servicePath:"+servicePath);

        // 读取table节点
        Document document = new SAXReader().read("generator/" + generatorPath);
        Node table = document.selectSingleNode("//table");
        System.out.println(table);
        Node tableName = table.selectSingleNode("@tableName");
        Node domainObjectName = table.selectSingleNode("@domainObjectName");
        System.out.println(tableName.getText() + "/" + domainObjectName.getText());

        // 示例：表名 jiawa_test
        // Domain = JiawaTest
        String Domain = domainObjectName.getText();
        // domain = jiawaTest
        String domain = Domain.substring(0, 1).toLowerCase() + Domain.substring(1);
        // do_main = jiawa-test
        String do_main = tableName.getText().replaceAll("_", "-");

        //组装参数
        Map<String,Object> map=new HashMap<>();
        map.put("Domain",Domain);
        map.put("domain",domain);
        map.put("do_main",do_main);
        System.out.printf("组装参数:"+ map.toString());

        FreemarkerUtil.initConfig("service.ftl");
        FreemarkerUtil.generator(servicePath+Domain+"Service.java",map);
    }

    //读取pom文件内的configurationFile节点获取生成器的xml文件位置
    private static String getGeneratorPath() throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Map<String, String> map=new HashMap<>();
        map.put("pom","http://maven.apache.org/POM/4.0.0");
        saxReader.getDocumentFactory().setXPathNamespaceURIs(map);
        Document document = saxReader.read(pomPath);
        Node node = document.selectSingleNode("//pom:configurationFile");
        System.out.println(node.getText());
        return node.getText();
    }


}
