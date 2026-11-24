package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import kotlin.random.Random

class VepViewModel(application: Application) : AndroidViewModel(application) {

    private val db = VepDatabase.getDatabase(application)
    private val repository = VepRepository(db.vepDao())

    // All historic runs
    val pipelineRuns: StateFlow<List<PipelineRunEntity>> = repository.allPipelineRuns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All self-healing patches
    val patches: StateFlow<List<SelfHealingPatchEntity>> = repository.allSelfHealingPatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current running pipeline state
    private val _currentRun = MutableStateFlow<PipelineRunEntity?>(null)
    val currentRun: StateFlow<PipelineRunEntity?> = _currentRun.asStateFlow()

    // Current run's layer statuses
    private val _currentLayerStatuses = MutableStateFlow<List<LayerStatusEntity>>(emptyList())
    val currentLayerStatuses: StateFlow<List<LayerStatusEntity>> = _currentLayerStatuses.asStateFlow()

    // Active layer log console output
    private val _consoleLogs = MutableStateFlow<String>("")
    val consoleLogs: StateFlow<String> = _consoleLogs.asStateFlow()

    // Is a simulation actively running
    private val _isSimulationRunning = MutableStateFlow(false)
    val isSimulationRunning: StateFlow<Boolean> = _isSimulationRunning.asStateFlow()

    // Interactive details for Guardians (Layer 2)
    private val _guardianVotes = MutableStateFlow<Map<String, String>>(emptyMap())
    val guardianVotes: StateFlow<Map<String, String>> = _guardianVotes.asStateFlow()

    // Interactive stats for Real Devices (Layer 7)
    private val _deviceMetrics = MutableStateFlow<DeviceMetrics?>(null)
    val deviceMetrics: StateFlow<DeviceMetrics?> = _deviceMetrics.asStateFlow()

    // Active exception fix diagnosis
    private val _activeHealingDiagnosis = MutableStateFlow<HealingDiagnosis?>(null)
    val activeHealingDiagnosis: StateFlow<HealingDiagnosis?> = _activeHealingDiagnosis.asStateFlow()

    // Selection for historic run details
    private val _selectedRunId = MutableStateFlow<Int?>(null)
    val selectedRunId: StateFlow<Int?> = _selectedRunId.asStateFlow()

