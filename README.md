# kubernetes
## 什么是kubernetes
Kubernetes(k8s)是Google开源的容器集群管理系统（谷歌内部:Borg）。在Docker技术的基础上，为容器化的应用提供部署运行、资源调度、服务发现和动态伸缩等一系列完整功能，提高了大规模容器集群管理的便捷性。

#### Kubernetes的核心概念
    1.Pod
    运行于Node节点上，若干相关容器的组合。Pod内包含的容器运行在同一宿主机上，使用相同的网络命名空间、IP地址和端口，能够通过localhost进行通。Pod是Kurbernetes进行创建、调度和管理的最小单位，它提供了比容器更高层次的抽象，使得部署和管理更加灵活。一个Pod可以包含一个容器或者多个相关容器。
    2.Replication Controller
    Replication Controller用来管理Pod的副本，保证集群中存在指定数量的Pod副本。集群中副本的数量大于指定数量，则会停止指定数量之外的多余容器数量，反之，则会启动少于指定数量个数的容器，保证数量不变。Replication Controller是实现弹性伸缩、动态扩容和滚动升级的核心。
    3.Service
    Service定义了Pod的逻辑集合和访问该集合的策略，是真实服务的抽象。Service提供了一个统一的服务访问入口以及服务代理和发现机制，用户不需要了解后台Pod是如何运行。
    4.Label
    Kubernetes中的任意API对象都是通过Label进行标识，Label的实质是一系列的K/V键值对。Label是Replication Controller和Service运行的基础，二者通过Label来进行关联Node上运行的Pod。
    5.Node
    Node是Kubernetes集群架构中运行Pod的服务节点（亦叫agent或minion）。Node是Kubernetes集群操作的单元，用来承载被分配Pod的运行，是Pod运行的宿主机。
#### Kubernetes组件
> Kubernetes Master控制组件，调度管理整个系统（集群），包含如下组件:

    1.Kubernetes API Server
    作为Kubernetes系统的入口，其封装了核心对象的增删改查操作，以RESTful API接口方式提供给外部客户和内部组件调用。维护的REST对象持久化到Etcd中存储
    2.Kubernetes Scheduler
    为新建立的Pod进行节点(node)选择(即分配机器)，负责集群的资源调度。组件抽离，可以方便替换成其他调度器。
    3.Kubernetes Controller
    负责执行各种控制器，目前已经提供了很多控制器来保证Kubernetes的正常运行。
        - Replication Controller
        管理维护Replication Controller，关联Replication Controller和Pod，保证Replication Controller定义的副本数量与实际运行Pod数量一致
        - Node Controller
        管理维护Node，定期检查Node的健康状态，标识出(失效|未失效)的Node节点。
        - Namespace Controller
        管理维护Namespace，定期清理无效的Namespace，包括Namesapce下的API对象，比如Pod、Service等。
        - Service Controller
        管理维护Service，提供负载以及服务代理。
        - EndPoints Controller
        管理维护Endpoints，关联Service和Pod，创建Endpoints为Service的后端，当Pod发生变化时，实时更新Endpoints
        - Service Account Controller
        管理维护Service Account，为每个Namespace创建默认的Service Account，同时为Service Account创建Service Account Secret
        - Persistent Volume Controller
        管理维护Persistent Volume和Persistent Volume Claim，为新的Persistent Volume Claim分配Persistent Volume进行绑定，为释放的Persistent Volume执行清理回收
        - Daemon Set Controller
        管理维护Daemon Set，负责创建Daemon Pod，保证指定的Node上正常的运行Daemon Pod。
        - Deployment Controller
        管理维护Deployment，关联Deployment和Replication Controller，保证运行指定数量的Pod。当Deployment更新时，控制实现Replication Controller和　Pod的更新 。
        - Job Controller
        管理维护Job，为Jod创建一次性任务Pod，保证完成Job指定完成的任务数目
        - Pod Autoscaler Controller
        实现Pod的自动伸缩，定时获取监控数据，进行策略匹配，当满足条件时执行Pod的伸缩动作。

> Kubernetes Node运行节点，运行管理业务容器，包含如下组件:

    1.Kubelet
    负责管控容器，Kubelet会从Kubernetes API Server接收Pod的创建请求，启动和停止容器，监控容器运行状态并汇报给Kubernetes API Server
    2.Kubernetes Proxy
    负责为Pod创建代理服务，Kubernetes Proxy会从Kubernetes API Server获取所有的Service信息，并根据Service的信息创建代理服务，实现Service到Pod的请求路由和转发，从而实现Kubernetes层级的虚拟转发网络。
    3.Docker
    Node上需要运行容器服务。
## 架构:
 ![架构图](architecture.png)


## 部署

## fabric8 api
## pv pvc demo


