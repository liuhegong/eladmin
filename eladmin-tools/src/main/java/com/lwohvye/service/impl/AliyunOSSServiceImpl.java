package com.lwohvye.service.impl;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import com.lwohvye.service.AliyunOSSService;
import com.lwohvye.utils.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hongyan Wang
 * @date 2021年09月05日 16:22
 */
@Slf4j
@Service
public class AliyunOSSServiceImpl implements AliyunOSSService {

    // 引入starter后，可直接注入
    @Autowired
    private OSSClient ossClient;

    // 存储桶名称
    @Value("alibaba.cloud.oss.bucket-name:")
    private String bucketName;
    // 文件目录
    @Value("alibaba.cloud.oss.file-path:")
    private String filePath;

    @SneakyThrows
    public void MultipartUploadFile(MultipartFile file) {
// yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//        String endpoint = "yourEndpoint";
// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "yourAccessKeyId";
//        String accessKeySecret = "yourAccessKeySecret";

// 填写Bucket名称，例如examplebucket。
//        String bucketName = "bucketName";
// 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        // TODO: 2021/9/5 待确认是否带后缀
        String objectName = filePath + "/" + file.getOriginalFilename();

// 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 创建InitiateMultipartUploadRequest对象。
        var request = new InitiateMultipartUploadRequest(bucketName, objectName);

// 如果需要在初始化分片时设置文件存储类型，请参考以下示例代码。
// ObjectMetadata metadata = new ObjectMetadata();
// metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
// request.setObjectMetadata(metadata);

// 初始化分片。
        var upresult = ossClient.initiateMultipartUpload(request);
// 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。
        var uploadId = upresult.getUploadId();

        log.info("文件名称：{} || 事件标识：{} ", objectName, uploadId);

// partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        List<PartETag> partETags = new ArrayList<>();
// 每个分片的大小，用于计算文件有多少个分片。单位为字节。
        final long partSize = 1 * 1024 * 1024L;   //1 MB。

// 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
        final File sampleFile = FileUtil.toFile(file);
        long fileLength = sampleFile.length();
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }
// 遍历分片上传。
        for (int i = 0; i < partCount; i++) {
            long startPos = i * partSize;
            long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
            UploadPartRequest uploadPartRequest;
            try (InputStream instream = new FileInputStream(sampleFile)) {
                // 跳过已经上传的分片。
                instream.skip(startPos);
                uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(instream);
            }
            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
            uploadPartRequest.setPartSize(curPartSize);
            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
            uploadPartRequest.setPartNumber(i + 1);
            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
            partETags.add(uploadPartResult.getPartETag());
        }


// 创建CompleteMultipartUploadRequest对象。
// 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
        var completeMultipartUploadRequest = new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

// 如果需要在完成文件上传的同时设置文件访问权限，请参考以下示例代码。
// completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.PublicRead);

// 完成上传。
        var completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        log.info("上传结果：{} ", completeMultipartUploadResult.getETag());
// 关闭OSSClient。 引用starter后，在服务关闭时，会自行shutdown
//        ossClient.shutdown();
    }

    @SneakyThrows
    @Override
    public void downloadFile(String ossUri, String downloadPath) {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//        String endpoint = "yourEndpoint";
// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "yourAccessKeyId";
//        String accessKeySecret = "yourAccessKeySecret";
// 填写Bucket名称，例如examplebucket。
//        String bucketName = "examplebucket";
// 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
//        String objectName = "exampledir/exampleobject.txt";

// 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 下载请求，10个任务并发下载，启动断点续传。
        DownloadFileRequest downloadFileRequest = new DownloadFileRequest(bucketName, ossUri);
        downloadFileRequest.setDownloadFile(downloadPath);
        downloadFileRequest.setPartSize(1 * 1024 * 1024);
        downloadFileRequest.setTaskNum(10);
        downloadFileRequest.setEnableCheckpoint(true);
// 设置断点记录文件的完整路径。只有当Object下载中断产生了断点记录文件后，如果需要继续下载该Object，才需要设置对应的断点记录文件。
//downloadFileRequest.setCheckpointFile("D:\\localpath\\examplefile.txt.dcp");

// 下载文件。
        DownloadFileResult downloadRes = ossClient.downloadFile(downloadFileRequest);
// 下载成功时，会返回文件元信息。
        ObjectMetadata objectMetadata = downloadRes.getObjectMetadata();
        log.info("下载成功，ETag: {} || LastModified: {} || UserMetadata: {} ",
                objectMetadata.getETag(), objectMetadata.getLastModified(), objectMetadata.getUserMetadata().get("meta"));

// 关闭OSSClient。
//        ossClient.shutdown();
    }
}
