package com.example.demo.base.command;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event 基礎實體類，此類包含一些通用的欄位，如: 訊息識別符、目標代碼。
 * */
@Data
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class BaseIdempotentCommand {

    /**
     * 消息的唯一識別符
     */
    protected String eventLogUuid;

    /**
     * targetId
     */
    protected String targetId;

}
