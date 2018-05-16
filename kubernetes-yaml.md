# kubernetes 组件模板介绍


## YAML 基础

如果你正在做的事与很多软件领域相关，那么将很难不涉及到YAML，特别是Kubernetes，SDN，和OpenStack。YAML，它代表着另一种标志语言，或者YAML不是标志语言（取决于你问谁）而是特定配置类型基于人类可读的文本格式的信息，例如，在本文中，我们将会分开说说明YAML定义创建Pod和使用Kubernetes创建一个Depolyment。

使用YAML用于k8s的定义将给你一些好处，包括：

便捷性：你将不再需要添加大量的参数到命令行中执行命令
可维护性：YAML文件可以通过源头控制，可以跟踪每次的操作
灵活性：通过YAML你将可以创建比命令行更加复杂的结构
YAML是一个JSON的超集，意味着任何有效JSON文件也都是一个有效的YAML文件。所以一方面，如果你知道JSON，你只是要去写自己的YAML（而不是阅读别人的）也就可以了。另一方面，不太可能,不幸的是，尽管你尝试去网上找到例子，但是他们通常都不是JSON，所以我们可能需要去习惯它。不过，有JSON的情况下可能会更方便，这样你将会很开心你懂得JSON。

幸运的是，YAML只有两种结构类型你需要知道：

Lists
Maps
那就是说，将有可能存在lists的maps和maps的lists，等等，但是，你只要掌握了这两种结构也就可以了。这并不是说你不能做更加复杂的事，但是通常，这些也就够了。

#### YAML Maps

我们先开始看YAML maps。Maps让你将键值组合，你就可以更加方便的去设置配置信息。例如，你可能有以下这样一个配置信息：

---
apiVersion: v1
kind: Pod
第一行是分隔符，并且是可选的，除非你试图在单个文件中定义多个结构。从这里你可以看到，我们有两个值，V1和Pod，对应他们的键是apiVersion和kind。

这种比较简单，当然你也可以将之转换为json格式，如下：
```
{
   "apiVersion": "v1",
   "kind": "Pod"
}
```
注意，在我们的YAML版本中，引号是可选的，处理器可以知道你正在查看基于格式化的字符串。

你也可以指定一个复杂的结构，创建一个key其对应的值不是字符串而是一个maps如下所示：
```
---
apiVersion: v1
kind: Pod
metadata:
  name: rss-site
  labels:
    app: web
```
这种情况下，我们有metadata这个key对应的值中又有两个key分别为name和labels。labels 这个key的值又是一个map。你可以根据场景进行多层嵌套。

YAML处理器可以根据行缩进来知道内容之间的关联。在这个例子中我用了两个空格使之可读，但是空格的数量不重要，但是至少要求一个，并且所有缩进都保持一致的空格数。例如，name和labels是相同缩进级别，因此YAML处理器知道他们属于同一map；它知道app是lables的值因为app的缩进更大。

注意：在YAML文件中绝对不要使用tab键

因此，如果我们将上述内容翻译成JSON，它看起来结果如下所示：
```
{
  "apiVersion": "v1",
  "kind": "Pod",
  "metadata": {
               "name": "rss-site",
               "labels": {
                          "app": "web"
                         }
              }
}
```
现在让我们来看看lists。

#### YAML lists

YAML lists 简直是一个序列的对象，例如：
```
args
  - sleep
  - "1000"
  - message
  - "Bring back Firefly!"
```
正如你可以看到,你可以有任何数量的项在列表中，项的定义以破折号（-）开头，并且与父元素之间存在缩进。在JSON格式中，它将表示如下：

```
{
   "args": ["sleep", "1000", "message", "Bring back Firefly!"]
}
```
当然，list的子项也可以是maps，maps的子项也可以是list如下所示：

```
---
apiVersion: v1
kind: Pod
metadata:
  name: rss-site
  labels:
    app: web
spec:
  containers:
    - name: front-end
      image: nginx
      ports:
        - containerPort: 80
    - name: rss-reader
      image: nickchase/rss-php-nginx:v1
      ports:
        - containerPort: 88
```
正如你所看到的，我们有一个叫container的list对象，每个子项都由name、image、ports组成，每个ports都由一个key为containerPort map组成

如下所示，是上述内容的JSON格式：

```
{
   "apiVersion": "v1",
   "kind": "Pod",
   "metadata": {
                 "name": "rss-site",
                 "labels": {
                             "app": "web"
                           }
               },
    "spec": {
       "containers": [{
                       "name": "front-end",
                       "image": "nginx",
                       "ports": [{
                                  "containerPort": "80"
                                 }]
                      }, 
                      {
                       "name": "rss-reader",
                       "image": "nickchase/rss-php-nginx:v1",
                       "ports": [{
                                  "containerPort": "88"
                                 }]
                      }]
            }
}
```
正如你所看到的，我们写的内容开始变的复杂，甚至我们还没有进入任何特别复杂!难怪YAML代替JSON如此之快。

