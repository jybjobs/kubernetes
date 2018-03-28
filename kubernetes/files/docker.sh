#!/bin/bash

# 安装和启动docker
# 2017-07-05


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

function load_bin() {
    curl -O http://mirrors.ustc.edu.cn/docker-yum/repo/centos7/Packages/docker-engine-1.13.1-1.el7.centos.x86_64.rpm
    curl -O http://mirrors.ustc.edu.cn/docker-yum/repo/centos7/Packages/docker-engine-selinux-1.13.1-1.el7.centos.noarch.rpm
}

function elog() {
	echo -e "\033[0;36;1m- [INFO] $*  \033[0m"
}

#########################################################################################
# Install Function
#########################################################################################
# 安装docker
function install_docker() {
  yum remove -y docker* container-selinux*
  yum install -y --nogpgcheck docker-engine-*
  cat <<EOF > /etc/sysconfig/docker
# --exec-opt native.cgroupdriver=systemd
DOCKER_OPTS="--insecure-registry=docker.ob.local --insecure-registry=docker.service.ob.local --insecure-registry=image.service.ob.local:5000 --insecure-registry=registry.service.ob.local:5001 --insecure-registry=registry-proxy.service.ob.local:5002"
EOF
  cat <<EOF > /lib/systemd/system/docker.service
[Unit]
Description=Docker Application Container Engine
Documentation=https://docs.docker.com
After=network.target firewalld.service confd.service

[Service]
Type=notify
EnvironmentFile=-/etc/sysconfig/docker
ExecStart=/usr/bin/dockerd -s overlay -H unix:///var/run/docker.sock \$DOCKER_OPTS
ExecReload=/bin/kill -s HUP $MAINPID
LimitNOFILE=infinity
LimitNPROC=infinity
LimitCORE=infinity
#TasksMax=infinity
TimeoutStartSec=0
Delegate=yes
KillMode=process
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
  reload docker
}

function config_docker() {
	if [ ! -d /etc/docker ];then
		mkdir -p /etc/docker
	fi
	cat<<EOF >/etc/docker/daemon.json
{
	"registry-mirrors":["http://image.service.ob.local:5000", "https://docker.mirrors.ustc.edu.cn"]
}
EOF
}

if [ "$1" = "-f" ];then
  echo "网络安装"; load_bin
fi

elog "安装配置docker"
config_docker
install_docker
