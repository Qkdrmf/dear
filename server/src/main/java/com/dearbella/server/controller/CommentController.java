package com.dearbella.server.controller;

import com.dearbella.server.domain.Comment;
import com.dearbella.server.domain.CommentLike;
import com.dearbella.server.dto.request.comment.CommentAddRequestDto;
import com.dearbella.server.dto.request.comment.CommentEditRequestDto;
import com.dearbella.server.dto.response.comment.CommentResponseDto;
import com.dearbella.server.service.comment.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
@Api(tags = {"댓글 API"})
public class CommentController {
    private final CommentService commentService;

    @ApiOperation("후기/커뮤니티 댓글 생성")
    @PostMapping("/add")
    public ResponseEntity<Comment> addComment(@RequestBody CommentAddRequestDto dto) {
        return ResponseEntity.ok(commentService.addComment(dto));
    }

    @ApiOperation("댓글 삭제")
    @DeleteMapping("/delete")
    public ResponseEntity deleteComment(@RequestParam Long commentId) {
        commentService.deleteComment(commentId);

        return ResponseEntity.ok().build();
    }

    @ApiOperation("댓글 수정")
    @PostMapping("/edit")
    public ResponseEntity<Comment> editComment(@RequestBody CommentEditRequestDto dto) {
        return ResponseEntity.ok(commentService.editComment(dto));
    }

    @ApiOperation("댓글 좋아요/취소")
    @GetMapping("/like")
    public ResponseEntity<String> likeComment(@RequestParam Long commentId) {
        return ResponseEntity.ok(commentService.likeComment(commentId));
    }
}
