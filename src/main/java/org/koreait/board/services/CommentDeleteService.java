package org.koreait.board.services;

import lombok.RequiredArgsConstructor;
import org.koreait.board.entities.Comment;
import org.koreait.board.repositories.CommentRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@RequiredArgsConstructor
public class CommentDeleteService {
    private final CommentRepository repository;
    private final CommentInfoService infoService;
    private final CommentUpdateService updateService;

    public Comment process(Long seq) {
        Comment comment = infoService.get(seq);
        Long boardDataSeq = comment.getItem().getSeq();
        repository.delete(comment);
        repository.flush();

        // 게시글의 댓글 갯수 업데이트
        updateService.updateCommentCount(boardDataSeq);

        return comment;
    }
}