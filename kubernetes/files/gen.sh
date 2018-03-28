#!/bin/bash

MASTER_ADDR=${1:-"k8s.service.ob.local"}
EXPIRY_YEAR=${2:-10}

# if [ "$MASTER_ADDR" = "" ];then
#   echo "必需输入一个k8s master的ip地址"
#   exit 1
# fi

PATH=$PATH:$(pwd)/bin
EXPIRY_HOUR="$((EXPIRY_YEAR*8760))h"
datadir=./

echo "master: $MASTER_ADDR, year: $EXPIRY_YEAR"
#87600h

function gen_cafile() {
  echo "生成k8s密钥"
  rm -f ca* kubernetes*
  cat <<EOF > ca-config.json
{
  "signing": {
    "default": {
      "expiry": "$1"
    },
    "profiles": {
      "kubernetes": {
        "usages": [
          "signing",
          "key encipherment",
          "server auth",
          "client auth"
         ],
         "expiry": "$1"
      }
    }
  }
}
EOF

  cat <<EOF > ca-csr.json 
{
  "CN": "kubernetes",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [{
    "C": "CN",
    "ST": "BeiJing",
    "L": "BeiJing",
    "O": "OpenBridge",
    "OU": "System"
  }]
}
EOF

  cat <<EOF > kubernetes-csr.json
{
    "CN": "kubernetes",
    "hosts": [
      "127.0.0.1",
      "10.96.0.1",
      "$MASTER_ADDR",
      "kubernetes",
      "kubernetes.default",
      "kubernetes.default.svc",
      "kubernetes.default.svc.cluster",
      "kubernetes.default.svc.cluster.local"
    ], "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [ {
      "C": "CN",
      "ST": "BeiJing",
      "L": "BeiJing",
      "O": "OpenBridge",
      "OU": "System"
    } ]
}
EOF

  # config ssl
  #rm -rf *.pem *.csr
  cfssl gencert -initca ca-csr.json | cfssljson -bare ca
  cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes kubernetes-csr.json | cfssljson -bare kubernetes
}

function gen_kube_config() {
  echo "生成k8s配置"
  KUBE_APISERVER="https://$MASTER_ADDR:6443"
  # BOOTSTRAP_TOKEN="$(head -c 16 /dev/urandom | od -An -t x | tr -d ' ')"
  BOOTSTRAP_TOKEN="$(echo $MASTER_ADDR|md5sum|cut -f 1 -d ' ')"
  echo "BOOTSTRAP_TOKEN: $BOOTSTRAP_TOKEN"

  echo "$BOOTSTRAP_TOKEN,kubelet-bootstrap,10001,\"system:kubelet-bootstrap\"" > token.csv
  echo "123456,admin,admin" > user.csv

  sudo mkdir -p /etc/kubernetes/ssl
  sudo cp -f ca.pem /etc/kubernetes/ssl/
  kubectl config set-cluster kubernetes \
      --certificate-authority=/etc/kubernetes/ssl/ca.pem \
      --embed-certs=true \
      --server=${KUBE_APISERVER} \
      --kubeconfig=kubelet.conf

  kubectl config set-credentials kubelet-bootstrap \
      --token=${BOOTSTRAP_TOKEN} \
      --kubeconfig=kubelet.conf

  kubectl config set-context default \
      --cluster=kubernetes \
      --user=kubelet-bootstrap \
      --kubeconfig=kubelet.conf

  kubectl config use-context default --kubeconfig=kubelet.conf
}

function gen_kube_key() {
  tar zvcf $datadir/kube-key.tar.gz *.pem *.csr *.csv
  cp -f kubelet.conf $datadir/
}

function gen_kube_package() {
  echo "打包k8s安装包"
  if [ ! -f kubelet.conf ];then
    cp -f $datadir/kubelet.conf ./
  fi
  tar zvcf $datadir/kube.tar.gz bin/kubelet bin/calicoctl bin/kubectl \
    addons/*.yaml \
    manifests/*.yaml \
    hooks/*.sh \
    script/{register,docker}.sh *kubelet*
}

# 不存在ca.pem文件时才重新生成k8s的ca密钥文件
# 如果要重新后成，可以在运行脚本前删除ca.pem
if [ ! -f $datadir/kube-key.tar.gz ];then
  gen_cafile $EXPIRY_HOUR
  gen_kube_config
  gen_kube_key
fi
gen_kube_package
