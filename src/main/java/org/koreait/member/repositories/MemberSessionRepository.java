package org.koreait.member.repositories;

import org.koreait.member.entities.MemberSession;
import org.springframework.data.repository.CrudRepository;

public interface MemberSessionRepository extends CrudRepository<MemberSession, String> {
}
