package org.koreait.member.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.koreait.member.entities.MemberSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RepositoryTest {
    @Autowired
    private MemberSessionRepository repository;

    @Test
    @DisplayName("레디스 레포지토리 저장 테스트")
    void saveTest() {
        MemberSession session = new MemberSession();
        session.setKey("testkey1");
        session.setValue("testValue");

        repository.save(session);
    }
    
    @Test
    @DisplayName("레디스 레포지토리 조회 테스트")
    void retrieveTest() {
        MemberSession session = repository.findById("testkey1").orElse(null);

        String value = (String)session.getValue();
        System.out.println("value:" + value);
    }
}
