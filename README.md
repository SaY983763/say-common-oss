# say-common-oss

# 1. 介绍

通用存储操作 支持所有兼容amazon-s3协议的云存储，如minio、oss、cos等

# 2. 软件架构

软件架构说明

# 3. 使用说明

## 3.1 推到私仓

```bash
mvn clean deploy
```

## 3.2 引入本包依赖

```xml

<dependency>
  <groupId>com.say.common</groupId>
  <artifactId>say-common-oss</artifactId>
  <version>1.0.0</version>
</dependency>
```

## 3.3 使用模板中实现的aws协议方法，调用对应存储

类上：@AllArgsConstructor
private final FileTemplate template;

## 3.4 配置文件，设置开关、对应存储相关配置，详细看下面

启动oss功能

```yaml
file:
  oss:
    enable: true
```

## 3.5 配置相关

### 3.5.1 阿里云OSS

  ```yaml
file:
  oss:
    enable: true
    endpoint: https://xxx.oss-cn-beijing.aliyuncs.com # 对象存储服务的URL
    bucketName: # 因为url本身代表了存储桶，这里空着不用配置
    accessKey: # 账户ak
    secretKey: # 账户sk
```

### 3.5.2 腾讯云COS

```yaml
file:
  oss:
    enable: true
    endpoint: https://xxx.cos.ap-beijing.myqcloud.com # 对象存储服务的URL
    bucketName: # 因为url本身代表了存储桶，这里空着不用配置
    accessKey: # 账户ak
    secretKey: # 账户sk
```

### 3.5.3 华为云OBS


```yaml
file:
  oss:
    enable: true
    endpoint: https://xxx.obs.cn-north-4.myhuaweicloud.com # 对象存储服务的URL
    bucketName: # 因为url本身代表了存储桶，这里空着不用配置
    accessKey: # 账户ak
    secretKey: # 账户sk
```

### 3.5.4 minio

```yaml
file:
  oss:
    enable: true
    endpoint: http://ip:port # 对象存储服务的URL
    bucketName: bucketName
    accessKey: xxx
    secretKey: xxx
```

# 4. 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

