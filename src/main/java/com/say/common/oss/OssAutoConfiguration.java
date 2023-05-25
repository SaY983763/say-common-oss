package com.say.common.oss;


import com.say.common.oss.conf.FileProperties;
import com.say.common.oss.service.OssTemplate;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * aws 自动配置类
 *
 * @author zrs
 */
@AllArgsConstructor
@EnableConfigurationProperties({FileProperties.class})
public class OssAutoConfiguration {

  private final FileProperties properties;

  @Bean
  @Primary
  @ConditionalOnMissingBean(OssTemplate.class)
  @ConditionalOnProperty(name = "file.oss.enable", havingValue = "true")
  public OssTemplate ossTemplate() {
    return new OssTemplate(properties);
  }


}
