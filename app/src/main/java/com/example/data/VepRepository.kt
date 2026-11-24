package com.example.data

import kotlinx.coroutines.flow.Flow

class VepRepository(private val vepDao: VepDao) {
    val allPipelineRuns: Flow<List<PipelineRunEntity>> = vepDao.getAllPipelineRuns()
    val allSelfHealingPatches: Flow<List<SelfHealingPatchEntity>> = vepDao.getAllSelfHealingPatches()

    suspend fun getPipelineRunById(id: Int): PipelineRunEntity? {
        return vepDao.getPipelineRunById(id)
    }

    suspend fun insertPipelineRun(run: PipelineRunEntity): Int {
        return vepDao.insertPipelineRun(run).toInt()
    }

    suspend fun updatePipelineRun(run: PipelineRunEntity) {
        vepDao.updatePipelineRun(run)
    }

    suspend fun deletePipelineRunById(id: Int) {
        vepDao.deletePipelineRunById(id)
    }

    fun getLayerStatusesForRun(runId: Int): Flow<List<LayerStatusEntity>> {
        return vepDao.getLayerStatusesForRun(runId)
    }

    suspend fun insertLayerStatus(status: LayerStatusEntity) {
        vepDao.insertLayerStatus(status)
    }

    suspend fun insertSelfHealingPatch(patch: SelfHealingPatchEntity) {
        vepDao.insertSelfHealingPatch(patch)
    }
}
