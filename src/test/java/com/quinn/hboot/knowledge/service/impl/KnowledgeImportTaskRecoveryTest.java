package com.quinn.hboot.knowledge.service.impl;

import com.quinn.hboot.knowledge.mapper.KnowledgeImportTaskMapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeSourceFileMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeImportTask;
import com.quinn.hboot.knowledge.model.entity.KnowledgeSourceFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeImportTaskRecoveryTest {

    @Mock
    private KnowledgeImportTaskMapper taskMapper;

    @Mock
    private KnowledgeSourceFileMapper sourceFileMapper;

    @InjectMocks
    private KnowledgeImportTaskRecovery recovery;

    @Test
    void marksPreviouslyRunningTasksAndSourcesAsRetryableFailures() {
        KnowledgeImportTask task = new KnowledgeImportTask();
        task.setId(5L);
        task.setSourceFileId(9L);
        task.setStatus("SUMMARIZING");
        task.setProgress(38);
        task.setCurrentStage("正在总结第 1/13 段");

        KnowledgeSourceFile source = new KnowledgeSourceFile();
        source.setId(9L);
        source.setStatus("PARSED");

        when(taskMapper.selectList(any())).thenReturn(List.of(task));
        when(sourceFileMapper.selectById(9L)).thenReturn(source);

        recovery.recoverInterruptedTasks();

        assertEquals("FAILED", task.getStatus());
        assertEquals("导入已中断", task.getCurrentStage());
        assertEquals(KnowledgeImportTaskRecovery.INTERRUPTED_MESSAGE, task.getErrorMessage());
        assertEquals("FAILED", source.getStatus());
        assertEquals(KnowledgeImportTaskRecovery.INTERRUPTED_MESSAGE, source.getErrorMessage());
        verify(taskMapper).updateById(task);
        verify(sourceFileMapper).updateById(source);
    }

    @Test
    void doesNothingWhenThereAreNoInterruptedTasks() {
        when(taskMapper.selectList(any())).thenReturn(List.of());

        recovery.recoverInterruptedTasks();

        verify(taskMapper, never()).updateById(any(KnowledgeImportTask.class));
        verify(sourceFileMapper, never()).updateById(any(KnowledgeSourceFile.class));
    }
}
