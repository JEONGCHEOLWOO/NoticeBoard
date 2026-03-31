package com.example.NoticeBoard.domain.comment.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentEventProducer {

    public void sendCommentCreatedEvent(Long id) {
    }

    public void sendCommentUpdateEvent(Long commentId) {
    }

    public void sendCommentDeleteEvent(Long commentId) {
    }
}
