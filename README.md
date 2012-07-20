介绍
====================================
阿里云弹性存储Java CLI，通过控制台方式管理OSS上的文件，如查看、上传、下载和修改属性等。
OSS Object URL: oss://bucket/filepath.xxx

### 如何使用
执行目录解压，然后运行aliyun_oss.sh或者aliyun_oss.bat即可，当然你需要Java 1.6+的运行环境。

* 进入控制台，运行 config --id aliyunossid --key aliyunoss key
* 使用df或者ls列出bucket
* 使用use bucket进行bucket切换

### 命令
常见命令列表：

* df: 显示bucket列表
* ls: 显示当前的buckt，目录或者文件列表，最多100条
* cd: 更改目录
* mv: 移动文件
* cp: 拷贝文件
* rm: 删除文件
* file: 文件详情
* put: 上传文件
* get: 下载文件



