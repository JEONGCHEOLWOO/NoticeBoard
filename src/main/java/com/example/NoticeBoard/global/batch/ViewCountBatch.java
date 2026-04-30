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

// Redis에 있는 캐시를 DB에 동기화 하는 클래스
@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountBatch {

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;

    // 조회수 증가 Batch - KEYS 대신 SCAN 사용, 5분마다 갱신
    // 왜 KEY를 안쓰고 SCAN으로 썼냐?
    // -> KEY를 사용할 경우 Redis 전체를 스캔하게 되는데 Redis는 싱글 스레드이기 때문에 해당 명령이 실행되는 동안 다른 모든 명령 실행이 블로킹이 되서 성능 저하와 장애가 발생할 가능성이 매우 크다.
    // SCAN은 점진적으로 반복 순회를 통해서 스캔하기 때문에 명령을 거의 차단하지 않는 SCAN을 이용하였다.
    // 현재 코드는 새로고침을 하면 조회수가 증가함. 추후 IP나 로그인 사용자 기반으로 10분을 제한걸어서 조회수 어뷰징을 막을 예정.
    // SCAN cursor [MATCH pattern] [COUNT count] 구조. cursor가 0에서 시작해서 0이 되면 모든 collection을 순회했다는 의미로 종료됨.
    // SCAN 0 MATCH post:view:* COUNT 100
    @Scheduled(fixedRate = 300000) // 5분 마다 갱신
    @Transactional
    public void syncViewCount(){
        ScanOptions options = ScanOptions.scanOptions()
                .match("post:view:count:*")
                .count(100) // 100개 단위로 나눠서 순회 - 기본값 10. Sweet Spot을 찾아서 블로킹 문제를 유발하지 않고 실행시간도 길지 않은 부분을 찾아서 설정하는게 좋음.
                .build();

        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options);

        while (cursor.hasNext()){
            String key = new String(cursor.next());
            try{
                Long postId = Long.parseLong(key.substring(key.lastIndexOf(":") + 1));
                Long count = Long.parseLong(redisTemplate.opsForValue().get(key)); // 증분값

                postRepository.increaseViewCount(postId, count);
                redisTemplate.delete(key);
                log.info("조회수 count 동기화 완료 postId={}, count={}", postId, count);
            } catch (Exception e) {
                log.error("View Count batch 처리 실패 key={}", key, e);
            }
        }
    }
}