现在然我们复习一下，我们有：

maps，键值对
lists，单独的项
maps的maps
maps的lists
lists的lists
lists的maps
基本上，无论你想要什么样结构，你都可以通过这两个结构去组合实现。



## API版本控制

为了更容易消除字段或重新构建资源表示，Kubernetes支持多个API版本，每个版本都位于不同的API路径（如/api/v1或） /apis/extensions/v1beta1。
我们选择在API级别进行版本化，而不是在资源或现场级别进行版本化，以确保API对系统资源和行为提供清晰，一致的视图，并控制对终止使用和/或实验性API的访问控制。JSON和Protobuf序列化模式遵循相同的模式更改准则 - 下面的所有描述都涵盖了这两种格式。
请注意，API版本控制和软件版本控制只是间接相关的。该API和发行版本的提案描述API版本和软件版本之间的关系。
不同的API版本意味着不同级别的稳定性和支持。每个级别的标准在API变更文档中有更详细的描述。他们总结在这里：
Alpha级别：
    版本名称包含alpha（例如v1alpha1）。
    可能是越野车。启用该功能可能会暴露错误。默认情况下禁用。
    对功能的支持可能随时丢失，恕不另行通知。
    在未来的软件版本中，API可能会以不兼容的方式更改，恕不另行通知。
    由于缺陷风险增加和缺乏长期支持，建议仅用于短期测试集群。
测试版级别：
    版本名称包含beta（例如v2beta3）。
    代码已经过良好测试。启用该功能被认为是安全的。默认启用。
    虽然细节可能会改变，但对整体功能的支持不会被丢弃。
    对象的模式和/或语义可能会在后续的测试版或稳定版中以不兼容的方式发生变化。发生这种情况时，我们将提供迁移到下一个版本的说明。这可能需要删除，编辑和重新创建API对象。编辑过程可能需要一些思考。这可能需要停用依赖于该功能的应用程序。
    建议仅用于非业务关键型用途，因为后续版本中可能存在不兼容的更改。如果您有多个可以独立升级的群集，则可以放宽此限制。
    请尝试使用我们的测试版功能并对它们提供反馈！一旦他们退出测试版，对我们做出更多改变可能不切实际。
稳定水平：
    版本名称是vX其中X的一个整数。
    稳定版本的功能将出现在许多后续版本的发布软件中。


## pod

Pod是Kubernetes创建或部署的最小/最简单的基本单位，一个Pod代表集群上正在运行的一个进程。

一个Pod封装一个应用容器（也可以有多个容器），存储资源、一个独立的网络IP以及管理控制容器运行方式的策略选项。Pod代表部署的一个单位：Kubernetes中单个应用的实例，它可能由单个容器或多个容器共享组成的资源。

Docker是Kubernetes Pod中最常见的runtime ，Pods也支持其他容器runtimes。
Kubernetes中的Pod使用可分两种主要方式：

Pod中运行一个容器。“one-container-per-Pod”模式是Kubernetes最常见的用法; 在这种情况下，你可以将Pod视为单个封装的容器，但是Kubernetes是直接管理Pod而不是容器。
Pods中运行多个需要一起工作的容器。Pod可以封装紧密耦合的应用，它们需要由多个容器组成，它们之间能够共享资源，这些容器可以形成一个单一的内部service单位 - 一个容器共享文件，另一个“sidecar”容器来更新这些文件。Pod将这些容器的存储资源作为一个实体来管理.

