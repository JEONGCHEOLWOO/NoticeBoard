package com.example.NoticeBoard.domain.comment.controller;

import com.example.NoticeBoard.domain.comment.service.CommentQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments/query")
@RequiredArgsConstructor
@Slf4j
public class CommentQueryController {

    private final CommentQueryService commentQueryService;
}
