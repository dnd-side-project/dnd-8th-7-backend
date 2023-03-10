package com.dnd8th.dto.task;

import com.dnd8th.entity.Block;
import com.dnd8th.entity.Task;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TaskCreateRequest {

    private String content;

    @Builder
    public TaskCreateRequest(String content) {
        this.content = content;
    }

    public Task toEntity(Block block) {
        return Task.builder()
                .block(block)
                .contents(content)
                .status(false)
                .build();
    }
}
