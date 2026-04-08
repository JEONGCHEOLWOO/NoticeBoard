package com.example.NoticeBoard.domain.post.batch;

import com.example.NoticeBoard.domain.post.event.PostEventProducer;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostDeleteBatch {

    private final PostRepository postRepository;
    private final PostEventProducer postEventProducer;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void hardDeletePosts(){
        LocalDateTime deletereqday = LocalDateTime.now().minusDays(30);

        // 삭제 요청이 30일이 지난 postId 조회
        List<Long> postIds = postRepository.findDeletedPostIdsBefore(deletereqday);

        if (postIds.isEmpty()){
            log.info("삭제 대상 게시글 없음");
            return;
        }

        // DB에서 Batch 삭제
        postRepository.deleteAllByIdInBatch(postIds);

        log.info("삭제 요청 30일 지난 게시글 삭제 완료(Hard Delete) count={}", postIds.size());

        // kafka로 Batch 이벤트 전송 (ES 동기화)
        postEventProducer.sendPostDeleteBatchEvent(postIds);
    }
}
