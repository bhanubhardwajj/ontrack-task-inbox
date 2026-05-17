package com.ontrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class that manages task submissions for students on OnTrack.
 * Provides functionality to submit tasks, retrieve a student's inbox,
 * and update submission status and feedback.
 * FIXED FILE
 */
public class TaskInboxService {

    private final List<TaskSubmission> submissions;

    public TaskInboxService() {
        this.submissions = new ArrayList<>();
    }

    /**
     * Submits a task for a student.
     *
     * @param studentId the ID of the student submitting the task
     * @param taskId    the ID of the task being submitted
     * @param taskName  the name/title of the task
     * @return the created TaskSubmission object
     * @throws IllegalArgumentException if any parameter is null or empty
     * @throws IllegalStateException    if the student has already submitted this task and it is PENDING or UNDER_REVIEW
     */
    public TaskSubmission submitTask(String studentId, String taskId, String taskName) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        if (taskName == null || taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be null or empty");
        }

        // Prevent duplicate active submissions
        boolean hasActiveSubmission = submissions.stream()
                .anyMatch(s -> s.getStudentId().equals(studentId)
                        && s.getTaskId().equals(taskId)
                        && (s.getStatus() == TaskSubmission.SubmissionStatus.PENDING
                                || s.getStatus() == TaskSubmission.SubmissionStatus.UNDER_REVIEW));
        if (hasActiveSubmission) {
            throw new IllegalStateException(
                    "Student " + studentId + " already has an active submission for task " + taskId);
        }

        String submissionId = "SUB-" + (submissions.size() + 1);
        TaskSubmission submission = new TaskSubmission(submissionId, studentId, taskId, taskName);
        submissions.add(submission);
        return submission;
    }

    /**
     * Retrieves all task submissions for a given student (their inbox).
     *
     * @param studentId the ID of the student
     * @return an unmodifiable list of the student's submissions, or an empty list if none
     */
    public List<TaskSubmission> getInbox(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }
        return submissions.stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific submission by its ID.
     *
     * @param submissionId the ID of the submission
     * @return an Optional containing the submission if found, otherwise empty
     */
    public Optional<TaskSubmission> getSubmissionById(String submissionId) {
        if (submissionId == null || submissionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Submission ID cannot be null or empty");
        }
        return submissions.stream()
                .filter(s -> s.getSubmissionId().equals(submissionId))
                .findFirst();
    }

    /**
     * Updates the status of a submission.
     *
     * @param submissionId the ID of the submission to update
     * @param newStatus    the new status to set
     * @throws IllegalArgumentException if the submission is not found
     */
    public void updateStatus(String submissionId, TaskSubmission.SubmissionStatus newStatus) {
        TaskSubmission submission = getSubmissionById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Submission not found: " + submissionId));
        submission.setStatus(newStatus);
    }

    /**
     * Adds tutor feedback to a submission.
     *
     * @param submissionId the ID of the submission
     * @param feedback     the feedback text from the tutor
     * @throws IllegalArgumentException if the submission is not found
     */
    public void addFeedback(String submissionId, String feedback) {
        TaskSubmission submission = getSubmissionById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Submission not found: " + submissionId));
        submission.setFeedback(feedback);
    }

    /**
     * Returns the total number of submissions in the system.
     */
    public int getTotalSubmissionCount() {
        return submissions.size();
    }

    /**
     * Returns all submissions with a given status for a student.
     *
     * @param studentId the student's ID
     * @param status    the status to filter by
     * @return list of submissions matching the status
     */
    public List<TaskSubmission> getSubmissionsByStatus(String studentId,
            TaskSubmission.SubmissionStatus status) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return submissions.stream()
                .filter(s -> s.getStudentId().equals(studentId) && s.getStatus() == status)
                .collect(Collectors.toList());
    }
}
