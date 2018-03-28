#!/bin/bash

# 安装和启动kubelet
# 2018-02-28
# todo: 需要完成master安装后，自动安装addons

basedir="/tmp/kubelet"

bin_url="https://dl.yihecloud.com/install/develop/ops"
#bin_url="https://dl.cloudos.yihecloud.com/release"
#yum_url=$bin_url/yum
k8s_ip=$2
KUBE_CONFIG_DIR="/etc/kubernetes"
mkdir -p $KUBE_CONFIG_DIR/{ssl,manifests}

mkdir -p $basedir
#########################################################################################
# Base Function
#########################################################################################


function getip() {
    host_ips=(`ip addr show |grep inet |grep -v inet6 |grep brd |awk '{print $2}' |cut -f1 -d '/'`)
    if [ "${host_ips[0]}" == "" ]; then
        echo "[ERROR] get ip address error!"
        exit 1
    else
        echo "${host_ips[0]}"
    fi
}

function reload() {
  echo "releoad $*."
  systemctl daemon-reload
  systemctl enable $*
  systemctl restart $*
}

function load_file() {
  local fkube="$1.tar.gz"
  if [ ! -f $fkube ]; then
    local fdest=${2:-"./"}
    curl -O $bin_url/$fkube
    tar zvxf $fkube -C $fdest
  else
    echo "$fkube 已经下载"
  fi
}

function elog() {
  echo -e "\033[0;36;1m- [INFO] $*  \033[0m"
}

function cmd_exists() {
  command -v "$@" > /dev/null 2>&1
}

function getport() {
    echo `grep -o 'hostPort:\s*[0-9]*' $1|head -n 1|grep -o '[0-9]*'`
}


###################
# 初始化工作
###################


#配置源
function init_repo() {
  cat <<EOF > /etc/yum.repos.d/ob.repo
[ob]
name=Openbridge res
baseurl=https://dl.yihecloud.com/install/develop/ops/yum
enabled=1
EOF
}

# 加载bin文件
function load_bin() {
  mkdir -p $basedir/{bin,conf}
  mkdir -p $datadir/

  sync_file cloudos-bin.tar.gz
  tar zvxf $datadir/cloudos-bin.tar.gz
}

# 同步安装文件
function sync_file() {
  for i in $*;do
    local dist_file="$datadir/$i"
    local url="$release_server/$i"
    if [ ! -f $dist_file ];then
      echo "同步: $url"
      curl -o $dist_file $url 2>&1
    fi
  done
}

function init_base() {
 # 修改内核参数
  cat <<EOF>/etc/sysctl.d/88-k8s.conf
net.bridge.bridge-nf-call-iptables=1
net.bridge.bridge-nf-call-ip6tables=1
net.ipv4.ip_forward=1
EOF
  sysctl -p /etc/sysctl.d/88-k8s.conf

  # 设置时区
  /bin/cp -f /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

  # 同步bin文件
  # load_bin

  # 配置repo
  echo "初始化repo"
  init_repo

  # 生成组件安装包
  elog "生成组件安装包"
  # ./gen.sh

  # 安装组件
  elog "安装runtime组件"
  sudo yum install -y --nogpgcheck --enablerepo=ob vim jq
  #reload ntpd

  # 安装监控
  elog "安装监控组件"

  # sync_file
  elog "同步文件"
  #sync_file cloudos-pause.tar.gz

}



#########################################################################################
# Install Function
#########################################################################################

# 开机自动配置ip
function install_kubeconf() {
  cat <<EOF >/etc/kubernetes/config.sh
#!/bin/bash

function get_ip() {
    local IPS=\$(ip addr show | grep inet | grep -v inet6 | grep brd | awk '{print \$2}' | cut -f1 -d '/')

    if [ "\${IPS[0]}" == "" ]; then
        echo "[ERROR] get ip address error."
        exit 1
    else
       echo \${IPS[0]}
    fi
}

IP=\$(get_ip)
sed -i "s/\"--hostname-override=.*\"/\"--hostname-override=\$IP\"/g" $KUBE_CONFIG_DIR/kubelet.env;
EOF
  cat <<EOF >/etc/kubernetes/kubelet.env
NODE_HOSTNAME="--hostname-override=0.0.0.0"
KUBELET_ARGS="--v=2 --cgroup-driver=cgroupfs --cgroups-per-qos=True"
# --cgroups-per-qos=false
EOF
  chmod +x /etc/kubernetes/config.sh
}

# install kubelet
# --cgroup-driver=systemd \\
# --node-labels=node-role.kubernetes.io/master=true,node-role.kubernetes.io/node=true \\
function install_kubelet() {
  local online=""
  if [ "$1" = "online" ];then
    online="\\
  --kubeconfig=/etc/kubernetes/kubelet.conf \\
  --require-kubeconfig=true \\
  --network-plugin=cni \\
  --cni-conf-dir=/etc/cni/net.d \\
  --cni-bin-dir=/opt/cni/bin \\
  --cluster-dns=10.96.0.10 \\
  --cluster-domain=cluster.local"
  fi

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
  --pod-infra-container-image=image.cloudos.yihecloud.com/google_containers/pause-amd64:3.0 \\
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
}

