package com.ontrack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD test suite for the TaskInboxService class.
 *
 * Tests are written in alignment with the TDD red-green-refactor cycle.
 * Each test defines expected behaviour before (or alongside) the implementation.
 */
@DisplayName("TaskInboxService Tests")
class TaskInboxServiceTest {

    private TaskInboxService service;

    @BeforeEach
    void setUp() {
        service = new TaskInboxService();
    }

    // ─────────────────────────────────────────────────────────────
    // submitTask() – happy path
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should successfully submit a task and return a TaskSubmission object")
    void testSubmitTask_Success() {
        TaskSubmission submission = service.submitTask("s123", "T1", "Pass Task 1");

        assertNotNull(submission, "Submission should not be null");
        assertEquals("s123", submission.getStudentId());
        assertEquals("T1", submission.getTaskId());
        assertEquals("Pass Task 1", submission.getTaskName());
        assertNotNull(submission.getSubmissionId());
        assertEquals(TaskSubmission.SubmissionStatus.PENDING, submission.getStatus());
    }

    @Test
    @DisplayName("Should assign a unique submission ID to each new submission")
    void testSubmitTask_UniqueSubmissionIds() {
        TaskSubmission sub1 = service.submitTask("s001", "T1", "Pass Task 1");
        TaskSubmission sub2 = service.submitTask("s002", "T1", "Pass Task 1");

        assertNotEquals(sub1.getSubmissionId(), sub2.getSubmissionId(),
                "Each submission should have a unique ID");
    }

    @Test
    @DisplayName("Should set initial submission status as PENDING")
    void testSubmitTask_InitialStatusIsPending() {
        TaskSubmission submission = service.submitTask("s123", "T2", "Credit Task 2");

        assertEquals(TaskSubmission.SubmissionStatus.PENDING, submission.getStatus());
    }

    @Test
    @DisplayName("Should record submission timestamp")
    void testSubmitTask_TimestampIsRecorded() {
        TaskSubmission submission = service.submitTask("s123", "T1", "Pass Task 1");

        assertNotNull(submission.getSubmittedAt(), "Submission timestamp should not be null");
    }

    @Test
    @DisplayName("Total submission count should increase after each submission")
    void testSubmitTask_IncreasesSubmissionCount() {
        service.submitTask("s001", "T1", "Pass Task 1");
        service.submitTask("s002", "T2", "Credit Task 2");

        assertEquals(2, service.getTotalSubmissionCount());
    }

