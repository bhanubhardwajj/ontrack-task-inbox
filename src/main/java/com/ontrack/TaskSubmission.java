package com.ontrack;

import java.time.LocalDateTime;

/**
 * Represents a task submission made by a student on the OnTrack platform.
 * 
 */
public class TaskSubmission {

    private final String submissionId;
    private final String studentId;
    private final String taskId;
    private final String taskName;
    private final LocalDateTime submittedAt;
    private SubmissionStatus status;
    private String feedback;

    public enum SubmissionStatus {
        PENDING,
        UNDER_REVIEW,
        NEED_RESUBMISSION,
        COMPLETE
    }

    public TaskSubmission(String submissionId, String studentId, String taskId, String taskName) {
        if (submissionId == null || submissionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Submission ID cannot be null or empty");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        if (taskName == null || taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be null or empty");
        }
        this.submissionId = submissionId;
        this.studentId = studentId;
        this.taskId = taskId;
        this.taskName = taskName;
        this.submittedAt = LocalDateTime.now();
        this.status = SubmissionStatus.PENDING;
        this.feedback = "";
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        if (feedback == null) {
            throw new IllegalArgumentException("Feedback cannot be null");
        }
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "TaskSubmission{" +
                "submissionId='" + submissionId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", status=" + status +
                '}';
    }
}
