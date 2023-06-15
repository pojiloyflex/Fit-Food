package com.dviss.calorietracker.repository

import com.dviss.tracker_domain.model.TrackableFood
import com.dviss.tracker_domain.model.TrackedFood
import com.dviss.tracker_domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.time.LocalDate
import kotlin.random.Random

class TrackerRepositoryFake : TrackerRepository {

    var shouldReturnError: Boolean = false

    private val trackedFoods = mutableListOf<TrackedFood>()
    var searchResults = listOf<TrackableFood>()

    private val getFoodsForDateFlow = MutableSharedFlow<List<TrackedFood>>(replay = 1)

    override suspend fun searchFood(
        query: String,
        page: Int,
        pageSize: Int
    ): Result<List<TrackableFood>> {
        return if (shouldReturnError) {
            Result.failure(Throwable())
        } else {
            Result.success(searchResults)
        }
    }

    override suspend fun insertTrackedFood(food: TrackedFood) {
        trackedFoods.add(food.copy(id = Random.nextInt()))
        getFoodsForDateFlow.emit(trackedFoods)
    }

    override suspend fun deleteTrackedFood(food: TrackedFood) {
        trackedFoods.remove(food)
        getFoodsForDateFlow.emit(trackedFoods)
    }

    override fun getFoodsForDate(localDate: LocalDate): Flow<List<TrackedFood>> {
        return getFoodsForDateFlow
    }
}