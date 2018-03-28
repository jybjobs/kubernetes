package com.k8s.api.demo;

import com.k8s.api.demo.utils.YamlUtils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.HashMap;
import java.util.Map;

/**
 * PersistentVolume
 * persistentVolumeClaims
 * Created by JYB on 2018/3/27.
 */
public class StorageTest {
    public static void main(String[] args) {
        //创建访问客户端
        Config config = new ConfigBuilder().withMasterUrl("https://192.168.70.138:6443/")
                .withUsername("admin").withPassword("123456")
                //    .withNamespace("demo")
                .withTrustCerts(true).build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        /**
         * 创建pv
         * kind: PersistentVolume
           apiVersion: v1
           metadata:
             name: task-pv-volume
             namespace: demo
             labels:
               type: local
           spec:
             storageClassName: manual
             capacity:
                storage: 1Gi
             accessModes:
                - ReadWriteOnce
             hostPath:
                path: "/tmp/data"

         */
        PersistentVolume persistentVolume = new PersistentVolumeBuilder()
                .withNewMetadata()
                    .withName("app-pv")
                    .withNamespace("demo")
                    .addToLabels("type","local")
                .endMetadata()
                .withNewSpec()
                .addToCapacity("storage",new Quantity("1Gi"))
                .withAccessModes("ReadWriteOnce")
                .withNewHostPath().withPath("/tmp/data").endHostPath()
                .endSpec().build();
        client.persistentVolumes().create(persistentVolume);//创建pv

        PersistentVolume pv = client.persistentVolumes().withName("app-pv").get();
        String str = YamlUtils.parseObjToYaml(pv);
        System.out.println(str);
        Map<String,Quantity> requests = new HashMap<>();
        requests.put("storage",new Quantity("1Gi"));
        PersistentVolumeClaim persistentVolumeClaim = new PersistentVolumeClaimBuilder()
                .withNewMetadata().withName("app-pv-claim").withNamespace("demo").endMetadata()
                .withNewSpec()
                    .withAccessModes("ReadWriteOnce")
                    .withNewResources()
                         .withRequests(requests)
                    .endResources()
                .endSpec()
                .build();
        client.persistentVolumeClaims().inNamespace("demo").create(persistentVolumeClaim);
        PersistentVolumeClaim pvc = client.persistentVolumeClaims().inNamespace("demo").withName("app-pv-claim").get();
        str = YamlUtils.parseObjToYaml(pv);
        System.out.println(str);


    }
}
