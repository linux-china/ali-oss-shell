介绍
====================================
阿里云弹性存储终端控制台，通过控制台方式管理OSS上的文件，如查看、上传、下载和修改属性等。


### OSS Console手册
关于OSS Console的手册，请访问： https://github.com/linux-china/ali-oss-java-cli/wiki

### 开发步骤
首先git clone出代码，然后执行

    mvn -DskipTests clean package，

接下来执行

    java -jar target/ali-oss-java-cli-1.0.0.jar
即可进入控制台。
打包分发：

    mvn -DskipTests clean package assembly:assembly
然后将target目录下tar.gz和zip文件提供下载即可。

### 开发指南
整个系统包含ConfigService和AliyunOssService，ConfigService包含相关的配置的服务，如保存Access Token和全局配置等。
AliyunOssService负责和OSS进行交互，如获取OSS Object信息，上传文件等。
由于OSS主要包含Bucket和Object，所以我们介入OSSUri类来标识Object，以后相关的操作都是基于object uri完成的。
整体类图如下：

![系统类图](https://github.com/linux-china/ali-oss-java-cli/wiki/assets/img/ali-oss-java-cli-class-diagram.png)

### Road Map

* 分片上传
* 统计支持：dump bucket下的所有object的基本信息，然后进行Lucene索引，支持自定义查询。

### Issues

* List Objects能够显示匹配的object总数

### 控制台截屏
![OSS Console](https://github.com/linux-china/ali-oss-java-cli/wiki/assets/img/console_shot.png)

