介绍
====================================
阿里云弹性存储终端控制台，通过控制台方式管理OSS云端的Bucket和Object。

### OSS Console手册

关于OSS Console的手册，请访问： https://github.com/linux-china/ali-oss-shell/wiki

### 开发步骤

Check out代码，编译，最后执行。步骤如下：

    git clone git://github.com/linux-china/ali-oss-shell.git
    cd ali-oss-shell
    mvn -DskipTests clean package

接下来执行

    java -jar target/ali-oss-java-cli-1.0.0.jar

即可进入控制台执行操作。
打包分发：

    mvn -DskipTests clean package assembly:assembly

然后将target目录下tar.gz和zip文件提供下载即可。

### 开发指南

整个系统包含ConfigService和AliyunOssService，ConfigService包含相关的配置的服务，如保存Access Token和全局配置等。
AliyunOssService负责和OSS进行交互，如获取OSS Object信息，上传文件等。
由于OSS主要包含Bucket和Object，所以我们介入OSSUri类来标识Object，以后相关的操作都是基于object uri完成的。
整体类图如下：

![系统类图](https://github.com/linux-china/ali-oss-java-cli/wiki/assets/img/ali-oss-java-cli-class-diagram.png)

### 如何调试控制台程序

OSS Console运行在terminal中，当然你也可以在IDEA中直接以debug方式运行程序，但是一些功能会缺失，如颜色显示，自动提示等，这个时候需在terminal中运行，但是我们也需要调试程序，
所以我们我们要以debug状态启动App，然后在IDEA中以Remote Debug方式连接到JVM上进行调试。运行参数如下

    java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar target/ali-oss-java-cli-1.0.0.jar

接下来就是在IDEA中创建一个Remote Debug运行项即可。

### OSS Console颜色列表

整个OSS Console主要涉及四种颜色：绿色、白色、黄色和红色。

* 绿色: Spring Shell的默认颜色，显示命令返回的字符串，通常是内容，提示消息等。
* 白色: Console在操作中输出的文本，如批量上传、下载和删除等。
* 黄色: 用于提示，如提示用户选择bukcet，设置必需参数等。
* 红色: 系统中的错误信息，如和Aliyun OSS通讯异常、非法操作等。

**注意:** 命令的返回值可以使用wrappedAsRed进行文本颜色设置，可以显示不同的颜色。

### Road Map

* 分片上传
* 允许设置一次显示object的最大数量
* 多参数时key为空的自动提示
* 统计支持：dump bucket下的所有object的基本信息，然后进行Lucene索引，支持自定义查询。

### Change Log

* 20220212: 更新至Spring Boot 2.6.3, Spring Shell 2.1.0-M2
* 20121031: 将域名调整为oss.aliyuncs.com，api endpoint也进行了调整
* 20121031: 修复complete有候选项时的错误提示
* 20121031: 更新aliyun-openservice版本为1.0.8

### Issues

* List Objects能够显示匹配的object总数

### 控制台截屏

![OSS Console](https://github.com/linux-china/ali-oss-java-cli/wiki/assets/img/console_shot.png)

