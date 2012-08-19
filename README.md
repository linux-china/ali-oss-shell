介绍
====================================
阿里云弹性存储Java CLI Console，通过控制台方式管理OSS上的文件，如查看、上传、下载和修改属性等。


### Java CLI Console手册
关于Aliyun Java CLI Console的手册，请访问： https://github.com/linux-china/ali-oss-java-cli/wiki

### 开发步骤
首先git clone出代码，然后执行

    mvn -DskipTests clean package，

接下来执行

    java -jar target/ali-oss-java-cli-1.0.0.jar
即可进入控制台。
打包分发：

    mvn -DskipTests clean package assembly:assembly
然后将target目录下tar.gz和zip文件提供下载即可。


### Road Map

* 添加更新metadata
* 统一使用OSS URI方式
* 支持命令行直接调用命令文件，直接命令调用，如 mysql -u root -p < ./xxx.sql 方式
* 文档更新

### 控制台截屏
![OSS Console](https://github.com/linux-china/ali-oss-java-cli/wiki/assets/img/console_shot.png)