```
apiVersion: v1  # api版本
kind: Pod　　　　# 组件类型
metadata:
  name: string　　　＃　组件名称，全局唯一
  namaspace: string　　＃　namespace 名称　
  labels:　　　　　　＃　label　用于selector选择　　
  - name: string 
  annotations:　   # 将任何非标识metadata附加到对象,主要为了方便阅读不会被k8s使用       
  - name: string
spec:　　　　　　　　　
  containers:　　　＃　pod内容的定义部分
  - name: string　　＃　容器名称
    images: string　　＃　镜像
    imagePullPolice: [Always | Never | IfNotPresent]　＃　镜像下载策略
    command: [string]
    args: [string]
    workingDir: string　
    volumeMounts:
    - name: string
      mountPath: string
      readOnly: boolean
    ports:　　　　　＃　端口信息
    - name: string
      containerPort: int
      hostPort: int
      protocol: string
    env:　　　　　　＃　环境变量
    - name: string
      value: string
    resources:　　＃　资源管理
      limits:
        cpu: string
        memory: string
      requests:
        cpu: string
        memory: string
    livenessProbe:　＃　健康检查－判断容器存活；readinessProbe 判断服务是否准备好
      exec:
        command: [string]
      httpGet:
        path: string
        port: int
        host: string
        scheme: string # HTTP or HTTPS
        httpHeaders:
        - name: string
          value: string
      tcpSocket:
        port: int
      initialDelaySeconds: number　＃　启动容器后首次进行健康检查的等待时间，单位为秒
      timeoutSeconds: number　　　　＃　健康检查发送请求等待响应时间
      periodSeconds: number        #  执行探测的频率。默认是10秒，最小1秒
      successThreshold: 0    # 探测失败后，最少连续探测成功多少次才被认定为成功。默认是1。对于liveness必须是1。最小值是1。
      failureThreshold: 0    # 探测成功后，最少连续探测失败多少次才被认定为失败。默认是3。最小值是1。
    securityContext:
      privileged: false
  restartPolicy: [Always | Never | OnFailure]   ＃　重启策略
  nodeSelector: object　　　　　　　＃　node选择器
  imagePullSecrets:　　　　　　　　　＃　镜像下载授权
  - name: string
  hostNetwork: false　＃　如果配置为true　则默认容器使用host网络，如果不配置hostPort默认容器端口和宿主机端口对应，如果指定了hostPort，则hostPort必须等于containerPort的值
  volumes:
  - name: string
    emptyDir: {}
    hostPath:
      path: string
    secret:
      secretName: string
      items:
      - key: string
        path: string
    configMap:
      name: string
      items:
      - key: string
        path: string
```

## ReplicaSet
ReplicaSet（RS）是Replication Controller（RC）的升级版本。ReplicaSet 和  Replication Controller之间的唯一区别是对选择器的支持。ReplicaSet支持labels user guide中描述的set-based选择器要求， 而Replication Controller仅支持equality-based的选择器要求。

```
apiVersion: extensions/v1beta1
kind: ReplicaSet
metadata:
  name: string
  labels:
    name: string
spec:
  replicas: 3    # 副本数
  selector:　　　＃　set-based 选择器
    matchLabels:　　　＃　matchLabels 是一个{key,value}的映
      tier: frontend　　　
    matchExpressions:　#  matchExpressions 是一个pod的选择器条件的list 。有效运算符包含In, NotIn, Exists, 和DoesNotExist
      - {key: tier, operator: In, values: [frontend]}
  template:  # 根据模板创建pod
    metadata:
      labels:
        app: guestbook
        tier: frontend
    spec:
      containers:
      - name: php-redis
        image: gcr.io/google_samples/gb-frontend:v3
        resources:
          requests:
            cpu: 100m
            memory: 100Mi
        env:
        - name: GET_HOSTS_FROM
          value: dns
        ports:
        - containerPort: 80  
```

## deployment
Deployment同样为Kubernetes的一个核心内容，主要职责同样是为了保证pod的数量和健康，90%的功能与Replication Controller完全一样，可以看做新一代的Replication Controller。但是，它又具备了Replication Controller之外的新特性：

Replication Controller全部功能：Deployment继承了上面描述的Replication Controller全部功能。

事件和状态查看：可以查看Deployment的升级详细进度和状态。

回滚：当升级pod镜像或者相关参数的时候发现问题，可以使用回滚操作回滚到上一个稳定的版本或者指定的版本。

版本记录: 每一次对Deployment的操作，都能保存下来，给予后续可能的回滚使用。

暂停和启动：对于每一次升级，都能够随时暂停和启动。

多种升级方案：Recreate：删除所有已存在的pod,重新创建新的; RollingUpdate：滚动升级，逐步替换的策略，同时滚动升级时，支持更多的附加参数，例如设置最大不可用pod数量，最小升级间隔时间等等。

```
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: demo-service-dp
  labels:
    app: demo-service
spec:
  replicas: 3
  minReadySeconds: 120     #滚动升级时60s后认为该pod就绪
  strategy:
    rollingUpdate:  ##由于replicas为3,则整个升级,pod个数在2-4个之间
      maxSurge: 1      #滚动升级时会先启动1个pod
      maxUnavailable: 1 #滚动升级时允许的最大Unavailable的pod个数
  selector:
    matchLabels:
      app: demo-service
  template:
    metadata:
      labels:
        app: demo-service
    spec:
      hostNetwork: false
      containers:
        - name: ingageapp
          image: docker.xsy.io/app/demo-service:1.8
          args: []
          env:
            - name: XSY_DISCOVERY_LOG_LEVEL
              value: "info"
            - name: SERVICE_NAME
              value: "demo-service"
            - name: XSY_DISCOVERY_SERVER_PORT
              value: "8080"
          ports:
            - containerPort: 8080
          resources:
            requests:
              memory: 256Mi
              cpu: 200m
            limits:
              memory: 1024Mi
              cpu: 600m
```
## service

