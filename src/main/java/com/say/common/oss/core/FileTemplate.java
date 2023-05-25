package com.say.common.oss.core;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件操作模板
 *
 * @author zrs
 */
public interface FileTemplate extends InitializingBean {

  @Override
  default void afterPropertiesSet() {
  }

  /**
   * 创建bucket
   *
   * @param bucketName bucket名称
   */
  void createBucket(String bucketName);

  /**
   * 获取全部bucket
   *
   * @return Bucket 列表
   */
  List<Bucket> getAllBuckets();

  /**
   * 根据bucket获取bucket详情
   *
   * @param bucketName bucket名称
   * @return Optional<Bucket>
   */
  Optional<Bucket> getBucket(String bucketName);

  /**
   * @param bucketName bucket名称
   */
  void removeBucket(String bucketName);

  /**
   * 上传文件
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @param stream 文件流
   * @param contextType 文件类型
   */
  void putObject(String bucketName, String objectName, InputStream stream, String contextType);

  /**
   * 上传文件
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @param stream 文件流
   * @param size 大小
   * @param contextType 类型
   */
  PutObjectResult putObject(String bucketName, String objectName, InputStream stream,
      long size, String contextType);

  /**
   * 获取文件信息
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   */
  S3Object getObjectInfo(String bucketName, String objectName);

  /**
   * 上传文件
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @param stream 文件流
   */
  void putObject(String bucketName, String objectName, InputStream stream);

  /**
   * 获取文件
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @return 二进制流
   */
  S3Object getObject(String bucketName, String objectName);

  /**
   * 删除文件
   *
   * @param bucketName bucketName
   * @param objectName objectName
   */
  void deleteObject(String bucketName, String objectName);

  /**
   * 大文件分段上传
   *
   * @param file MultipartFile
   * @param bucketName bucketName
   * @param objectName objectName
   * @param minPartSize 每片大小，单位：字节（eg：5242880 <- 5m）
   */
  void uploadMultipartFileByPart(MultipartFile file, String bucketName, String objectName,
      int minPartSize);

  /**
   * 根据文件前置查询文件
   *
   * @param bucketName bucket名称
   * @param prefix 前缀
   * @param recursive 是否递归查询
   * @return S3ObjectSummary 列表
   */
  List<S3ObjectSummary> getAllObjectsByPrefix(String bucketName, String prefix, boolean recursive);

  /**
   * 查询文件版本
   *
   * @param bucketName bucket名称
   * @return S3ObjectSummary 列表
   */
  List<S3VersionSummary> getAllObjectsVersionsByPrefixV2(String bucketName, String objectName);

  /**
   * 获取文件外链
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @param expires 过期时间 <=7
   * @return url
   */
  String generatePresignedUrl(String bucketName, String objectName, Integer expires);
}
