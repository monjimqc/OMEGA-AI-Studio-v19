package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VepDao {
    @Query("SELECT * FROM pipeline_runs ORDER BY timestamp DESC")
    fun getAllPipelineRuns(): Flow<List<PipelineRunEntity>>

    @Query("SELECT * FROM pipeline_runs WHERE id = :id")
    suspend fun getPipelineRunById(id: Int): PipelineRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPipelineRun(run: PipelineRunEntity): Long

    @Update
    suspend fun updatePipelineRun(run: PipelineRunEntity)

    @Query("DELETE FROM pipeline_runs WHERE id = :id")
    suspend fun deletePipelineRunById(id: Int)

    @Query("SELECT * FROM layer_statuses WHERE runId = :runId ORDER BY layerNumber ASC")
    fun getLayerStatusesForRun(runId: Int): Flow<List<LayerStatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayerStatus(status: LayerStatusEntity)

    @Query("SELECT * FROM self_healing_patches ORDER BY patchTimestamp DESC")
    fun getAllSelfHealingPatches(): Flow<List<SelfHealingPatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelfHealingPatch(patch: SelfHealingPatchEntity)
}
