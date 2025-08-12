package org.koreait.board.services;

import lombok.RequiredArgsConstructor;
import org.koreait.board.entities.BoardData;
import org.koreait.board.repositories.BoardDataRepository;
import org.koreait.file.services.FileDeleteService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Lazy
@Service
@RequiredArgsConstructor
public class BoardDeleteService {
    private final BoardInfoService infoService;
    private final BoardDataRepository repository;
    private final FileDeleteService deleteService;

    public void process(Long seq) {
        BoardData item = infoService.get(seq);
        if (item.getCommentCount() > 0) { // 댓글이 남아 있는 경우 소프트 삭제
            item.setDeletedAt(LocalDateTime.now());
            repository.saveAndFlush(item);
            return;
        }

        // 댓글이 남아있지 않은 경우는 DB에서 삭제 처리
        String gid = item.getGid();
        repository.delete(item);
        deleteService.process(gid);

        repository.flush();
    }
}