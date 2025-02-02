package com.say.common.oss.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * aws 配置信息
 *
 * @author zrs
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

  /**
   * 对象存储服务的URL
   */
  private String endpoint;

  /**
   * 自定义域名
   */
  private String customDomain;

  /**
   * true path-style nginx 反向代理和S3默认支持 pathStyle {http://endpoint/bucketname} false
   * <p/>
   * supports virtual-hosted-style 阿里云等需要配置为 virtual-hosted-style
   * <p/>
   * 模式{http://bucketname.endpoint}
   */
  private Boolean pathStyleAccess = true;

  /**
   * 应用ID
   */
  private String appId;

  /**
   * 区域
   */
  private String region;

  /**
   * Access key就像用户ID，可以唯一标识你的账户
   */
  private String accessKey;

  /**
   * Secret key是你账户的密码
   */
  private String secretKey;

  /**
   * 最大线程数，默认： 100
   */
  private Integer maxConnections = 100;

}
