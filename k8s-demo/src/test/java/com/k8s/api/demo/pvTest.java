package com.k8s.api.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class pvTest {
     private static final Logger logger = LoggerFactory.getLogger(pvTest.class);
     
      public static void main(String[] args){
          KubernetesClient kubernetesClient= connectK8s();
          //创建pv
          //testCreatePv_Nfs(kubernetesClient);
          //testCreatePv_Rbd(kubernetesClient);
          //删除指定的pv          //删除指定的pv
          //testDeletePv(kubernetesClient, "hzbtestpv");
          //获取pv列表
          //testPvList(kubernetesClient);
          //更新pv
          //testUpdatePv(kubernetesClient,"hzbtestpv");
          //创建pvc
          testCreatePvc(kubernetesClient);
          //删除指定的pvc
         // testDeletePvc(kubernetesClient, "hzbtestpvc");
          //获取pvc列表
         // testPvcList(kubernetesClient);
          //更新指定的PVC
          //testUpdatePvc(kubernetesClient,"hzbtestpvc");
          //System.out.println(System.nanoTime());
          //扩容
          //expanseStorage(kubernetesClient,"hzbtestpv","15Gi");
      }
      
      /**
       * 获取pv列表
       * @param kubernetesClient
       */
      public static void  testPvList(KubernetesClient kubernetesClient){
          if(kubernetesClient!=null){
              PersistentVolumeList pVolumeList=kubernetesClient.persistentVolumes().list();     
              List<PersistentVolume> pvList=pVolumeList.getItems();
              for( PersistentVolume pv:pvList){    
                    System.out.println("显示一个pv信息===============================================");    
                    System.out.println("========="+jsonFormatter(pv));        
              }
          }
      }
      
      /**
       * 创建pv信息，挂载nfs存储
       * @param kubernetesClient
       */
      public static void  testCreatePv_Nfs(KubernetesClient kubernetesClient){
          PersistentVolume pv=new PersistentVolume();
          pv.setApiVersion("v1");
          pv.setKind("PersistentVolume");
         
          ObjectMeta meta=new ObjectMeta();
          meta.setName("hzbtestpv");
          Map<String, String> labelsMap=new HashMap<String, String>();
          labelsMap.put("app", "hzbtestpv-lb");
          meta.setLabels(labelsMap);
          //设置pv的metadata
          pv.setMetadata(meta);
          
          PersistentVolumeSpec pvs=new PersistentVolumeSpec();
          Map<String, Quantity> capacityMap=new HashMap<String, Quantity>();
          Quantity quantity=new Quantity();
          quantity.setAmount("5Gi");
          capacityMap.put("storage", quantity);
          //设置Spec的capacity
          pvs.setCapacity(capacityMap);
          
          List<String> accessModes=new ArrayList<String>();
          accessModes.add("ReadWriteOnce");
        //设置Spec的accessModes
          pvs.setAccessModes(accessModes);
          //设置Spec的回收pvc的回收策略。
          pvs.setPersistentVolumeReclaimPolicy("Recycle");         
          NFSVolumeSource nfsVolumeSource=new NFSVolumeSource();
          nfsVolumeSource.setServer("172.16.101.189");
          nfsVolumeSource.setPath("/srv/nfs/hzb/test");
          //设置Spec的nfs
          pvs.setNfs(nfsVolumeSource);          
          //设置pv的Spec
          pv.setSpec(pvs);
          try {
              //将pv信息存储到服务端
              kubernetesClient.persistentVolumes().create(pv);
              System.out.println("创建pv成功");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }                
      }
      
      /**
       * 创建pv信息，挂载Rbd存储
       * @param kubernetesClient
       */
      public static void  testCreatePv_Rbd(KubernetesClient kubernetesClient){
          PersistentVolume pv=new PersistentVolume();
          pv.setApiVersion("v1");
          pv.setKind("PersistentVolume");
         
          ObjectMeta meta=new ObjectMeta();
          meta.setName("mysql-hzb-pv");
          //设置pv的metadata
          pv.setMetadata(meta);
          
          PersistentVolumeSpec pvs=new PersistentVolumeSpec();
          Map<String, Quantity> capacityMap=new HashMap<String, Quantity>();
          Quantity quantity=new Quantity();
          quantity.setAmount("2Gi");
          capacityMap.put("storage", quantity);
          //设置Spec的capacity
          pvs.setCapacity(capacityMap);
          
          List<String> accessModes=new ArrayList<String>();
          accessModes.add("ReadWriteOnce");
        //设置Spec的accessModes
          pvs.setAccessModes(accessModes);
          //设置Spec的回收pvc的回收策略。
          pvs.setPersistentVolumeReclaimPolicy("Recycle");        
          
          RBDVolumeSource rbdVolumeSource=new RBDVolumeSource();
          //设置ceph的monitors
          rbdVolumeSource.setMonitors(Arrays.asList("172.16.60.41:6789", "172.16.60.42:6789", "172.16.60.43:6789"));
          //设置ceph使用的存储池，ceph中默认是rbd
          rbdVolumeSource.setPool("rbd");
          //设置该pv要用的ceph的image
          rbdVolumeSource.setImage("hzb-mysql");
          //设置连接ceph的用户
          rbdVolumeSource.setUser("admin");
          //设置admin用户的认证信息
          rbdVolumeSource.setKeyring("/etc/ceph/ceph.client.admin.keyring");
          //设置要以哪种格式来格式化image
          rbdVolumeSource.setFsType("xfs");
          //设置image的读写权限
          rbdVolumeSource.setReadOnly(false);
          pvs.setRbd(rbdVolumeSource);
          
          //设置pv的Spec
          pv.setSpec(pvs);
          try {
              //将pv信息存储到服务端
              kubernetesClient.persistentVolumes().create(pv);
              System.out.println("创建挂载rbd的pv成功");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }                
      }
      
      /**
       * 删除指定的pv信息
       * @param kubernetesClient
       * @param pvName
       */
      public static void testDeletePv(KubernetesClient kubernetesClient,String pvName){
          try {
              kubernetesClient.persistentVolumes().withName(pvName).delete();
              System.out.println("成功删除pv========"+pvName);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }  
      }
      
      /**
       * 更新指定的pv信息
       * @param kubernetesClient
       * @param pvName
       */
      public static void testUpdatePv(KubernetesClient kubernetesClient,String pvName){
          try {
              PersistentVolume pv=kubernetesClient.persistentVolumes().withName(pvName).get();
              System.out.println("更新前的pv信息===============================================");    
              System.out.println("========="+jsonFormatter(pv));    
              
              pv.getMetadata().getLabels().put("app", "hzbtestpv-new2");
              pv.getStatus().setPhase("Available");
//              kubernetesClient.persistentVolumes().withName(pvName).update(pv);
              
              PersistentVolume pvnew=kubernetesClient.persistentVolumes().withName(pvName).get();
              System.out.println("更新后的pv信息===============================================");    
              System.out.println("========="+jsonFormatter(pvnew));    
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }  
      }
      
      /**
       * 扩容pv
       * @param kubernetesClient
       * @param pvName 要扩容的pv名字。
       * @param quantityAmount 扩容的规格
       */
      public static void expanseStorage(KubernetesClient kubernetesClient,String pvName,String quantityAmount){
          PersistentVolume pv=kubernetesClient.persistentVolumes().withName(pvName).get();
          System.out.println("扩容前的pv信息===============================================");    
          System.out.println("========="+jsonFormatter(pv));    
          Quantity newquantity=new Quantity();
          newquantity.setAmount(quantityAmount);
          pv.getSpec().getCapacity().put("storage",newquantity);
//          kubernetesClient.persistentVolumes().withName(pvName).update(pv);
          PersistentVolume pvnew=kubernetesClient.persistentVolumes().withName(pvName).get();
          System.out.println("扩容后的pv信息===============================================");    
          System.out.println("========="+jsonFormatter(pvnew));    
      }
      
      /**
       * 获取pvc列表
       * @param kubernetesClient
       */
      public static void  testPvcList(KubernetesClient kubernetesClient){
          if(kubernetesClient!=null){
              PersistentVolumeClaimList pVolumeClaimList=kubernetesClient.persistentVolumeClaims().list();     
              List<PersistentVolumeClaim> pvcList=pVolumeClaimList.getItems();
              for( PersistentVolumeClaim pvc:pvcList){    
                    System.out.println("显示一个pvc信息===============================================");    
                    System.out.println("========="+jsonFormatter(pvc));        
              }
          }
      }
      
      /**
       * 创建pvc信息
       * @param kubernetesClient
       */
      public static void testCreatePvc(KubernetesClient kubernetesClient){
          PersistentVolumeClaim pvc=new PersistentVolumeClaim();
          pvc.setApiVersion("v1");
          pvc.setKind("PersistentVolumeClaim");
         
          ObjectMeta meta=new ObjectMeta();
          meta.setName("mysql-hzb-pvc");
          //设置pvc的metadata
          pvc.setMetadata(meta);
          
          PersistentVolumeClaimSpec pvcs=new PersistentVolumeClaimSpec();
  
          List<String> accessModes=new ArrayList<String>();
          accessModes.add("ReadWriteOnce");
        //设置Spec的accessModes
          pvcs.setAccessModes(accessModes);
          //设置Spec绑定的pv
          pvcs.setVolumeName("mysql-hzb-pv");
          
          ResourceRequirements resources=new ResourceRequirements();
          Map<String, Quantity> requests=new HashMap<String, Quantity>();
          Quantity quantity=new Quantity();
          quantity.setAmount("2Gi");
          requests.put("storage", quantity);
          resources.setRequests(requests);
          //设置Spec的Resources
          pvcs.setResources(resources);
          
          LabelSelector labelSelector=new LabelSelector();
          Map<String,String> matchLabels=new HashMap<String, String>();
          matchLabels.put("app", "mysql-hzb-pvc-lbl");
          labelSelector.setMatchLabels(matchLabels);
          //设置Spec的Selector
          pvcs.setSelector(labelSelector);
          
          //设置pvc的Spec
          pvc.setSpec(pvcs);
          try {
              //将pvc信息存储到服务端
              kubernetesClient.persistentVolumeClaims().create(pvc);
              System.out.println("创建pvc成功");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }                
      }
        
      /**
       * 删除指定的pvc信息
       * @param kubernetesClient
       * @param pvcName
       */
      public static void testDeletePvc(KubernetesClient kubernetesClient,String pvcName){
          try {
              kubernetesClient.persistentVolumeClaims().withName(pvcName).delete();
              System.out.println("成功删除pvc========"+pvcName);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }  
      }
      
      /**
       * 更新指定的pvc信息
       * @param kubernetesClient
       * @param pvcName
       */
      public static void testUpdatePvc(KubernetesClient kubernetesClient,String pvcName){
          try {
              PersistentVolumeClaim pvc=kubernetesClient.persistentVolumeClaims().withName(pvcName).get();
              System.out.println("更新前的pvc信息===============================================");    
              System.out.println("========="+jsonFormatter(pvc));    
              
             //pvc.getSpec().getAdditionalProperties().put("appName", "宠物商店");
              pvc.getSpec().getSelector().getMatchLabels().put("appName", "pet-shop");
//              kubernetesClient.persistentVolumeClaims().withName(pvcName).update(pvc);
              
              PersistentVolumeClaim pvcnew=kubernetesClient.persistentVolumeClaims().withName(pvcName).get();
              System.out.println("更新后的pvc信息===============================================");    
              System.out.println("========="+jsonFormatter(pvcnew));    
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }  
      }
      
      /**
       * 连接k8s master服务器
       * @return
       */
      public static KubernetesClient connectK8s(){
            String namespace = "default";
            String master = "http://172.16.70.73:8080/";
            KubernetesClient client=null;
            Config config = new ConfigBuilder().withMasterUrl(master)
                    .withTrustCerts(true)
                    .withNamespace(namespace).build();
            try {
                    client = new DefaultKubernetesClient(config);
                
            }catch (Exception e) {
                   logger.error(e.getMessage(), e);
            }
            return client;
      }
      
         /**
          * 格式化json
          * @param
          * @return
          */
         public static String jsonFormatter(Object uglyJSON){
             if(uglyJSON == null ){
                 return "";
             }
             Gson gson = new GsonBuilder().setPrettyPrinting().create();
             JsonParser jp = new JsonParser();
             String prettyJsonString = gson.toJson(uglyJSON);
             return prettyJsonString;
         }
      
}