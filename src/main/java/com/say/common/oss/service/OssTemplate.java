package com.say.common.oss.service;

import cn.hutool.core.util.ObjectUtil;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.util.IOUtils;
import com.say.common.oss.conf.FileProperties;
import com.say.common.oss.core.FileTemplate;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.multipart.MultipartFile;

/**
 * aws-s3 通用存储操作 支持所有兼容s3协议的云存储
 *
 * @author zrs
 */
@Slf4j
@RequiredArgsConstructor
public class OssTemplate implements InitializingBean, FileTemplate {

  private final FileProperties properties;

  private AmazonS3 amazonS3;

  @Override
  public void afterPropertiesSet() {
    ClientConfiguration clientConfiguration = new ClientConfiguration();
    clientConfiguration.setMaxConnections(properties.getOss().getMaxConnections());

    AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
        properties.getOss().getEndpoint(), properties.getOss().getRegion());
    AWSCredentials awsCredentials = new BasicAWSCredentials(properties.getOss().getAccessKey(),
        properties.getOss().getSecretKey());
    AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(
        awsCredentials);
    this.amazonS3 = AmazonS3Client.builder().withEndpointConfiguration(endpointConfiguration)
        .withClientConfiguration(clientConfiguration).withCredentials(awsCredentialsProvider)
        .disableChunkedEncoding()
        .withPathStyleAccessEnabled(properties.getOss().getPathStyleAccess()).build();
  }

  /**
   * 创建bucket
   *
   * @param bucketName bucket名称
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/CreateBucket">AWS API
   * Documentation</a>
   */
  @Override
  @SneakyThrows
  public void createBucket(String bucketName) {
    // 检验bucket是否存在
    if (!amazonS3.doesBucketExistV2(bucketName)) {
      amazonS3.createBucket((bucketName));
    }
  }

  /**
   * 获取全部bucket
   * <p>
   *
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/ListBuckets">AWS
   * API Documentation</a>
   */
  @Override
  @SneakyThrows
  public List<Bucket> getAllBuckets() {
    return amazonS3.listBuckets();
  }

  /**
   * 根据bucket获取bucket详情
   *
   * @param bucketName bucket名称
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/ListBuckets">AWS
   * API Documentation</a>
   */
  @Override
  @SneakyThrows
  public Optional<Bucket> getBucket(String bucketName) {
    return amazonS3.listBuckets().stream().filter(b -> b.getName().equals(bucketName)).findFirst();
  }

  /**
   * @param bucketName bucket名称
   * @see <a href=
   * "http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/DeleteBucket">AWS API
   * Documentation</a>
   */
  @Override
  @SneakyThrows
  public void removeBucket(String bucketName) {
    amazonS3.deleteBucket(bucketName);
  }

  /**
   * 上传文件，指定文件类型
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @param stream 文件流
   * @param contextType 文件类型
   * @throws Exception
   */
  @Override
  @SneakyThrows
  public void putObject(String bucketName, String objectName, InputStream stream,
      String contextType) {
    putObject(bucketName, objectName, stream, stream.available(), contextType);
  }

  /**
   * 上传文件
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @param stream 文件流
   * @param size 大小
   * @param contextType 类型
   * @throws Exception
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/PutObject">AWS
   * API Documentation</a>
   */
  @Override
  @SneakyThrows
  public PutObjectResult putObject(String bucketName, String objectName, InputStream stream,
      long size, String contextType) {
    byte[] bytes = IOUtils.toByteArray(stream);
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(size);
    objectMetadata.setContentType(contextType);
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    // 上传
    return amazonS3.putObject(bucketName, objectName, byteArrayInputStream, objectMetadata);
  }

  /**
   * 获取文件信息
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/GetObject">AWS
   * API Documentation</a>
   */
  @Override
  @SneakyThrows
  public S3Object getObjectInfo(String bucketName, String objectName) {
    @Cleanup
    S3Object object = amazonS3.getObject(bucketName, objectName);
    return object;
  }

  /**
   * 上传文件
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @param stream 文件流
   * @throws Exception
   */
  @Override
  @SneakyThrows
  public void putObject(String bucketName, String objectName, InputStream stream) {
    putObject(bucketName, objectName, stream, stream.available(), "application/octet-stream");
  }

  /**
   * 获取文件
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @return 二进制流
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/GetObject">AWS
   * API Documentation</a>
   */
  @Override
  @SneakyThrows
  public S3Object getObject(String bucketName, String objectName) {
    return amazonS3.getObject(bucketName, objectName);
  }

  /**
   * 删除文件
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @throws Exception
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/DeleteObject">AWS
   * API Documentation</a>
   */
  @Override
  @SneakyThrows
  public void deleteObject(String bucketName, String objectName) {
    amazonS3.deleteObject(bucketName, objectName);
  }

  /**
   * 大文件分段上传
   *
   * @param file MultipartFile
   * @param bucketName bucketName
   * @param objectName objectName
   * @param minPartSize 每片大小，单位：字节（eg：5242880 <- 5m）
   */
  @Override
  public void uploadMultipartFileByPart(MultipartFile file, String bucketName, String objectName,
      int minPartSize) {
    if (ObjectUtil.isEmpty(file)) {
      log.error("file is empty");
    }
    // 计算分片大小
    long size = file.getSize();
    // 得到总共的段数，和 分段后，每个段的开始上传的字节位置
    List<Long> positions = Collections.synchronizedList(new ArrayList<>());
    long filePosition = 0;
    while (filePosition < size) {
      positions.add(filePosition);
      filePosition += Math.min(minPartSize, (size - filePosition));
    }
    if (log.isDebugEnabled()) {
      log.debug("总大小：{}，分为{}段", size, positions.size());
    }

    // 创建一个列表保存所有分传的 PartETag, 在分段完成后会用到
    List<PartETag> partETags = Collections.synchronizedList(new ArrayList<>());

    // 第一步，初始化，声明下面将有一个 Multipart Upload
    // 设置文件类型
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(file.getContentType());

    InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName,
        objectName, metadata);
    InitiateMultipartUploadResult initResponse = this.initiateMultipartUpload(initRequest);
    if (log.isDebugEnabled()) {
      log.debug("开始上传");
    }
    //声明线程池
    ExecutorService exec = Executors.newFixedThreadPool(3);
    long begin = System.currentTimeMillis();
    try {
      // MultipartFile 转 File
      File toFile = multipartFileToFile(file);
      for (int i = 0; i < positions.size(); i++) {
        int finalI = i;
        exec.execute(() -> {
          long time1 = System.currentTimeMillis();
          UploadPartRequest uploadRequest = new UploadPartRequest()
              .withBucketName(bucketName)
              .withKey(objectName)
              .withUploadId(initResponse.getUploadId())
              .withPartNumber(finalI + 1)
              .withFileOffset(positions.get(finalI))
              .withFile(toFile)
              .withPartSize(Math.min(minPartSize, (size - positions.get(finalI))));
          // 第二步，上传分段，并把当前段的 PartETag 放到列表中
          partETags.add(this.uploadPart(uploadRequest).getPartETag());
          if (log.isDebugEnabled()) {
            log.debug("第{}段上传耗时：{}", finalI + 1, (System.currentTimeMillis() - time1));
          }
        });
      }
      //任务结束关闭线程池
      exec.shutdown();
      //判断线程池是否结束，不加会直接结束方法
      while (true) {
        if (exec.isTerminated()) {
          break;
        }
      }

      // 第三步，完成上传，合并分段
      CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
          bucketName,
          objectName,
          initResponse.getUploadId(), partETags);
      this.completeMultipartUpload(compRequest);

      //删除本地缓存文件
      if (toFile != null && !toFile.delete()) {
        log.error("Failed to delete cache file");
      }
    } catch (Exception e) {
      this.abortMultipartUpload(
          new AbortMultipartUploadRequest(bucketName, objectName,
              initResponse.getUploadId()));
      log.error("Failed to upload, " + e.getMessage());
    }
    if (log.isDebugEnabled()) {
      log.debug("总上传耗时：{}", (System.currentTimeMillis() - begin));
    }
  }

  /**
   * 根据文件前置查询文件集合
   *
   * @param bucketName bucket名称
   * @param prefix 前缀
   * @param recursive 是否递归查询
   * @return S3ObjectSummary 列表
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/ListObjects">AWS
   * API Documentation</a>
   */
  @Override
  @SneakyThrows
  public List<S3ObjectSummary> getAllObjectsByPrefix(String bucketName, String prefix,
      boolean recursive) {
    ObjectListing objectListing = amazonS3.listObjects(bucketName, prefix);
    return new ArrayList<>(objectListing.getObjectSummaries());
  }

  /**
   * 查询文件版本
   *
   * @param bucketName bucket名称
   * @return S3ObjectSummary 列表
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/ListObjects">AWS
   * API Documentation</a>
   */
  @Override
  @SneakyThrows
  public List<S3VersionSummary> getAllObjectsVersionsByPrefixV2(String bucketName,
      String objectName) {
    VersionListing versionListing = amazonS3.listVersions(bucketName, objectName);
    return new ArrayList<>(versionListing.getVersionSummaries());
  }

  /**
   * 获取文件外链
   *
   * @param bucketName bucket名称
   * @param objectName 文件名称
   * @param expires 过期时间 <=7
   * @return url
   */
  @Override
  @SneakyThrows
  public String generatePresignedUrl(String bucketName, String objectName, Integer expires) {
    Date date = new Date();
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DAY_OF_MONTH, expires);
    URL url = amazonS3.generatePresignedUrl(bucketName, objectName, calendar.getTime());
    return url.toString();
  }

  /**
   * 初始化，声明有一个Multipart Upload
   *
   * @param initRequest 初始化请求
   * @return 初始化返回
   */
  private InitiateMultipartUploadResult initiateMultipartUpload(
      InitiateMultipartUploadRequest initRequest) {
    return amazonS3.initiateMultipartUpload(initRequest);
  }

  /**
   * 上传分段
   *
   * @param uploadRequest 上传请求
   * @return 上传分段返回
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/UploadPart">AWS
   * API Documentation</a>
   */
  private UploadPartResult uploadPart(UploadPartRequest uploadRequest) {
    return amazonS3.uploadPart(uploadRequest);
  }

  /**
   * 分段合并
   *
   * @param compRequest 合并请求
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/CompleteMultipartUpload">AWS
   * API Documentation</a>
   */
  private CompleteMultipartUploadResult completeMultipartUpload(
      CompleteMultipartUploadRequest compRequest) {
    return amazonS3.completeMultipartUpload(compRequest);
  }

  /**
   * 中止分片上传
   *
   * @param uploadRequest 中止文件上传请求
   * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/s3-2006-03-01/AbortMultipartUpload">AWS
   * API Documentation</a>
   */
  private void abortMultipartUpload(AbortMultipartUploadRequest uploadRequest) {
    amazonS3.abortMultipartUpload(uploadRequest);
  }

  /**
   * MultipartFile 转 File
   */
  private File multipartFileToFile(MultipartFile file) throws Exception {
    File toFile = null;
    if (file.equals("") || file.getSize() <= 0) {
      file = null;
    } else {
      InputStream ins = null;
      ins = file.getInputStream();
      toFile = new File(file.getOriginalFilename());
      //获取流文件
      OutputStream os = new FileOutputStream(toFile);
      int bytesRead = 0;
      byte[] buffer = new byte[8192];
      while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
        os.write(buffer, 0, bytesRead);
      }
      os.close();
      ins.close();
    }
    return toFile;
  }

}
