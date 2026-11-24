package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pipeline_runs")
data class PipelineRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val commitHash: String,
    val status: String, // "PASSED", "FAILED", "IN_PROGRESS"
    val activeLayer: Int,
    val successLayersCount: Int,
    val evidenceHash: String,
    val ramUsage: String,
    val cpuUsage: String,
    val startupTime: String
)

@Entity(tableName = "layer_statuses")
data class LayerStatusEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val runId: Int,
    val layerNumber: Int,
    val layerName: String,
    val status: String, // "PASSED", "FAILED", "SKIPPED", "RUNNING", "IDLE"
    val logs: String,
    val evidenceSignature: String
)

@Entity(tableName = "self_healing_patches")
data class SelfHealingPatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val runId: Int,
    val exceptionMessage: String,
    val rootCauseAnalysis: String,
    val patchCode: String,
    val patchTimestamp: Long = System.currentTimeMillis(),
    val status: String // "SUCCESS", "FAILED_ROLLBACK"
)
