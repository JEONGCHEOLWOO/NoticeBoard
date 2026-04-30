package com.example.NoticeBoard.global.batch;

import com.example.NoticeBoard.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentCountBatch {

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void syncCommentCount() {

        ScanOptions options = ScanOptions.scanOptions()
                .match("post:comment:count:*")
                .count(100)
                .build();

        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options);

        while (cursor.hasNext()) {
            String key = new String(cursor.next());
            try {
                Long postId = Long.parseLong(key.substring(key.lastIndexOf(":") + 1));
                Long count = Long.parseLong(redisTemplate.opsForValue().get(key));

                if (count > 0) {
                    postRepository.increaseCommentCount(postId, count);
                } else {
                    postRepository.decreaseCommentCount(postId, count);
                }

                redisTemplate.delete(key);
                log.info("댓글 count 동기화 완료 postId={}, count={}", postId, count);
            } catch (Exception e) {
                log.error("Comment Count batch 처리 실패 key={}", key, e);
            }
        }
    }
}