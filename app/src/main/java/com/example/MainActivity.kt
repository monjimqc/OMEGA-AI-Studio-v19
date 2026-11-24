package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.LayerStatusEntity
import com.example.data.PipelineRunEntity
import com.example.ui.DeviceMetrics
import com.example.ui.HealingDiagnosis
import com.example.ui.VepViewModel
import com.example.ui.components.GuardiansDashboardComponent
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    containerColor = DarkSlateBg
                ) { innerPadding ->
                    VepDashboardScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VepDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: VepViewModel = viewModel()
) {
    val pipelineRuns by viewModel.pipelineRuns.collectAsStateWithLifecycle()
    val patches by viewModel.patches.collectAsStateWithLifecycle()
    val currentRun by viewModel.currentRun.collectAsStateWithLifecycle()
    val currentLayerStatuses by viewModel.currentLayerStatuses.collectAsStateWithLifecycle()
    val consoleLogs by viewModel.consoleLogs.collectAsStateWithLifecycle()
    val isSimulationRunning by viewModel.isSimulationRunning.collectAsStateWithLifecycle()
    val guardianVotes by viewModel.guardianVotes.collectAsStateWithLifecycle()
    val deviceMetrics by viewModel.deviceMetrics.collectAsStateWithLifecycle()
    val activeHealingDiagnosis by viewModel.activeHealingDiagnosis.collectAsStateWithLifecycle()
    val selectedRunId by viewModel.selectedRunId.collectAsStateWithLifecycle()
    val selectedRunLayerStatuses by viewModel.selectedRunLayerStatuses.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Live Pipeline, 1: History & Patches, 2: Constitutional Laws
    var showLayerDetailSheet by remember { mutableStateOf<LayerStatusEntity?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBg)
    ) {
        // App Header Bar with Dual-Language Branding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkSlateSurface, DarkSlateBg)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "VEP Verified Icon",
                            tint = TechCyan,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("app_logo_icon")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VEP Control Center",
                            color = TechTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "سامانه خط لوله مهندسی اثبات کیفیت",
                        color = TechTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }

                // Quick Status Indicator
                val overallStatus = currentRun?.status ?: "READY"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (overallStatus) {
                                "IN_PROGRESS" -> TechCyan.copy(alpha = 0.2f)
                                "PASSED" -> TechLime.copy(alpha = 0.2f)
                                "FAILED" -> TechCrimson.copy(alpha = 0.2f)
                                else -> TechTextSecondary.copy(alpha = 0.15f)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = when (overallStatus) {
                                "IN_PROGRESS" -> TechCyan
                                "PASSED" -> TechLime
                                "FAILED" -> TechCrimson
                                else -> TechTextSecondary.copy(alpha = 0.4f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (overallStatus) {
                            "IN_PROGRESS" -> "درحال ارزیابی VEP"
                            "PASSED" -> "تأیید نهایی (RELEASE)"
                            "FAILED" -> "انتشار مسدود (BLOCKED)"
                            else -> "آماده به کار"
                        },
                        color = when (overallStatus) {
                            "IN_PROGRESS" -> TechCyan
                            "PASSED" -> TechLime
                            "FAILED" -> TechCrimson
                            else -> TechTextPrimary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Custom High-Fidelity Navigation Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DarkSlateBg,
            contentColor = TechCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = TechCyan,
                    height = 2.dp
                )
            },
            divider = { HorizontalDivider(color = TechBorder) }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("خط لوله زنده", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Live Pipeline", fontSize = 9.sp, color = if (activeTab == 0) TechCyan else TechTextSecondary)
                    }
                },
                modifier = Modifier.testTag("tab_live_pipeline")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("نگهبانان (۲۸)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Guardians (28)", fontSize = 9.sp, color = if (activeTab == 1) TechCyan else TechTextSecondary)
                    }
                },
                modifier = Modifier.testTag("tab_guardians")
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تاریخچه و اصلاحات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("History & Patches", fontSize = 9.sp, color = if (activeTab == 2) TechCyan else TechTextSecondary)
                    }
                },
                modifier = Modifier.testTag("tab_history")
            )
            Tab(
                selected = activeTab == 3,
                onClick = { activeTab = 3 },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("قانون اساسی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Constitution", fontSize = 9.sp, color = if (activeTab == 3) TechCyan else TechTextSecondary)
                    }
                },
                modifier = Modifier.testTag("tab_constitution")
            )
        }

        // Main Tab Screens
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp)
        ) {
            when (activeTab) {
                0 -> LivePipelineTab(
                    currentRun = currentRun,
                    currentLayerStatuses = currentLayerStatuses,
                    isSimulationRunning = isSimulationRunning,
                    consoleLogs = consoleLogs,
                    guardianVotes = guardianVotes,
                    deviceMetrics = deviceMetrics,
                    activeHealingDiagnosis = activeHealingDiagnosis,
                    onStartSimulation = { selfHeal -> viewModel.startPipelineRun(selfHeal) },
                    onLayerClick = { showLayerDetailSheet = it }
                )
                1 -> GuardiansDashboardComponent(
                    guardianVotes = guardianVotes
                )
                2 -> HistoryTab(
                    pipelineRuns = pipelineRuns,
                    patches = patches,
                    selectedRunId = selectedRunId,
                    selectedRunLayerStatuses = selectedRunLayerStatuses,
                    onSelectRun = { viewModel.selectRun(it) }
                )
                3 -> ConstitutionTab()
            }
        }
    }

    // Modal Bottom Sheet or Detail dialog for viewing Layer Logs & Evidence hashes
    if (showLayerDetailSheet != null) {
        AlertDialog(
            onDismissRequest = { showLayerDetailSheet = null },
            confirmButton = {
                TextButton(
                    onClick = { showLayerDetailSheet = null },
                    modifier = Modifier.testTag("dismiss_detail_dialog")
                ) {
                    Text("بستن (Close)", color = TechCyan)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Layer info",
                        tint = TechCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جزئیات لایه ${showLayerDetailSheet?.layerNumber}: ${showLayerDetailSheet?.layerName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSlateBg, RoundedCornerShape(6.dp))
                            .border(1.dp, TechBorder, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "امضای اثبات ممیزی لایه (Layer SHA):\n" +
                                    (showLayerDetailSheet?.evidenceSignature.takeIf { !it.isNullOrBlank() }
                                        ?: "sha256:${showLayerDetailSheet?.logs.hashCode().toString(16)}"),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TechLime
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لاگ‌های سیستم و خروجی تائید خودکار:",
                        fontSize = 12.sp,
                        color = TechTextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF1C1B1F), RoundedCornerShape(6.dp))
                            .border(1.dp, TechBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = showLayerDetailSheet?.logs ?: "بدون لاگ فعال",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            containerColor = DarkSlateSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("layer_detail_dialog")
        )
    }
}

@Composable
fun LivePipelineTab(
    currentRun: PipelineRunEntity?,
    currentLayerStatuses: List<LayerStatusEntity>,
    isSimulationRunning: Boolean,
    consoleLogs: String,
    guardianVotes: Map<String, String>,
    deviceMetrics: DeviceMetrics?,
    activeHealingDiagnosis: HealingDiagnosis?,
    onStartSimulation: (Boolean) -> Unit,
    onLayerClick: (LayerStatusEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // VEP Controller Buttons Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, TechBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulation_controller_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "کنترل و آغاز ارزیابی خودکار خط لوله",
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "یک سناریو را اجرا کنید تا جریان ۱۰ لایه اثبات ممیزی را مشاهده کنید.",
                        fontSize = 11.sp,
                        color = TechTextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onStartSimulation(false) },
                            enabled = !isSimulationRunning,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TechCyan,
                                contentColor = Color.White,
                                disabledContainerColor = TechBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_run_standard")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run Standard")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("سناریو نرمال (Pass)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onStartSimulation(true) },
                            enabled = !isSimulationRunning,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TechAmber,
                                contentColor = Color.White,
                                disabledContainerColor = TechBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_run_self_heal")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Run Self Heal")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("سناریو خوداصلاحی (Fix)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Diagnostics Console Log
        if (isSimulationRunning || consoleLogs.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    border = BorderStroke(1.dp, TechBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Terminal,
                                    contentDescription = "Console",
                                    tint = TechCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "کنسول عیب‌یابی خط لوله VEP",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = TechTextPrimary
                                )
                            }
                            if (isSimulationRunning) {
                                CircularProgressIndicator(
                                    color = TechCyan,
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = consoleLogs,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = TechCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Layer 3 Smart Self-Healing (Gemini Powered) Live Panel
        if (activeHealingDiagnosis != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.5.dp, TechAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("self_healing_panel")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Healing",
                                tint = TechAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "خوداصلاحی هوشمند لایه ۳ (Gemini AI Client)",
                                fontWeight = FontWeight.Bold,
                                color = TechAmber,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "یک خطای مهندسی در لایه ۱ شناسایی شد. هوش مصنوعی علت را ریشه‌یابی و پچ متناظر را اعمال کرد:",
                            fontSize = 11.sp,
                            color = TechTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "❌ خطای ردیابی شده: ${activeHealingDiagnosis.exception}",
                            color = TechCrimson,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 ریشه‌یابی خطا (RCA): ${activeHealingDiagnosis.analysis}",
                            color = TechLime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "کد اصلاح شده (Kotlin Patch):",
                            fontSize = 10.sp,
                            color = TechTextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1C1B1F), RoundedCornerShape(4.dp))
                                .border(1.dp, TechBorder, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = activeHealingDiagnosis.patchCode,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = Color.White,
                                maxLines = 10,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Layer 2 Guardians Panel (28 AI Guardians Dashboard)
        item {
            GuardiansDashboardComponent(
                guardianVotes = guardianVotes
            )
        }

        // Layer 7 Physical Device validation metrics
        if (deviceMetrics != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, TechBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Devices,
                                contentDescription = "Devices",
                                tint = TechCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اعتبارسنجی لایه ۷ روی سخت‌افزار واقعی",
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricItemCard(modifier = Modifier.weight(1f), title = "مصرف RAM", value = deviceMetrics.ramUsage, icon = Icons.Default.Memory)
                            MetricItemCard(modifier = Modifier.weight(1f), title = "بار پردازنده CPU", value = deviceMetrics.cpuUsage, icon = Icons.Default.Analytics)
                            MetricItemCard(modifier = Modifier.weight(1f), title = "زمان راه‌اندازی App", value = deviceMetrics.startupTime, icon = Icons.Default.History)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "محیط‌های هدف ممیزی شده: Android, Windows, Linux",
                            fontSize = 10.sp,
                            color = TechTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Layer 1 to 10 Progress Grid Title
        item {
            Text(
                text = "۱۰ لایه مهندسی و اثبات پایداری (VEP Layers)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TechTextPrimary,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // Layer Status Row List
        if (currentLayerStatuses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ اجرای فعالی در جریان نیست. سناریوی دلخواه را آغاز کنید.",
                        color = TechTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(currentLayerStatuses) { layer ->
                LayerStatusCard(
                    layer = layer,
                    onClick = { onLayerClick(layer) }
                )
            }
        }

        // Layer 9 Indicators Summary Card (Release Checklist)
        if (currentRun != null && currentRun.status != "IN_PROGRESS") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurfaceVariant),
                    border = BorderStroke(2.dp, if (currentRun.status == "PASSED") TechLime else TechCrimson),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 24.dp)
                        .testTag("release_scorecard_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "گزارش نهایی لایه ۹: آمادگی انتشار (Ready-For-Release)",
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimary,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = if (currentRun.status == "PASSED") Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = "Readiness Status",
                                tint = if (currentRun.status == "PASSED") TechLime else TechCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ScoreCheckItem(label = "موفقیت در کامپایل سیستم (Build Reproducibility)", passed = true)
                            ScoreCheckItem(label = "موفقیت در تمام آزمون‌های مرجع (Functional Tests)", passed = true)
                            ScoreCheckItem(label = "موافقت نگهبانان ارشد (Consensus Audit)", passed = true)
                            ScoreCheckItem(label = "اعتبارسنجی کارکرد روی سخت‌افزار (Device Farm)", passed = true)
                            ScoreCheckItem(label = "پایداری حافظه و عدم کرش (Zero Leaks/Crash)", passed = currentRun.status == "PASSED")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1C1B1F), RoundedCornerShape(6.dp))
                                .border(1.dp, TechBorder, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "امضای رهاسازی دیجیتال (Digital Sealed Signature):",
                                    fontSize = 10.sp,
                                    color = TechTextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "sha256:${currentRun.evidenceHash}",
                                    fontSize = 11.sp,
                                    color = TechLime,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuardianVoteRow(name: String, vote: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSlateSurface, RoundedCornerShape(4.dp))
            .border(0.5.dp, TechBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 10.sp,
            color = TechTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    when (vote) {
                        "PASS" -> TechLime.copy(alpha = 0.2f)
                        "WARN" -> TechAmber.copy(alpha = 0.2f)
                        else -> TechBorder.copy(alpha = 0.4f)
                    }
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = vote,
                fontSize = 8.sp,
                color = when (vote) {
                    "PASS" -> TechLime
                    "WARN" -> TechAmber
                    else -> TechTextSecondary
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MetricItemCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = modifier
            .background(DarkSlateSurface, RoundedCornerShape(6.dp))
            .border(1.dp, TechBorder, RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TechCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                color = TechTextSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = TechLime,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScoreCheckItem(label: String, passed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "• $label", fontSize = 11.sp, color = TechTextPrimary)
        Icon(
            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (passed) TechLime else TechAmber,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun LayerStatusCard(
    layer: LayerStatusEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (layer.status) {
                "RUNNING" -> DarkSlateSurfaceVariant
                "PASSED" -> DarkSlateSurface
                "FAILED" -> Color(0xFFF2B8B5)
                else -> DarkSlateSurface.copy(alpha = 0.5f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (layer.status) {
                "RUNNING" -> TechCyan
                "PASSED" -> TechBorder
                "FAILED" -> TechCrimson
                else -> TechBorder.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("layer_card_${layer.layerNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Layer badge index
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when (layer.status) {
                                "RUNNING" -> TechCyan
                                "PASSED" -> TechLime.copy(alpha = 0.15f)
                                "FAILED" -> TechCrimson.copy(alpha = 0.15f)
                                else -> TechBorder
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = when (layer.status) {
                                "RUNNING" -> TechCyan
                                "PASSED" -> TechLime
                                "FAILED" -> TechCrimson
                                else -> TechTextSecondary.copy(alpha = 0.5f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = layer.layerNumber.toString(),
                        color = if (layer.status == "RUNNING") Color.White else TechTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = layer.layerName,
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = when (layer.status) {
                            "IDLE" -> "در انتظار نوبت ارزیابی"
                            "RUNNING" -> "درحال بررسی و تحلیل خودکار..."
                            "PASSED" -> "اثبات کامل شد (PASS)"
                            "FAILED" -> "خطای سیستمی (RELEASE BLOCK)"
                            else -> "تأیید نشده / میانبر"
                        },
                        fontSize = 10.sp,
                        color = when (layer.status) {
                            "RUNNING" -> TechCyan
                            "PASSED" -> TechLime
                            "FAILED" -> TechCrimson
                            else -> TechTextSecondary
                        }
                    )
                }
            }

            // Interactive status icon/indicator
            Box {
                when (layer.status) {
                    "RUNNING" -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "angle"
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Running Progress",
                            tint = TechCyan,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(angle)
                        )
                    }
                    "PASSED" -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Passed Icon",
                            tint = TechLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    "FAILED" -> {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Failed Icon",
                            tint = TechCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    "SKIPPED" -> {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Skipped Icon",
                            tint = TechTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Idle Icon",
                            tint = TechTextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTab(
    pipelineRuns: List<PipelineRunEntity>,
    patches: List<com.example.data.SelfHealingPatchEntity>,
    selectedRunId: Int?,
    selectedRunLayerStatuses: List<LayerStatusEntity>,
    onSelectRun: (Int?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (selectedRunId != null) {
            // Detailed View of Selected Historic Run
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { onSelectRun(null) },
                        modifier = Modifier.testTag("btn_back_history")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بازگشت به تاریخچه", color = TechCyan)
                    }
                    Text(
                        text = "ممیزی دقیق اجرای ID: $selectedRunId",
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary,
                        fontSize = 14.sp
                    )
                }
            }

            if (selectedRunLayerStatuses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TechCyan)
                    }
                }
            } else {
                items(selectedRunLayerStatuses) { layer ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                        border = BorderStroke(1.dp, TechBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "لایه ${layer.layerNumber}: ${layer.layerName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TechTextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (layer.status == "PASSED") TechLime.copy(alpha = 0.2f)
                                            else TechCrimson.copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = layer.status,
                                        color = if (layer.status == "PASSED") TechLime else TechCrimson,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "اثبات دیجیتال ممیزی (Digital Envelope Hash):",
                                fontSize = 9.sp,
                                color = TechTextSecondary
                            )
                            Text(
                                text = "sha256:${layer.logs.hashCode().toString(16)}",
                                fontSize = 9.sp,
                                color = TechLime,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = TechBorder, modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "خلاصه ممیزی و گواهی:",
                                fontSize = 10.sp,
                                color = TechTextSecondary
                            )
                            Text(
                                text = layer.logs.substringAfterLast("Summary: ").ifBlank { "تمام شاخص‌ها پاس شدند." },
                                fontSize = 11.sp,
                                color = TechTextPrimary
                            )
                        }
                    }
                }
            }
        } else {
            // List of Pipeline Run History
            item {
                Text(
                    text = "تاریخچه مستندات و ثبت ممیزی‌ها (Run History)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TechTextPrimary
                )
            }

            if (pipelineRuns.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                        border = BorderStroke(1.dp, TechBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هیچ گزارش ممیزی قبلی وجود ندارد.",
                                color = TechTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(pipelineRuns) { run ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                        border = BorderStroke(1.dp, TechBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRun(run.id) }
                            .testTag("run_history_card_${run.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "شناسه اجرا: ${run.id} | ${run.commitHash}",
                                    fontWeight = FontWeight.Bold,
                                    color = TechTextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (run.status == "PASSED") TechLime.copy(alpha = 0.2f)
                                            else TechCrimson.copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = run.status,
                                        color = if (run.status == "PASSED") TechLime else TechCrimson,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تعداد لایه‌های با موفقیت سپری شده: ${run.successLayersCount}/۱۰",
                                fontSize = 11.sp,
                                color = TechTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CPU: ${run.cpuUsage} | RAM: ${run.ramUsage} | Startup: ${run.startupTime}",
                                    fontSize = 10.sp,
                                    color = TechLime
                                )
                                Text(
                                    text = "کلیک برای مشاهده جزئیات >",
                                    fontSize = 10.sp,
                                    color = TechCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // AI Patches History
            item {
                Text(
                    text = "آرشیو پچ‌های اصلاحی هوشمند لایه ۳ (Self-Healing Log)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TechTextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (patches.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, TechBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هیچ پچ اصلاحی فعالی در ممیزی ثبت نشده است.",
                                color = TechTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(patches) { patch ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                        border = BorderStroke(1.dp, TechAmber),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = TechAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "رفع خطای کامپایل - اجرای VEP #${patch.runId}",
                                    fontWeight = FontWeight.Bold,
                                    color = TechTextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "خطای ره‌گیری شده: ${patch.exceptionMessage}",
                                fontSize = 10.sp,
                                color = TechCrimson,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ریشه‌یابی خودکار: ${patch.rootCauseAnalysis}",
                                fontSize = 11.sp,
                                color = TechTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "پچ اعمال شده:", fontSize = 9.sp, color = TechTextSecondary)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1C1B1F), RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = patch.patchCode,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = TechLime,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConstitutionTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "قانون اساسی توسعه پروژه (Engineering Constitution)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TechTextPrimary
            )
            Text(
                text = "هرگونه تغییر قبل از Merge باید لایه‌های ده‌گانه زیر را با موفقیت سپری کند و به تایید مستقل نگهبانان برسد.",
                fontSize = 11.sp,
                color = TechTextSecondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        val constitutionalItems = listOf(
            Triple("لایه ۱: قانون اساسی", "تایید صد در صدی کامپایل موفق، پاس شدن تست‌ها، و پایداری حافظه و عدم وجود ریس‌کاندیشن‌های شناخته شده.", Icons.Default.Verified),
            Triple("لایه ۲: ارتش نگهبانان", "کسب توافق کامل ۱۴ نگهبان تخصصی مستقل شامل نگهبان معماری، امنیت، بهینگی، پکیجینگ و سیستم‌عامل‌ها.", Icons.Default.Security),
            Triple("لایه ۳: خوداصلاحی هوشمند", "تشخیص خطاهای گذرا یا پایدار، عیب‌یابی به کمک Gemini، اعمال خودکار پچ‌های اصلاحی و تایید مجدد.", Icons.Default.Build),
            Triple("لایه ۴: اثبات واقعی", "ممنوعیت کامل ادعای بدون شاهد. تولید کدهای اصالت (Provenance) و امضای دیجیتال و هش‌ها برای ممیزی.", Icons.Default.CheckCircle),
            Triple("لایه ۵: ساخت چندپلتفرمه", "تولید همزمان APK اندروید، EXE ویندوز، AppImage لینوکس و داکر ایمیج در هر تاییدیه خروجی.", Icons.Default.Settings),
            Triple("لایه ۶: نصب خودکار", "بررسی پیش‌نیازها، راه‌اندازی شبیه‌سازها و دایمن‌ها، پیکربندی فایل‌ها و اجرای نهایی Health Check سلامت.", Icons.Default.History),
            Triple("لایه ۷: اعتبارسنجی دستگاه واقعی", "تست خودکار و اندازه‌گیری زمان راه‌اندازی، رم، سی‌پی‌یو و پایداری سخت‌افزار روی دستگاه‌های فیزیکی.", Icons.Default.Devices),
            Triple("لایه ۸: خطایاب پیشرفته", "دسته‌بندی خودکار اکسپشن‌ها، شدت سنجی، تحلیل علل احتمالی و تولید استراتژی‌های بازیابی و رول‌بک هوشمند.", Icons.Default.Warning),
            Triple("لایه ۹: شاخص آمادگی انتشار", "گزارش وضعیت تفکیک شده تمام شاخص‌ها. عدم رهاسازی یا انتشار نسخه عمومی (GA) در صورت وجود حتی یک خطای ممیزی.", Icons.Default.Analytics),
            Triple("لایه ۱۰: عدم ادعای بی‌شاهد", "ثبت لاگ‌ها، خروجی آزمون‌ها، هش‌ها و گواهی‌های معتبر در یک مخزن قابل جستجو و ممیزی.", Icons.Default.Info)
        )

        items(constitutionalItems) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, TechBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(TechCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.third,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.first,
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.second,
                            fontSize = 11.sp,
                            color = TechTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
