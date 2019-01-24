#FROM registry.cn-hangzhou.aliyuncs.com/cqz/elasticsearch:5.5.1 
FROM  registry.cn-hangzhou.aliyuncs.com/joinin/elasticsearch:6.2.4

MAINTAINER jybjobs@sina.cn

#RUN echo "vm.max_map_count=262144" >> /etc/sysctl.conf
COPY run.sh /

RUN chmod 775 /run.sh

CMD ["/run.sh"]

