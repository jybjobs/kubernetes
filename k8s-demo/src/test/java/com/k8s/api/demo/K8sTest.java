package com.k8s.api.demo;

import com.k8s.api.demo.utils.YamlUtils;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;

/**
 * Created by JYB on 2018/3/21.
 */
public class K8sTest {
    public static void main(String[] args) {
        //创建访问客户端
        Config config = new ConfigBuilder().withMasterUrl("https://192.168.70.138:6443/")
                .withUsername("admin").withPassword("123456")
            //    .withNamespace("demo")
                .withTrustCerts(true).build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        //查询所有的namespaces列表
        List<Namespace> nameSpaceList =client.namespaces().list().getItems();
        for(Namespace ns: nameSpaceList){
            System.out.println("namespace >"+ns.getMetadata().getName());//输出所有的namespace name
        }
//        String str = YamlUtils.parseObjToYaml(nameSpaceList);
//        System.out.println(str);
        // 查询所有的service
        List<Service> services = client.services()
                .inNamespace("demo")
                .list().getItems();
        for(Service s:services){
            System.out.println("service >"+s.getMetadata().getName());
        }

        /**
         * edit
         */
//        Namespace myns = client.namespaces().withName("demo").edit()
//                .editMetadata()
//                .addToLabels("editnamespace", "label")
//                .endMetadata()
//                .done();
        Service myservice = client.services().inNamespace("demo").withName("myservice").edit()
//                .editMetadata()
//                .addToLabels("editservice", "label")
//                .removeFromLabels("another")
//                .endMetadata()
                .editSpec().addToSelector("app","tomcat01") //service 根据selecter 来 查询后端pod
                    .editPort(0).withPort(8080).editOrNewTargetPort().withIntVal(8080).endTargetPort().endPort()
                .endSpec()
                .done();

        /**
         * get a resource
         */
        // select namespace
        Namespace demons = client.namespaces().withName("demo").get();
        String str = YamlUtils.parseObjToYaml(demons);
        System.out.println(str);
        // select Service
        Service demoService = client.services().inNamespace("demo").withName("myservice").get();
        str = YamlUtils.parseObjToYaml(demoService);
        System.out.println(str);
        /**
         * create
         */
        try{
              //创建namespace
//            Namespace myns = client.namespaces().createNew()
//                    .withNewMetadata()
//                    .withName("demo")
//                    .addToLabels("a", "label")
//                    .endMetadata()
//                    .done();
            //创建service
//            Service myservice = client.services().inNamespace("demo").createNew()
//                    .withNewMetadata()
//                    .withName("myservice2").addToLabels("another", "label")
//                    .endMetadata()
//                    .withNewSpec()
//                    .withType("NodePort")
//                    .addNewPort().withName("port0").withProtocol("TCP").withPort(8000).endPort()
//                    .endSpec()
//                    .done();
            //创建pod

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        /**
         * //delete
         */
//        Namespace testns = client.namespaces().withName("").delete();
//        Boolean s = client.services().inNamespace("demo").withName("myservice2").delete();
//        if(s) System.out.println("删除成功！！！");
    }
}
