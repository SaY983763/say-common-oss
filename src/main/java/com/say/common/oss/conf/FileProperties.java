package com.say.common.oss.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件 配置信息 <p> bucket 设置公共读权限
 *
 * @author zrs
 */
@Data
@ConfigurationProperties(prefix = "file")
public class FileProperties {

  /**
   * 默认的存储桶名称
   */
  private String bucketName = "test";

  /**
   * oss 文件配置信息
   */
  private OssProperties oss;

}