    // ─────────────────────────────────────────────────────────────
    // submitTask() – validation failures
    // ─────────────────────────────────────────────────────────────

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Should throw IllegalArgumentException when studentId is null, empty, or blank")
    void testSubmitTask_InvalidStudentId(String studentId) {
        assertThrows(IllegalArgumentException.class,
                () -> service.submitTask(studentId, "T1", "Pass Task 1"),
                "Should reject invalid student ID");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Should throw IllegalArgumentException when taskId is null, empty, or blank")
    void testSubmitTask_InvalidTaskId(String taskId) {
        assertThrows(IllegalArgumentException.class,
                () -> service.submitTask("s123", taskId, "Pass Task 1"),
                "Should reject invalid task ID");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Should throw IllegalArgumentException when taskName is null, empty, or blank")
    void testSubmitTask_InvalidTaskName(String taskName) {
        assertThrows(IllegalArgumentException.class,
                () -> service.submitTask("s123", "T1", taskName),
                "Should reject invalid task name");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when student submits same task while still PENDING")
    void testSubmitTask_DuplicateActivePendingSubmission() {
        service.submitTask("s123", "T1", "Pass Task 1");

        assertThrows(IllegalStateException.class,
                () -> service.submitTask("s123", "T1", "Pass Task 1"),
                "Should not allow duplicate active submissions");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when student submits same task while UNDER_REVIEW")
    void testSubmitTask_DuplicateActiveUnderReviewSubmission() {
        TaskSubmission sub = service.submitTask("s123", "T1", "Pass Task 1");
        service.updateStatus(sub.getSubmissionId(), TaskSubmission.SubmissionStatus.UNDER_REVIEW);

        assertThrows(IllegalStateException.class,
                () -> service.submitTask("s123", "T1", "Pass Task 1"),
                "Should not allow submission while another is UNDER_REVIEW");
    }

    @Test
    @DisplayName("Should allow resubmission when previous submission is NEED_RESUBMISSION")
    void testSubmitTask_AllowsResubmissionAfterNeedResubmission() {
        TaskSubmission sub = service.submitTask("s123", "T1", "Pass Task 1");
        service.updateStatus(sub.getSubmissionId(), TaskSubmission.SubmissionStatus.NEED_RESUBMISSION);

        assertDoesNotThrow(() -> service.submitTask("s123", "T1", "Pass Task 1"),
                "Should allow resubmission when previous attempt needs revision");
    }

    // ─────────────────────────────────────────────────────────────
    // getInbox()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return all submissions for a given student")
    void testGetInbox_ReturnsStudentSubmissions() {
        service.submitTask("s123", "T1", "Pass Task 1");
        service.submitTask("s123", "T2", "Credit Task 2");
        service.submitTask("s999", "T1", "Pass Task 1"); // different student

        List<TaskSubmission> inbox = service.getInbox("s123");

        assertEquals(2, inbox.size(), "Inbox should contain exactly 2 submissions for s123");
    }

    @Test
    @DisplayName("Should return empty list when student has no submissions")
    void testGetInbox_EmptyForNewStudent() {
        List<TaskSubmission> inbox = service.getInbox("s999");

        assertNotNull(inbox);
        assertTrue(inbox.isEmpty(), "New student should have an empty inbox");
    }

    @Test
    @DisplayName("Should not include other students submissions in inbox")
    void testGetInbox_IsolatedByStudent() {
        service.submitTask("sA", "T1", "Pass Task 1");
        service.submitTask("sB", "T1", "Pass Task 1");

        List<TaskSubmission> inboxA = service.getInbox("sA");

        assertEquals(1, inboxA.size());
        assertEquals("sA", inboxA.get(0).getStudentId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw IllegalArgumentException for invalid studentId in getInbox")
    void testGetInbox_InvalidStudentId(String studentId) {
        assertThrows(IllegalArgumentException.class,
                () -> service.getInbox(studentId));
    }

    // ─────────────────────────────────────────────────────────────
    // getSubmissionById()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return submission when given a valid submission ID")
    void testGetSubmissionById_Found() {
        TaskSubmission submitted = service.submitTask("s123", "T1", "Pass Task 1");

        Optional<TaskSubmission> result = service.getSubmissionById(submitted.getSubmissionId());

        assertTrue(result.isPresent(), "Should find the submission");
        assertEquals(submitted.getSubmissionId(), result.get().getSubmissionId());
    }

    @Test
    @DisplayName("Should return empty Optional for a non-existent submission ID")
    void testGetSubmissionById_NotFound() {
        Optional<TaskSubmission> result = service.getSubmissionById("SUB-INVALID");

        assertFalse(result.isPresent(), "Should return empty Optional for unknown ID");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw IllegalArgumentException for null or empty submission ID")
    void testGetSubmissionById_InvalidId(String id) {
        assertThrows(IllegalArgumentException.class, () -> service.getSubmissionById(id));
    }

    // ─────────────────────────────────────────────────────────────
    // updateStatus()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should update submission status successfully")
    void testUpdateStatus_Success() {
        TaskSubmission sub = service.submitTask("s123", "T1", "Pass Task 1");

        service.updateStatus(sub.getSubmissionId(), TaskSubmission.SubmissionStatus.UNDER_REVIEW);

        assertEquals(TaskSubmission.SubmissionStatus.UNDER_REVIEW,
                service.getSubmissionById(sub.getSubmissionId()).get().getStatus());
    }

    @Test
    @DisplayName("Should mark submission as COMPLETE")
    void testUpdateStatus_MarkComplete() {
        TaskSubmission sub = service.submitTask("s123", "T1", "Pass Task 1");

        service.updateStatus(sub.getSubmissionId(), TaskSubmission.SubmissionStatus.COMPLETE);

        assertEquals(TaskSubmission.SubmissionStatus.COMPLETE,
                service.getSubmissionById(sub.getSubmissionId()).get().getStatus());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating status for a non-existent submission")
    void testUpdateStatus_SubmissionNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus("SUB-GHOST", TaskSubmission.SubmissionStatus.COMPLETE));
    }

    // ─────────────────────────────────────────────────────────────
    // addFeedback()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should add tutor feedback to a submission")
    void testAddFeedback_Success() {
        TaskSubmission sub = service.submitTask("s123", "T1", "Pass Task 1");

        service.addFeedback(sub.getSubmissionId(), "Good effort, please clarify section 2.");

        assertEquals("Good effort, please clarify section 2.",
                service.getSubmissionById(sub.getSubmissionId()).get().getFeedback());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when adding feedback to non-existent submission")
    void testAddFeedback_SubmissionNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addFeedback("SUB-GHOST", "Great work!"));
    }

    @Test
    @DisplayName("Initial feedback for a new submission should be empty string")
    void testSubmission_InitialFeedbackIsEmpty() {
        TaskSubmission sub = service.submitTask("s123", "T1", "Pass Task 1");

        assertEquals("", sub.getFeedback(), "New submission should have no feedback initially");
    }

    // ─────────────────────────────────────────────────────────────
    // getSubmissionsByStatus()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should filter submissions by status for a given student")
    void testGetSubmissionsByStatus_FiltersByStatus() {
        TaskSubmission sub1 = service.submitTask("s123", "T1", "Pass Task 1");
        TaskSubmission sub2 = service.submitTask("s123", "T2", "Credit Task 2");
        service.updateStatus(sub1.getSubmissionId(), TaskSubmission.SubmissionStatus.COMPLETE);

        List<TaskSubmission> pending = service.getSubmissionsByStatus(
                "s123", TaskSubmission.SubmissionStatus.PENDING);
        List<TaskSubmission> complete = service.getSubmissionsByStatus(
                "s123", TaskSubmission.SubmissionStatus.COMPLETE);

        assertEquals(1, pending.size(), "Should have 1 PENDING submission");
        assertEquals(1, complete.size(), "Should have 1 COMPLETE submission");
    }

    @Test
    @DisplayName("Should return empty list when no submissions match the given status")
    void testGetSubmissionsByStatus_EmptyWhenNoMatch() {
        service.submitTask("s123", "T1", "Pass Task 1");

        List<TaskSubmission> complete = service.getSubmissionsByStatus(
                "s123", TaskSubmission.SubmissionStatus.COMPLETE);

        assertTrue(complete.isEmpty(), "Should return empty list when no matching submissions");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when status is null")
    void testGetSubmissionsByStatus_NullStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getSubmissionsByStatus("s123", null));
    }
}
