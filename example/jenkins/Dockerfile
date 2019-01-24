FROM jenkins/jenkins:lts

USER root
RUN echo '' > /etc/apt/sources.list.d/jessie-backports.list \
  && echo "deb http://mirrors.aliyun.com/debian jessie main contrib non-free" > /etc/apt/sources.list \
  && echo "deb http://mirrors.aliyun.com/debian jessie-updates main contrib non-free" >> /etc/apt/sources.list \
  && echo "deb http://mirrors.aliyun.com/debian-security jessie/updates main contrib non-free" >> /etc/apt/sources.list

RUN apt-get update       && apt-get install -y sudo       && apt-get install -y libltdl7       && rm -rf /var/lib/apt/lists/*
RUN echo "jenkins ALL=NOPASSWD: ALL" >> /etc/sudoers

# USER jenkins
#COPY plugins.txt /usr/share/jenkins/plugins.txt
#RUN /usr/local/bin/plugins.sh /usr/share/jenkins/plugins.txt
