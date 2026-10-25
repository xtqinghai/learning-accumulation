package com.quinn.hboot.productivity.model.vo;

import com.quinn.hboot.knowledge.model.entity.KnowledgeArticle;
import com.quinn.hboot.productivity.model.entity.PersonalTask;
import com.quinn.hboot.schedule.model.entity.ScheduledJob;

import java.util.List;

public record ProductivityOverview(
    long knowledgeCount,
    long snippetCount,
    long documentCount,
    long openTaskCount,
    List<KnowledgeArticle> recentArticles,
    List<PersonalTask> upcomingTasks,
    List<ScheduledJob> scheduledJobs
) {}
