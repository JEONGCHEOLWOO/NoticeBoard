package com.example.NoticeBoard.global.batch;

import com.example.NoticeBoard.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeCountBatch {

    private final StringRedisTemplate stringRedisTemplate;
    private final PostRepository postRepository;

    @Scheduled(fixedRate = 300000)
    public void syncLikeCount(){

        ScanOptions options = ScanOptions.scanOptions()
                .match("post:like:count:*")
                .count(100)
                .build();

        Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options);

        while (cursor.hasNext()){
            String key = new String(cursor.next());
            Long postId = Long.parseLong(key.split(":")[3]);
            int count = Integer.parseInt(stringRedisTemplate.opsForValue().get(key));

            postRepository.incrementLikeCount(postId, count);
            stringRedisTemplate.delete(key);
        }

    }
}
