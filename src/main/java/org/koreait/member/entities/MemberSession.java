package org.koreait.member.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@RedisHash(timeToLive = 3600L)
public class MemberSession implements Serializable {
    @Id
    private String key;

    private Object value;
}
