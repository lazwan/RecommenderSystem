## 一、环境配置

### 1. 安装 MongoDB（Windows 和 CentOS 7）

> Windows 安装

下载链接：[https://www.mongodb.com/try/download/community](https://www.mongodb.com/try/download/community)

1. 在 MongoDB 根目录创建 data 和 logs 文件夹，并在 logs 文件夹下创建空的 mongo.log 文件
2. 在 MongoDB 根目录创建 MongoDB 的配置文件 mongo.conf
3. 在 mongo.conf 内配置一下内容

    ```
    # 数据库路径
    dbpath=D:\Software\mongodb-5.0.6\data
    # 日志输出文件路径
    logpath=D:\Software\mongodb-5.0.6\logs\mongo.log
    # 错误日志采用追加模式
    logappend=true
    # 启用日志文件，默认启用
    journal=true
    # 这个选项可以过滤掉一些无用的日志信息，若需要调试使用请设置为false
    quiet=true
    # 端口号默认为 27017
    port=27017
    ```

4. 安装 MongoDB 服务，并运行 MongoDB 服务

    ```shell
    mongod --config "D:\Software\mongodb-5.0.6\mongo.conf" -install
    net start mongodb
    ```
> CentOS 7 安装

1. Create a `/etc/yum.repos.d/mongodb-org-4.4.repo` file so that you can install MongoDB directly using `yum`:

    ```
    [mongodb-org-4.4]
    name=MongoDB Repository
    baseurl=https://repo.mongodb.org/yum/redhat/$releasever/mongodb-org/4.4/x86_64/
    gpgcheck=1
    enabled=1
    gpgkey=https://www.mongodb.org/static/pgp/server-4.4.asc
    ```

3. Install the MongoDB packages.

    ```bash
    sudo yum install -y mongodb-org
    ```

4. Remote Connection MongoDB

    Change bindIp `127.0.0.1` -> `0.0.0.0`

    ```
    vi /etc/mongod.conf
    
    # network interfaces
    net:
      port: 27017
      bindIp: 0.0.0.0  # Enter 0.0.0.0,:: to bind to all IPv4 and IPv6 addresses or, alternatively, use the net.bindIpAll setting.
    ```

### 2. 安装 zookeeper

- 修改配置文件 `vim /opt/zookeeper/conf/zoo.cfg`

    ```
    dataDir=/opt/zookeeper/data
    server.0=master:2888:3888
    server.1=slave1:2888:3888
    server.2=slave2:2888:3888
    ```

- 分别在每台机器的 `/opt/zookeeper/data/myid` 中填入 `id`

    ```
    0, 1, 2
    ```

### 3. 安装 Kafka

- 修改配置文件 `vim /opt/kafka/conf/server.properties`

    ```
    # 1. 修改 broker.id 每台机器不能重复
    broker.id=1
    
    # 2. 修改 log.dirs 存放路径
    log.dirs=/opt/kafka/logs
    
    # 3. 修改 zookeeper.connect
    zookeeper.connect=master:2181,node1:2181,node2:2181
    ```

### 4. 安装 Redis

```shell
yum install epel-release
yum install redis
```

- 修改配置文件 `vim /etc/redis.conf`

    ```
    bind 127.0.0.1     => bind 192.168.10.131
    protected-mode yes => protected-mode no
    ```

### 5. 安装 flume


## 二、模块介绍

数据源  -> 数据采集 -> 数据存储 -> 数据计算 -> 数据应用

### 1. 数据加载模块（DataLoader）

> 数据加载服务（Spark SQL）---> 业务数据库（MongoDB）

数据源解析

- 商品信息：produces.csv
    - 商品 ID
    - 商品名称
    - 商品种类（'|'分割）
    - 商品图片 URL
    - 商品标签（'|'分割）
- 用户评分数据：ratings.csv
    - 用户 ID
    - 商品 ID
    - 商品评分
    - 评分时间

主要数据模型

- 商品信息表：`product` -> productId, name, categories, imageUrl, tags
- 用户评分表：`rating` -> uid, productId, score, timestamp
- 用户表：`user` -> uid, username, password, timestamp


- 历史热门商品统计表：`rate_more_products` -> productId, count
- 近期热门商品统计表：`reat_more_recently_products` -> productId, count, yearmonth
- 商品平均评分统计表：`average_products` -> productId, avg
- 离线（基于 LFM）用户推荐表：`` -> uid, recs: [(productId, ...)]
- 离线（基于 LFM）商品相似度表：`` -> productId, recs: [(productId, ...)]
- 离线（基于内容）商品相似度表：`` -> productId, recs: [(productId, ...)]
- 离线（基于 Item-CF）商品相似度表：`` -> productId, recs: [(productId, ...)]
- 实时用户推荐表：`` -> uid, recs: [(productId, ...)]

### 2. 统计推荐模块（StatisticsRecommender）

> 离线统计服务（Spark SQL）---> 业务数据库（MongoDB）

- 历史热门商品统计 -> `rate_more_products`

  统计所有历史数据中每个商品的评分数，代表商品历史热门度
    ```sql
    select productId, count(productId) as count from ratings group by productId order by count desc;
    ```
- 近期热门商品统计 -> `reat_more_recently_products`

  统计每月的商品评分个数，代表商品近期的热门度
    ```sql
    select productId, score, changeDate(timestamp) as yearmonth from ratings; -- rating_of_month
    select productId, count(productId) as count, yearmonth form rating_of_month group by yearmonth, productId order by yearmonth desc, count desc;
    ```
- 商品平均评分统计 -> `average_products`

    ```sql
    select productId, avg(score) as avg from ratings group by productId order by avs asc;
    ```

### 2. 基于 LFM 的离线推荐模块（OfflineRecommender）

> 离线统计服务（Spark MLlib）---> 业务数据库（MongoDB）

- 用 ALS 算法训练隐语义模型（LFM）

  `ratings -> DataSet[ProductRating] -> RDD[Rating(uid, productId, score)] -> ALS.train(trainData, rank, iterations, lambda)`

    - 均方根误差（RMSE）：均方误差的算术平方根，预测值与真实值之间的误差

      <div style="display: flex;">
        <img src="https://latex.codecogs.com/svg.image?RMSE=\sqrt{\frac&space;1N\sum_{t=1}^N(observed_t-predicted_t)^2}" style="margin: 5px 5px 5px 80px" />
      </div>
    - 参数调整：可以通过均方根误差，来多次调整参数值，选择 RMSE 最小的一组参数值
    - rank, iterations, lambda

- 计算用户推荐矩阵

    ```
    userRDD: RDD[Int], productRDD: RDD[Int] --> 笛卡尔积 ==>
    userProducts: RDD[uid, productId] --> model.predict(userProducts) ==>
    predictRating: RDD[Rating(uid, productId, predict)] --> groupBy ==>
    userGroupRatings: RDD[(uid, Seq[Rating])] --> sortBy(score).take(20) ==>
    userResc: RDD[uid, Seq(productId, score)]
    ```

- 计算商品相似度矩阵（为实时推荐做提前计算）

    <div style="display: flex;">
      <img src="https://latex.codecogs.com/svg.image?cos\theta&space;=\frac{a*b}{||&space;a||\times||b||}" style="margin: 5px 5px 5px 80px" />
    </div>

    ```
    model --> model.productFeatures ==>
    productFeatures: RDD[productId, DoubleMatrix], productFeatures: RDD[productId, DoubleMatrix] --> 笛卡尔积 ==>
    productSim: RDD[(productId, (productId, consinSim))] --> Filter(cosinSim > 0.6).groupByKey ==>
    productsSimGroup: RDD[(productId, Seq(productId, consinSim))]
    ```

### 3. 基于自定义模型的实时推荐模块（OnlineRecommender）

> 日志采集模块（Flume-ng）---> 消息缓冲服务（Kafka）、缓存数据库（Redis）---> 实时推荐服务（Spark Streaming）---> 业务数据库（MongoDB）

> 计算速度要快  
> 结果可以不是特别精确  
> 有预先设计好的推荐模型

基本原理：用户最近一段时间的口味是相似的  
备选商品推荐优先级：

<div style="display: flex;">
  <img src="https://latex.codecogs.com/svg.image?E_{uq}=&space;\frac{\sum_{r\in&space;RK}sim(q,r)\times&space;R_r}{sim\_sum}&plus;lgmax\left\{incount,1\right\}=lgmax\left\{recount,1\right\}" style="margin: 5px 5px 5px 80px" />
</div>

- `incount`：高分项，加分
- `recount`：低分项，减分

### 4. 其他形式的离线相似推荐模块（）

1. 找到商品 A 的相似商品 --> 与 A 有相同标签的商品，喜欢 A 的人同样喜欢的商品
2. 根据 UGC 的特征提取 --> 利用 TF-IDF 算法从商品内容标签中提取特征
3. 根据行为数据的相似度计算 --> Item-CF 根据行为数据，找到喜欢了商品 A 的用户，同时喜欢了那些商品，喜欢的人重合度越高相似度就越大

- 基于内容的推荐模块
    - 基于商品的用户标签信息，用 TF-IDF 算法提取特征向量
        <div style="display: flex;">
            <img src="https://latex.codecogs.com/svg.image?TF_{i,j}=\frac{n_{i,j}}{n_{*,j}}" style="margin: 5px 5px 5px 80px"/>
            <img src="https://latex.codecogs.com/svg.image?IDF_{i}=log\left(\frac{N&plus;1}{N_i&plus;1}\right)" style="margin: 5px 5px 5px 80px" />
        </div>
    - 计算特征向量的余弦相似度，从而得到商品的相似度列表
        <div style="display: flex;">
          <img src="https://latex.codecogs.com/svg.image?cos\theta=\frac{a\cdot&space;b}{\left\|a\right\|\times\left\|b\right\|}=\frac{\sum_ix_iy_i}{\sqrt{\sum_ix_i^2}\times\sqrt{\sum_iy_i^2}}" style="margin: 5px 5px 5px 80px" />
        </div>
    - 在商品详情页面、商品购买页面展示出来

- 基于物品的协同过滤推荐模块
    - 基于物品的协同过滤（Item-CF ），只需要收集用户的常规行为数据（点击、收藏、购买）就可以得到商品的相似度
    - "同现相似度"：利用行为数据计算不同商品间的相似度
        <div style="display: flex;">
          <img src="https://latex.codecogs.com/svg.image?w_{ij}=\frac{\left|&space;N_i&space;\cap&space;N_j&space;\right|}{\sqrt{\left|&space;N_i&space;\right|\left|&space;N_j&space;\right|}}" style="margin: 5px 5px 5px 80px" />
        </div>

        - 其中 Ni 是购买商品 i（或对商品 i 评分）的用户列表，Nj 是购买商品 j 的用户列表

### 5. 业务系统模块（mall）



## 三、冷启动问题处理

整个推荐系统更多的是依赖于用户的偏好信息进行商品的推荐，对于一个新注册的用户是没有任何偏好信息记录的，
那这个时候推荐就会出现问题，导致没有任何推荐的项目出现。

**解决办法**

通过用户首次登录时，为用户提供交互式的窗口来获取用户对物品的偏好，让用户勾选预设的兴趣标签。
当获取用户偏好之后，就可以直接给出相应类型商品的推荐。