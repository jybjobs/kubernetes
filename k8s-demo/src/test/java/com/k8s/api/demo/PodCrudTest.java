package com.k8s.api.demo;

import com.k8s.api.demo.utils.YamlUtils;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import static org.junit.Assert.assertNotNull;


public class PodCrudTest {

  public static void main(String[] args) {
    //创建访问客户端
    Config config = new ConfigBuilder().withMasterUrl("https://192.168.70.138:6443/")
            .withUsername("admin").withPassword("123456")
            //    .withNamespace("demo")
            .withTrustCerts(true).build();
    KubernetesClient client = new DefaultKubernetesClient(config);
    Pod pod1 = new PodBuilder().withNewMetadata().withName("pod2").addToLabels("testKey", "testValue").endMetadata()
            .withNewSpec()
               .addNewVolume()
                  .withName("app-pv")
                  .withNewPersistentVolumeClaim().withClaimName("task-pv-claim").endPersistentVolumeClaim()//挂载pvc
               .endVolume()
               .addNewContainer()
                  .withName("app")
                  .withImage("tomcat")
                  .addNewPort()
                       .withName("port0").withProtocol("TCP").withContainerPort(8080)
                  .endPort()
                  .addNewVolumeMount()
                       .withName("app-pv")
                       .withMountPath("/usr/local/tomcat/webapps")
                  .endVolumeMount()
               .endContainer()
            .endSpec()
            .build();


    client.pods().inNamespace("demo").create(pod1);//创建pod

//    PodList podList = client.pods().list();
//    assertNotNull(podList);
    /**
     *  update
      */
//   Pod pod = client.pods().inNamespace("demo").withName("pod1").edit()
//            .editMetadata().addToLabels("app", "tomcat01").endMetadata()
//            .done();
//    assertNotNull(pod);


    PodList podList = client.pods().inNamespace("demo").list();
    assertNotNull(podList);
    for(Pod p : podList.getItems()){
      String str = YamlUtils.parseObjToYaml(p);
      System.out.println(str);
    }
    client.pods().inNamespace("demo").withName("pod1").delete();

    // test listing with labels
//    podList = client.pods().inAnyNamespace().withLabels(Collections.singletonMap("testKey", "testValue")).list();
//    assertNotNull(podList);
//    assertEquals(2, podList.getItems().size());


  }
}