
# k8s 手动安装文档

## 安装方式选择

自从kubernetes(k8s)出现以来，安装复杂、部署困难就一直被业内吐槽，同时也把很多初学者拒之门外。虽然官方也有专门用来入门的单机部署方案：Minikube，和用来搭建集群的Kubeadm，但国内网络环境不翻墙情况下基本无法使用。所以社区也提供了很多部署k8s的文档或项目，像使用ansible脚本方式的kubeasz，在github上已经有600多star。
不管是官方的还是社区的方案，总体上都离不开下面这几种方式：

1. 使用现成的二进制文件
直接从官方或其他第三方下载，就是k8s各个组件的可执行文件。拿来就可以直接运行了。不管是centos，ubuntu还是其他的linux发行版本，只要gcc编译环境没有太大的区别就可以直接运行的。使用较新的系统一般不会有什么跨平台的问题。

2. 使用源码编译安装
编译结果也是各个组件的二进制文件，所以如果能直接下载到需要的二进制文件基本没有什么编译的必要性了。

3. 使用镜像的方式运行
同样一个功能使用二进制文件提供的服务，也可以选择使用镜像的方式。就像nginx，像mysql，我们可以使用安装版，搞一个可执行文件运行起来，也可以使用它们的镜像运行起来，提供同样的服务。k8s也是一样的道理，二进制文件提供的服务镜像也一样可以提供。


从上面的三种方式中其实使用镜像是比较优雅的方案，容器的好处自然不用多说。但从初学者的角度来说容器的方案会显得有些复杂，不那么纯粹，会有很多容器的配置文件以及关于类似二进制文件提供的服务如何在容器中提供的问题，容易跑偏。所以二进制的方式更适合初学者。


## 环境准备

#### 1. 初始化（安装worker 节点不需要重新生成证书）

````
  1) 下载安装文件
    https://kubernetes.io/docs/setup/building-from-source/

  2) 修改内核
    cat <<EOF>/etc/sysctl.d/88-k8s.conf
    net.bridge.bridge-nf-call-iptables=1
    net.bridge.bridge-nf-call-ip6tables=1
    net.ipv4.ip_forward=1
    EOF
  3) 生成根证书(详见安装脚本)
    
  4） 生成配置（详见安装脚本）

````
#### 2. 安装docker
> 详见 https://docs.docker.com/install/linux/docker-ce/centos/

