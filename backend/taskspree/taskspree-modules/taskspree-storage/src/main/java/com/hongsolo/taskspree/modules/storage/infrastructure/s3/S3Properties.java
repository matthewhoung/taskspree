package com.hongsolo.taskspree.modules.storage.infrastructure.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "s3")
@Getter
@Setter
public class S3Properties {

    /**
     * S3-compatible endpoint URL
     * For MinIO: http://localhost:9000
     * For AWS S3: https://s3.{region}.amazonaws.com
     */
    private String endpoint;

    /**
     * Access key / username
     */
    private String accessKey;

    /**
     * Secret key / password
     */
    private String secretKey;

    /**
     * AWS region (required even for MinIO)
     */
    private String region = "ap-east-2";

    /**
     * Bucket names for different purposes
     */
    private Buckets buckets = new Buckets();

    @Getter
    @Setter
    public static class Buckets {
        private String tasks = "taskspree-tasks";
        private String avatars = "taskspree-avatars";

        public Map<String, String> all() {
            Map<String, String> all = new HashMap<>();
            all.put("tasks", tasks);
            all.put("avatars", avatars);
            return all;
        }
    }
}