# 安装etcd
function install_etcd() {
  sudo yum install -y --nogpgcheck https://dl.yihecloud.com/install/rpms/etcd-3.2.5-1.el7.x86_64.rpm
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
}

# install master
function install_master() {
  local master=$1
  echo "Install master to ip: $master"
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
    image: image.cloudos.yihecloud.com/google_containers/kube-apiserver-amd64:v1.6.10
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
    image: image.cloudos.yihecloud.com/google_containers/kube-scheduler-amd64:v1.6.10
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
    image: image.cloudos.yihecloud.com/google_containers/kube-controller-manager-amd64:v1.6.10
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
}

function install_kubetools() {
  cat <<EOF > /usr/bin/kid
#!/bin/bash
sudo docker ps |grep -v POD|grep "\$1@"|awk '{print \$1}'
EOF

  cat <<EOF > /usr/bin/kc
#!/bin/bash
kubectl \$*
EOF

  cat <<EOF > /usr/bin/kca
#!/bin/bash
kubectl \$* --all-namespaces
EOF

  cat <<EOF > /usr/bin/kcs
#!/bin/bash
kubectl \$* --namespace=kube-system
EOF

  chmod +x /usr/bin/k*
}

function install_module() {
  local module=$1
  hfile="hooks/init-$module.sh"
  mfile="manifests/ob-$module.yaml"

  if [ -f $hfile ];then
      elog "!!存在hook文件 $hfile !!"
      bash $hfile
  fi

  if [ ! -f $mfile ];then
    echo "!!不存在文件 $mfile !!"
  else
    ### 先拷贝文件到manifests然后，获取端口
    echo "-> 安装模块：$module" && cp $mfile $KUBE_CONFIG_DIR/manifests/
    mport=$(getport $KUBE_CONFIG_DIR/$mfile)
    echo "-> 从 $KUBE_CONFIG_DIR/$mfile 获取端口 $mport"
    echo "-> 注册模块服务：$module" && register $module $(getip) $mport
  fi
}

function check_master() {
  local delay=60
  kubectl get no
  while [ $? != 0 ];do
    echo "$(date -R): (sleep $delay s) Check master status...."; sleep $delay
    delay=10
    kubectl get no
  done
}

function create_addons() {
  #  node-exporter
  for i in proxy dns calico backend ingress-filebeat jvmviewer;do
    kubectl apply -f addons/kube-$i.yaml
  done
  kubectl apply -f addons/example.yaml
  kubectl get po --all-namespaces
}

function add_label() {
  kubectl label nodes $(getip) $1=$2
}

function register() {
  if [ ! -f ./script/register.sh ];then
    elog "!!!!!注册脚本script/register.sh不存在!!!!!"
  else
    ./script/register.sh $*
  fi
}

#########################################################################################
# Install
#########################################################################################
# add hosts
#echo "$k8s_ip  k8s.service.ob.local" >> /etc/hosts


elog "下载安装文件" && load_file k8s-bin
/bin/cp -f bin/* /usr/bin/

# init base
init_base

elog "安装Node" 
if cmd_exists docker;then
  elog "已安装Docker"
else
  elog "安装Docker" && ./docker.sh  
fi

install_kubetools

if [ "$1" = "node" ];then
  /bin/cp -f kubelet.conf $KUBE_CONFIG_DIR/
  elog "安装kubelet在线节点"; install_kubeconf; install_kubelet "online"
elif [ "$1" = "master" ];then
  elog "安装kubelet在线节点"; install_kubeconf; install_kubelet "online"
  # 初始化
  #init_base
  elog "生成组件安装包" && ./gen.sh
  /bin/cp -f kubelet.conf $KUBE_CONFIG_DIR/
  tar zxvf kube-key.tar.gz  -C $KUBE_CONFIG_DIR/ssl/
  elog "载入master文件" && tar zxvf kube-key.tar.gz  -C $KUBE_CONFIG_DIR/ssl/

  elog "安装Etcd" && install_etcd 
  elog "安装Master" && install_master $(getip)
  elog "Master安装完成"

  # 注册服务
  #elog "注册etcd,master服务"
  #register etcd localip 2379
  #register k8s localip 6443
 # register jvmviewer localip 80

  # 其它配置
  #sed -i "s/k8s.service.ob.local/$MASTER_IP/g" addons/*.yaml

  # auto add addons
  elog "检测master启动状态，并安装addons"
  check_master && create_addons
  add_label ingress controller
  add_label ENGINE_SYSTEM_NODE_PROXY cloudos.master
else
  elog "参数异常！"
fi
