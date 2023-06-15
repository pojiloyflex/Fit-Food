package com.dviss.tracker_data.local

import androidx.room.*
import com.dviss.tracker_data.local.entity.TrackedFoodEntity
import kotlinx.coroutines.flow.Flow
import org.intellij.lang.annotations.Language

@Dao
interface TrackerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedFood(trackedFoodEntity: TrackedFoodEntity)

    @Delete
    suspend fun deleteTrackedFood(trackedFoodEntity: TrackedFoodEntity)

    @Query(
        """
            SELECT *
            FROM trackedFoodEntity
            WHERE dayOfMonth = :day AND month = :month AND year = :year
        """
    )
    fun getFoodsForDate(day: Int, month: Int, year: Int): Flow<List<TrackedFoodEntity>>
}