Service是kubernetes最核心的概念，通过创建Service，可以为一组具有相同功能的容器应用提供一个统一的入口地址，并且将请求进行负载分发到后端的各个容器应用上。

Service服务是一个虚拟概念，逻辑上代理后端pod。众所周知，pod生命周期短，状态不稳定，pod异常后新生成的pod ip会发生变化，之前pod的访问方式均不可达。通过service对pod做代理，service有固定的ip和port，ip:port组合自动关联后端pod，即使pod发生改变，kubernetes内部更新这组关联关系，使得service能够匹配到新的pod。这样，通过service提供的固定ip，用户再也不用关心需要访问哪个pod，以及pod是否发生改变，大大提高了服务质量。如果pod使用rc创建了多个副本，那么service就能代理多个相同的pod，通过kube-proxy，实现负载均衡。

集群中每个Node节点都有一个组件kube-proxy，实际上是为service服务的，通过kube-proxy，实现流量从service到pod的转发，kube-proxy也可以实现简单的负载均衡功能。

kube-proxy代理模式：userspace方式。kube-proxy在节点上为每一个服务创建一个临时端口，service的IP:port过来的流量转发到这个临时端口上，kube-proxy会用内部的负载均衡机制（轮询），选择一个后端pod，然后建立iptables，把流量导入这个pod里面。

```
apiVersion: v1
kind: Service
matadata:
  name: string
  namespace: string
  labels:
    - name: string
  annotations:
    - name: string
spec:
  selector: []
  type: string  # 默认为　clusterIP(虚拟服务IP); NodePort（开发宿主机端口） loadBalancer（使用外接负载均衡器）
  clusterIP: string　＃ clusterIP类型下不指定默认分配，ＬＢ下需要指定ＩＰ
  sessionAffinity: string　＃是否支持serssion,clientIP支持同一个客户端ＩＰ转发到同一个ｐｏｄ上执行
  ports:
  - name: string
    protocol: string　＃TCP（默认） or UDP
    port: int　　　　　# service port
    targetPort: int　　# 后端pod端口
    nodePort: int　　# 指定node 端口，不指定时由系统自动生成
  status:
    loadBalancer:　＃外部负载均衡器配置
      ingress:
        ip: string
        hostname: string
```


## Labels选择器

与Name和UID 不同，标签不需要有唯一性。一般来说，我们期望许多对象具有相同的标签。

通过标签选择器（Labels Selectors），客户端/用户 能方便辨识出一组对象。标签选择器是kubernetes中核心的组成部分。

API目前支持两种选择器：equality-based（基于平等）和set-based（基于集合）的。标签选择器可以由逗号分隔的多个requirements 组成。在多重需求的情况下，必须满足所有要求，因此逗号分隔符作为AND逻辑运算符。

一个为空的标签选择器（即有0个必须条件的选择器）会选择集合中的每一个对象。

一个null型标签选择器（仅对于可选的选择器字段才可能）不会返回任何对象。

注意：两个控制器的标签选择器不能在命名空间中重叠。

#### Equality-based requirement 基于相等的要求

基于相等的或者不相等的条件允许用标签的keys和values进行过滤。匹配的对象必须满足所有指定的标签约束，尽管他们可能也有额外的标签。有三种运算符是允许的，“=”，“==”和“!=”。前两种代表相等性（他们是同义运算符），后一种代表非相等性。例如：
```
environment = production
tier != frontend
```
第一个选择所有key等于 environment 值为 production 的资源。后一种选择所有key为 tier 值不等于 frontend 的资源，和那些没有key为 tier 的label的资源。要过滤所有处于 production 但不是 frontend 的资源，可以使用逗号操作符，

frontend：environment=production,tier!=frontend
#### Set-based requirement

Set-based 的标签条件允许用一组value来过滤key。支持三种操作符: in ， notin 和 exists(仅针对于key符号) 。例如：
```
environment in (production, qa)
tier notin (frontend, backend)
partition
!partition
```
第一个例子，选择所有key等于 environment ，且value等于 production 或者 qa 的资源。 第二个例子，选择所有key等于 tier 且值是除了 frontend 和 backend 之外的资源，和那些没有标签的key是 tier 的资源。 第三个例子，选择所有有一个标签的key为partition的资源；value是什么不会被检查。 第四个例子，选择所有的没有lable的key名为 partition 的资源；value是什么不会被检查。

类似的，逗号操作符相当于一个AND操作符。因而要使用一个 partition 键（不管value是什么），并且 environment 不是 qa 过滤资源可以用 partition,environment notin (qa) 。

Set-based 的选择器是一个相等性的宽泛的形式，因为 environment=production 相当于environment in (production) ，与 != and notin 类似。

Set-based的条件可以与Equality-based的条件结合。例如， partition in (customerA,customerB),environment!=qa 。