#### 3. 安装 kubectl kubelet calicoctl
> /bin/cp -f bin/* /usr/bin/
> 添加配置文件

#### 4. 安装 kubelet
```
cat <<EOF > /lib/systemd/system/kubelet.service
[Unit]
Description=Kubernetes Kubelet Server
Documentation=http://kubernetes.io/docs/
After=docker.service
Wants=docker.service

[Service]
EnvironmentFile=-/etc/kubernetes/kubelet.env
ExecStartPre=-/etc/kubernetes/config.sh
ExecStart=/usr/bin/kubelet $online\\
  --allow-privileged=true \\
  --pod-manifest-path=/etc/kubernetes/manifests \\
  --resolv-conf=/etc/resolv.conf \\
  --kube-reserved cpu=100m,memory=512M \\
  --node-status-update-frequency=10s \\
  --enforce-node-allocatable= \\
  --pod-manifest-path=/etc/kubernetes/manifests \\
  --pod-infra-container-image=docker.xxx.com/google_containers/pause-amd64:3.0 \\
  \$NODE_HOSTNAME \\
  \$KUBELET_ARGS
Restart=always
RestartSec=10s
StartLimitInterval=0

[Install]
WantedBy=multi-user.target
EOF
  cp -f bin/* /usr/bin/ # kube* calico*
  reload kubelet
  ```

  #### 5. 安装etcd(woker节点不需要安装)
```
sudo yum install -y --nogpgcheck https://dl.xxx.com/install/rpms/etcd-3.2.5-1.el7.x86_64.rpm
  cat <<EOF > /lib/systemd/system/etcd.service
[Unit]
Description=Etcd Server
After=network.target
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
WorkingDirectory=/data/etcd/
EnvironmentFile=-/etc/etcd/etcd.conf
# User=etcd
# set GOMAXPROCS to number of processors
ExecStart=/bin/bash -c "GOMAXPROCS=\$(nproc) /usr/bin/etcd --name=\"\${ETCD_NAME}\" --data-dir=\"\${ETCD_DATA_DIR}\" --listen-client-urls=\"\${ETCD_LISTEN_CLIENT_URLS}\""
Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF

  mkdir -p /data/etcd
  sed -i "s/localhost:2379/0.0.0.0:2379/g" /etc/etcd/etcd.conf
  sed -i "s/var\/lib/data/g" /etc/etcd/etcd.conf
  reload etcd
  ```
#### 6. 安装 k8s master（worker节点不需要安装）

```
cat <<EOF > /$KUBE_CONFIG_DIR/manifests/master.yaml
apiVersion: v1
kind: Pod
metadata:
  name: kube-master
  namespace: kube-system
  labels:
    component: kube-apiserver
spec:
  hostNetwork: true
  containers:
  - name: kube-apiserver
    image: docker.xxx.com/google_containers/kube-apiserver-amd64:v1.6.10
    command:
    - kube-apiserver
    # - --v=4
    - --insecure-port=8080
    - --insecure-bind-address=127.0.0.1
    - --secure-port=6443
    - --advertise-address=$master
    - --bind-address=$master
    - --tls-private-key-file=/etc/kubernetes/ssl/kubernetes-key.pem
    - --tls-cert-file=/etc/kubernetes/ssl/kubernetes.pem
    - --client-ca-file=/etc/kubernetes/ssl/ca.pem
    - --service-account-key-file=/etc/kubernetes/ssl/ca-key.pem
    - --authorization-mode=AlwaysAllow
    - --anonymous-auth=false
    - --basic-auth-file=/etc/kubernetes/ssl/user.csv
    - --kubelet-https=true
    - --experimental-bootstrap-token-auth
    - --token-auth-file=/etc/kubernetes/ssl/token.csv
    - --service-node-port-range=20000-40000
    - --admission-control=NamespaceLifecycle,NamespaceExists,LimitRanger,ServiceAccount,DefaultStorageClass,ResourceQuota
    - --storage-backend=etcd3
    - --etcd-servers=http://$master:2379
    - --allow-privileged=true
    - --service-cluster-ip-range=10.96.0.0/16
    livenessProbe:
      failureThreshold: 8
      httpGet:
        host: 127.0.0.1
        path: /healthz
        port: 8080
        scheme: HTTP
      initialDelaySeconds: 15
      timeoutSeconds: 15
    resources:
      requests:
        cpu: 250m
    volumeMounts:
    - mountPath: /etc/kubernetes/
      name: k8s
      readOnly: true
  - name: kube-scheduler
    image: docker.xxx.com/google_containers/kube-scheduler-amd64:v1.6.10
    command:
    - kube-scheduler
    - --leader-elect=true
    - --master=http://127.0.0.1:8080
    livenessProbe:
      failureThreshold: 8
      httpGet:
        host: 127.0.0.1
        path: /healthz
        port: 10251
        scheme: HTTP
      initialDelaySeconds: 15
      timeoutSeconds: 15
    resources:
      requests:
        cpu: 100m
  - name: kube-controller-manager
    image: docker.xxx.com/google_containers/kube-controller-manager-amd64:v1.6.10
    command:
    - kube-controller-manager
    - --leader-elect=true
    - --cluster-signing-cert-file=/etc/kubernetes/ssl/ca.pem
    - --cluster-signing-key-file=/etc/kubernetes/ssl/ca-key.pem
    - --service-account-private-key-file=/etc/kubernetes/ssl/ca-key.pem
    - --root-ca-file=/etc/kubernetes/ssl/ca.pem
    - --master=http://127.0.0.1:8080
    #- --allocate-node-cidrs=true
    #- --cluster-cidr=10.50.0.0/16
    livenessProbe:
      failureThreshold: 8
      httpGet:
        host: 127.0.0.1
        path: /healthz
        port: 10252
        scheme: HTTP
      initialDelaySeconds: 15
      timeoutSeconds: 15
    resources:
      requests:
        cpu: 200m
    volumeMounts:
    - mountPath: /etc/kubernetes/
      name: k8s
      readOnly: true
  volumes:
  - hostPath:
      path: /etc/kubernetes
    name: k8s
EOF
  reload kubelet
```

#### 7. 添加 addons





