package org.koreait.member.services;

import lombok.RequiredArgsConstructor;
import org.koreait.member.entities.MemberSession;
import org.koreait.member.libs.MemberUtil;
import org.koreait.member.repositories.MemberSessionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@RequiredArgsConstructor
public class MemberSessionService {
    private final MemberSessionRepository repository;
    private final MemberUtil memberUtil;

    /**
     * 사용자별(비회원 + 회원) 키,값의 쌍으로 데이터를 저장
     *
     * @param key
     * @param value
     * @param <T>
     */
    public <T> void set(String key, T value) {

        MemberSession session = new MemberSession();
        session.setKey(getKey(key));
        session.setValue(value);

        repository.save(session);
    }

    // 사용자별 데이터를 조회
    public <R> R get(String key) {
        Object item = repository.findById(getKey(key)).orElse(null);

        return item == null ? null : (R)item;
    }

    // 사용자별 데이터 삭제
    public void remove(String key) {
        repository.deleteById(getKey(key));
    }

    private String getKey(String key) {
        int userHash = memberUtil.getUserHash();
        return String.format("%s_%s", userHash, key);
    }
}
