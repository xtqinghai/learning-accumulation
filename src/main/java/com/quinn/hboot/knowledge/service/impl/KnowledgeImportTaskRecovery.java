package com.quinn.hboot.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeImportTaskMapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeSourceFileMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeImportTask;
import com.quinn.hboot.knowledge.model.entity.KnowledgeSourceFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Prevents import tasks from remaining forever in an in-progress state after an application restart.
 *
 * <p>Only the process that accepted an import task owns its in-memory execution. Once the
 * application starts again, those tasks cannot resume automatically, so they are changed to a
 * retryable failed state instead of leaving the UI polling indefinitely.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeImportTaskRecovery {
    static final Set<String> ACTIVE_STATUSES = Set.of(
        "PENDING", "PARSING", "SUMMARIZING", "SAVING_ARTICLE"
    );
    static final String INTERRUPTED_MESSAGE = "应用重启导致导入任务中断，请点击“重试”继续";

    private final KnowledgeImportTaskMapper taskMapper;
    private final KnowledgeSourceFileMapper sourceFileMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recoverInterruptedTasks() {
        List<KnowledgeImportTask> tasks = taskMapper.selectList(
            new LambdaQueryWrapper<KnowledgeImportTask>()
                .in(KnowledgeImportTask::getStatus, ACTIVE_STATUSES)
        );
        if (tasks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (KnowledgeImportTask task : tasks) {
            task.setStatus("FAILED");
            task.setCurrentStage("导入已中断");
            task.setErrorMessage(INTERRUPTED_MESSAGE);
            task.setUpdateTime(now);
            taskMapper.updateById(task);

            KnowledgeSourceFile source = sourceFileMapper.selectById(task.getSourceFileId());
            if (source != null) {
                source.setStatus("FAILED");
                source.setErrorMessage(INTERRUPTED_MESSAGE);
                source.setUpdateTime(now);
                sourceFileMapper.updateById(source);
            }
        }
        log.warn("Recovered {} interrupted knowledge import task(s)", tasks.size());
    }
}