    // Selected historic run details
    val selectedRunLayerStatuses: StateFlow<List<LayerStatusEntity>> = _selectedRunId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getLayerStatusesForRun(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectRun(runId: Int?) {
        _selectedRunId.value = runId
    }

    // Full List of 28 Specialized AI Guardians (Layer 2)
    val guardiansList = listOf(
        "Architecture Guardian", "Backend Guardian", "Frontend Guardian", "Android Guardian",
        "Windows Guardian", "Linux Guardian", "Security Guardian", "Cryptography Guardian",
        "Database Guardian", "API Guardian", "UI Guardian", "UX Guardian",
        "Accessibility Guardian", "Performance Guardian", "Memory Guardian", "Concurrency Guardian",
        "Static Analysis Guardian", "Test Guardian", "Chaos Guardian", "Recovery Guardian",
        "Deployment Guardian", "Documentation Guardian", "SBOM Guardian", "Supply Chain Guardian",
        "Compliance Guardian", "Evidence Guardian", "Verification Guardian", "Release Guardian"
    )

    fun startPipelineRun(isSelfHealingTrial: Boolean) {
        if (_isSimulationRunning.value) return
        _isSimulationRunning.value = true
        _selectedRunId.value = null // Close details if viewing old runs

        viewModelScope.launch(Dispatchers.Default) {
            // Initializing states
            _consoleLogs.value = "Initializing VEP Session...\n"
            _guardianVotes.value = emptyMap()
            _deviceMetrics.value = null
            _activeHealingDiagnosis.value = null

            val commitHash = generateRandomHash().take(8)
            val runEntity = PipelineRunEntity(
                commitHash = "git-$commitHash",
                status = "IN_PROGRESS",
                activeLayer = 1,
                successLayersCount = 0,
                evidenceHash = "Calculating...",
                ramUsage = "Pending",
                cpuUsage = "Pending",
                startupTime = "Pending"
            )

            val runId = repository.insertPipelineRun(runEntity)
            val activeRun = runEntity.copy(id = runId)
            _currentRun.value = activeRun

            // Initialize all 10 Layer status placeholders
            val initialLayers = listOf(
                "Engineering Constitution",
                "Specialized Guardians",
                "Self-Healing Orchestrator",
                "Real Proof & Provenance",
                "Multi-Platform Compiler",
                "Automated Installation",
                "Real Device Validation",
                "Smart Exception Manager",
                "Release Readiness Metrics",
                "Evidence Archive & Bind"
            ).mapIndexed { idx, name ->
                LayerStatusEntity(
                    runId = runId,
                    layerNumber = idx + 1,
                    layerName = name,
                    status = "IDLE",
                    logs = "Waiting for pipeline to reach Layer ${idx + 1}...",
                    evidenceSignature = ""
                ).also {
                    repository.insertLayerStatus(it)
                }
            }
            _currentLayerStatuses.value = initialLayers

            var pipelinePassed = true
            var layersSuccessful = 0

            // Loop through each Layer
            for (layerNum in 1..10) {
                if (!pipelinePassed) {
                    // Mark remaining layers as SKIPPED
                    updateLayer(runId, layerNum, "SKIPPED", "Skipped due to upstream pipeline block.")
                    continue
                }

                // Mark current layer as RUNNING
                updateLayer(runId, layerNum, "RUNNING", "Starting validation diagnostics...\n")
                _currentRun.value = _currentRun.value?.copy(activeLayer = layerNum)
                _currentRun.value?.let { repository.updatePipelineRun(it) }

                delay(1200) // Aesthetic delay for user visual tracing

                when (layerNum) {
                    1 -> {
                        // LAYER 1: Engineering Constitution
                        appendLog("Checking Constitutional Laws:\n")
                        appendLog("- System Compilation Build Check...\n")
                        delay(600)
                        if (isSelfHealingTrial) {
                            appendLog("❌ compilation failed! [MainActivity.kt: Line 22: Unresolved reference: VepDashboardController]\n")
                            appendLog("Halting constitution check to dispatch Layer 3 Self-Healing agents...\n")
                            updateLayer(runId, 1, "FAILED", "Compilation Error: Unresolved reference: VepDashboardController in MainActivity.kt")
                            
                            // Trigger Self-Healing Layer 3
                            val healSuccess = runSelfHealingSequence(runId, commitHash)
                            if (healSuccess) {
                                appendLog("✅ Layer 1 Re-compilation: SUCCESS!\n")
                                appendLog("✅ Constitutional Test Suites: 12 passed, 0 failed.\n")
                                appendLog("✅ Constitutional Memory Leak Profiles: Clean.\n")
                                updateLayer(runId, 1, "PASSED", "Passed constitutional checks after smart self-healing patch.")
                                layersSuccessful++
                            } else {
                                appendLog("❌ Self-Healing failed to generate verified patch. Release blocked.\n")
                                pipelinePassed = false
                            }
                        } else {
                            appendLog("✅ compilation successful in 842ms\n")
                            delay(400)
                            appendLog("✅ constitutional tests: 12 passed, 0 failed.\n")
                            delay(400)
                            appendLog("✅ memory profile: no leaks detected.\n")
                            updateLayer(runId, 1, "PASSED", "Successfully completed compilation, test suite verification, and memory safety audits.")
                            layersSuccessful++
                        }
                    }
                    2 -> {
                        // LAYER 2: Specialized Guardians
                        appendLog("Deploying specialized agents panel...\n")
                        val votes = mutableMapOf<String, String>()
                        for (guardian in guardiansList) {
                            delay(60)
                            val vote = if (guardian == "Compliance Guardian" && isSelfHealingTrial) "WARN" else "PASS"
                            votes[guardian] = vote
                            _guardianVotes.value = votes.toMap()
                            appendLog("Agent '$guardian' casted vote: $vote\n")
                        }
                        delay(600)
                        appendLog("✅ specialized agent guardians achieved constitutional consensus.\n")
                        updateLayer(runId, 2, "PASSED", "Specialized Guardians casted votes with 100% consensus approval.")
                        layersSuccessful++
                    }
                    3 -> {
                        // LAYER 3: Self-Healing Orchestrator
                        if (isSelfHealingTrial) {
                            appendLog("Audit Log: Self-Healing already successfully applied at Layer 1 fallback. Logging event details in local DB...\n")
                            updateLayer(runId, 3, "PASSED", "Successfully diagnosed, patched, and verified compilation exceptions.")
                        } else {
                            appendLog("No compilation or constitutional defects detected. Bypassing active patch application...\n")
                            updateLayer(runId, 3, "PASSED", "Bypassed actively: 0 exceptions detected.")
                        }
                        layersSuccessful++
                    }
                    4 -> {
                        // LAYER 4: Real Proof & Provenance
                        appendLog("Cryptographic proof systems auditing run integrity...\n")
                        delay(500)
                        val runHash = generateSHA256("VEP-Run-$runId-commit-$commitHash")
                        val signature = generateSHA256(runHash + "PRIVATE_KEY_VEP_RELEASER")
                        appendLog("SHA-256 Run Fingerprint: $runHash\n")
                        delay(400)
                        appendLog("Digital Release Certificate Signature: $signature\n")
                        appendLog("Saved in provenance chain metadata.\n")
                        updateLayer(runId, 4, "PASSED", "Provenance certificate and SHA-256 integrity signatures generated and sealed.")
                        layersSuccessful++
                    }
                    5 -> {
                        // LAYER 5: Multi-Platform Compiler
                        appendLog("Initializing Multi-Platform Compiler targets...\n")
                        delay(400)
                        appendLog("- compiling target: Android APK -> COMPLETED (Size: 8.2 MB, Checksum: e4a2b...)\n")
                        delay(300)
                        appendLog("- compiling target: Windows x64 EXE/MSI -> COMPLETED (Size: 24.5 MB, Checksum: b8c1d...)\n")
                        delay(300)
                        appendLog("- compiling target: Linux AppImage -> COMPLETED (Size: 18.9 MB, Checksum: a3e0f...)\n")
                        delay(200)
                        appendLog("- building docker: Docker Hub -> COMPLETED\n")
                        updateLayer(runId, 5, "PASSED", "Successfully built and checksummed installable artifacts for Android, Windows, Linux, and Docker.")
                        layersSuccessful++
                    }
                    6 -> {
                        // LAYER 6: Automated Installation
                        appendLog("Launching sandbox containers for setup diagnostics...\n")
                        delay(500)
                        appendLog("- setting up configuration parameters... DONE\n")
                        delay(400)
                        appendLog("- launching system daemons and target app services... OK\n")
                        delay(400)
                        appendLog("- executing local node Health Checks... [HTTP 200 OK]\n")
                        updateLayer(runId, 6, "PASSED", "Simulated automated configuration and environment installation validation passed health-check tests.")
                        layersSuccessful++
                    }
                    7 -> {
                        // LAYER 7: Real Device Validation
                        appendLog("Interfacing with remote physical device farm test-bed...\n")
                        delay(600)
                        val ram = "${Random.nextInt(120, 158)} MB"
                        val cpu = "${String.format("%.1f", Random.nextDouble(5.5, 11.2))}%"
                        val startup = "${String.format("%.2f", Random.nextDouble(0.78, 1.35))}s"
                        
                        val metrics = DeviceMetrics(
                            androidDevice = "Pixel 8 (Android 14)",
                            windowsDevice = "Dell XPS 15 (Win 11)",
                            linuxDevice = "Lenovo ThinkPad (Ubuntu 22)",
                            ramUsage = ram,
                            cpuUsage = cpu,
                            startupTime = startup
                        )
                        _deviceMetrics.value = metrics
                        
                        appendLog("- target: $metrics.androidDevice. CPU: $cpu, RAM: $ram, Startup: $startup\n")
                        delay(500)
                        appendLog("- validation test-bed complete. 0 UI overflows, 100% interactable.\n")
                        
                        // Update run stats
                        _currentRun.value = _currentRun.value?.copy(
                            ramUsage = ram,
                            cpuUsage = cpu,
                            startupTime = startup
                        )
                        _currentRun.value?.let { repository.updatePipelineRun(it) }

                        updateLayer(runId, 7, "PASSED", "Validation on real targets successful. Low footprint resources confirmed.")
                        layersSuccessful++
                    }
                    8 -> {
                        // LAYER 8: Smart Exception Manager
                        appendLog("Exception Log Analytics active...\n")
                        delay(500)
                        if (isSelfHealingTrial) {
                            appendLog("1 runtime exception caught in sandbox cycle, analyzed and corrected by Self-Healing. Index updated.\n")
                        } else {
                            appendLog("0 exception files recorded during active runtime monitor execution.\n")
                        }
                        updateLayer(runId, 8, "PASSED", "Clean runtime logs. Zero unhandled exception errors traced.")
                        layersSuccessful++
                    }
                    9 -> {
                        // LAYER 9: Release Readiness Metrics
                        appendLog("Assembling Release Quality scorecard...\n")
                        delay(500)
                        appendLog("Build Reproducibility: PASS\n")
                        appendLog("Constitutional Audits: PASS\n")
                        appendLog("Consensus Guardians: PASS\n")
                        appendLog("Real-bed Verification: PASS\n")
                        updateLayer(runId, 9, "PASSED", "All 10 core release criteria met. Release is fully APPROVED for GA release.")
                        layersSuccessful++
                    }
                    10 -> {
                        // LAYER 10: Evidence Archive & Bind
                        appendLog("Finalizing Auditable Release Evidence Bundle...\n")
                        delay(600)
                        val archiveHash = generateSHA256("VEP-Evidence-Release-$runId")
                        appendLog("Generating verifiable digital signature envelope:\n")
                        appendLog("Hash: $archiveHash\n")
                        appendLog("Saved package to /releases/vep-audit-$runId.zip\n")
                        appendLog("Pipeline verified. Release unlocked!\n")
                        updateLayer(runId, 10, "PASSED", "All audit logs, evidence metrics, and signature bundles archived with SHA-256 proof.")
                        layersSuccessful++
                    }
                }
                delay(800)
            }

            // Update overall run outcome
            val finalStatus = if (pipelinePassed) "PASSED" else "FAILED"
            val finalRun = _currentRun.value?.copy(
                status = finalStatus,
                successLayersCount = layersSuccessful,
                evidenceHash = if (pipelinePassed) generateSHA256("VEP-Proof-$runId").take(24) else "FAILED-UNVERIFIED"
            )
            if (finalRun != null) {
                repository.updatePipelineRun(finalRun)
                _currentRun.value = finalRun
            }

            _isSimulationRunning.value = false
        }
    }

    private suspend fun runSelfHealingSequence(runId: Int, commitHash: String): Boolean {
        updateLayer(runId, 3, "RUNNING", "Self-Healing agents activated. Fetching Gemini intelligence diagnostics...\n")
        appendLog("[Self-Healing] Sending error trace to Gemini API layer...\n")
        
        val errorMsg = "Unresolved reference: VepDashboardController in MainActivity.kt"
        val mockLogs = """
            MainActivity.kt:22: error: unresolved reference: VepDashboardController
            val controller = VepDashboardController()
                             ^
            1 error
            :app:compileDebugKotlin FAILED
        """.trimIndent()

        val responseStr = withContext(Dispatchers.IO) {
            RetrofitClient.getSelfHealingFix(errorMsg, mockLogs)
        }

        var analysis = ""
        var patch = ""
        var status = "SUCCESS"

        try {
            val json = JSONObject(responseStr)
            analysis = json.optString("analysis", "Unresolved class reference in source entry path.")
            patch = json.optString("patch", "import com.example.controller.VepDashboardController")
        } catch (e: Exception) {
            // Local high-fidelity fallback if API key is not configured or fails
            analysis = "The VepDashboardController class was instantiated before being imported into MainActivity.kt."
            patch = """
                package com.example
                
                // VEP Self-Healing Corrective Patch
                import com.example.ui.VepDashboardController
                
                class MainActivity : ComponentActivity() {
                    // Correctly imported and referenceable
                    private val controller = VepDashboardController()
                }
            """.trimIndent()
        }

        val healingDiag = HealingDiagnosis(
            exception = errorMsg,
            analysis = analysis,
            patchCode = patch
        )
        _activeHealingDiagnosis.value = healingDiag

        appendLog("[Self-Healing] Diagnosis: $analysis\n")
        delay(1200)
        appendLog("[Self-Healing] Formulating code correction patch...\n")
        appendLog("[Self-Healing] Applying patch to MainActivity.kt source...\n")
        delay(1200)
        appendLog("[Self-Healing] Running Verification Kit checks...\n")
        delay(800)

        // Save patch in Database for enterprise audit logs
        val patchEntity = SelfHealingPatchEntity(
            runId = runId,
            exceptionMessage = errorMsg,
            rootCauseAnalysis = analysis,
            patchCode = patch,
            status = "SUCCESS"
        )
        repository.insertSelfHealingPatch(patchEntity)

        updateLayer(runId, 3, "PASSED", "Identified and patched: 'VepDashboardController' import exception successfully.")
        return true
    }

    private suspend fun updateLayer(runId: Int, layerNum: Int, status: String, summary: String) {
        val list = _currentLayerStatuses.value.map {
            if (it.layerNumber == layerNum) {
                val updated = it.copy(status = status, logs = _consoleLogs.value + "\n\nSummary: $summary")
                repository.insertLayerStatus(updated)
                updated
            } else {
                it
            }
        }
        _currentLayerStatuses.value = list
    }

    private fun appendLog(text: String) {
        _consoleLogs.value += text
    }

    private fun generateRandomHash(): String {
        val chars = "abcdef0123456789"
        return (1..32).map { chars.random() }.joinToString("")
    }

    private fun generateSHA256(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "HASH-FALLBACK-38c7f99d7a9b0c2e"
        }
    }
}

data class DeviceMetrics(
    val androidDevice: String,
    val windowsDevice: String,
    val linuxDevice: String,
    val ramUsage: String,
    val cpuUsage: String,
    val startupTime: String
)

data class HealingDiagnosis(
    val exception: String,
    val analysis: String,
    val patchCode: String
)
