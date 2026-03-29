package com.expensetracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.expensetracker.domain.usecase.ProcessDueRecurringExpensesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringExpenseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val processRecurring: ProcessDueRecurringExpensesUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val processed = processRecurring()
            Result.success(
                workDataOf("processed_count" to processed)
            )
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "recurring_expense_worker"

        fun enqueue(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val request = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun runOnce(workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<RecurringExpenseWorker>().build()
            workManager.enqueue(request)
        }
    }
}