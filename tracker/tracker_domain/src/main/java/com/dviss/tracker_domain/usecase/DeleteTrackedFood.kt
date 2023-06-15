package com.dviss.tracker_domain.usecase

import com.dviss.tracker_domain.model.TrackedFood
import com.dviss.tracker_domain.repository.TrackerRepository

class DeleteTrackedFood(
    private val repository: TrackerRepository
) {

    suspend operator fun invoke(
        trackedFood: TrackedFood
    ) {
        repository.deleteTrackedFood(trackedFood)
    }
}