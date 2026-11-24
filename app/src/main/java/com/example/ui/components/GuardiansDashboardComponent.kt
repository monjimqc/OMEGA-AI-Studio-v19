package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class GuardianInfo(
    val name: String,
    val tag: String,
    val category: String,
    val description: String,
    val policyRule: String
)

val ALL_28_GUARDIANS = listOf(
    GuardianInfo("Architecture Guardian", "ARCH", "Governance & System", "Enforces Clean Architecture, Layer boundaries, circular dependency detection, and ADR compliance.", "Policy ARCH-01: Zero circular dependencies & strict layer boundary enforcement."),
    GuardianInfo("Backend Guardian", "BACK", "Core & Services", "Verifies API specs, server handler isolation, fault tolerance, and backend error handling.", "Policy BACK-01: 100% endpoint handler isolation & graceful exception mapping."),
    GuardianInfo("Frontend Guardian", "FRONT", "UI & UX", "Validates Jetpack Compose state isolation, UI re-composition efficiency, and responsive layouts.", "Policy FRONT-01: Zero redundant recompositions & full WindowInsets handling."),
    GuardianInfo("Android Guardian", "ANDR", "Target Platform", "Validates Android manifest permissions, SDK compatibility, lifecycle management, and APK sizing.", "Policy ANDR-01: Android 14+ target SDK compliance & runtime permission safety."),
    GuardianInfo("Windows Guardian", "WIN", "Target Platform", "Validates Win32/MSI packaging specs, runtime dll bindings, and Windows execution safety.", "Policy WIN-01: Deterministic MSI/EXE binary compilation & signature check."),
    GuardianInfo("Linux Guardian", "LNX", "Target Platform", "Validates AppImage/DEB scripts, systemd unit integrity, and POSIX permissions.", "Policy LNX-01: POSIX standards compliance & sandbox execution verification."),
    GuardianInfo("Security Guardian", "SEC", "Cyber Security", "Performs SAST/DAST scans, secret detection, vulnerability checks, and threat modeling.", "Policy SEC-01: Zero critical CVE vulnerabilities & strict secret sanitization."),
    GuardianInfo("Cryptography Guardian", "CRYP", "Cyber Security", "Enforces TLS 1.3, SHA-256 evidence hashing, AES-256 payload encryption, and key rotation.", "Policy CRYP-01: Cryptographic evidence chain verification with SHA-256 integrity."),
    GuardianInfo("Database Guardian", "DB", "Storage & Data", "Validates Room/SQLite migrations, query performance, indexing, and transactional integrity.", "Policy DB-01: Schema version locking & zero destructive unmigrated tables."),
    GuardianInfo("API Guardian", "API", "Interoperability", "Enforces OpenAPI standards, backward compatibility, endpoint schema validation, and rate limits.", "Policy API-01: Strict OpenAPI contract adherence & breaking-change denial."),
    GuardianInfo("UI Guardian", "UI", "UI & Visual", "Enforces Material 3 design tokens, color contrast, spatial grid alignment, and visual polish.", "Policy UI-01: Material 3 design system compliance & high-density layout alignment."),
    GuardianInfo("UX Guardian", "UX", "UI & Visual", "Verifies touch target sizes (≥48dp), error recovery flows, loading states, and navigation safety.", "Policy UX-01: Minimum 48dp touch target size & non-blocking user feedback."),
    GuardianInfo("Accessibility Guardian", "A11Y", "Quality & Standards", "Audits TalkBack screen reader content descriptions, font scaling, and color contrast compliance.", "Policy A11Y-01: 100% contentDescription coverage & WCAG AAA contrast ratio."),
    GuardianInfo("Performance Guardian", "PERF", "Runtime & Reliability", "Monitors startup times (<1.2s), frame rates (60fps), background thread usage, and CPU throttle.", "Policy PERF-01: App cold-start time <1200ms & 60 FPS UI rendering continuity."),
    GuardianInfo("Memory Guardian", "MEM", "Runtime & Reliability", "Detects memory leaks, GC pressure, heap usage thresholds, and allocation velocity.", "Policy MEM-01: Zero leak suspects in heap dumps & stable RAM utilization <128MB."),
    GuardianInfo("Concurrency Guardian", "CONC", "Runtime & Reliability", "Prevents deadlocks, race conditions, thread starvation, and coroutine scope leaks.", "Policy CONC-01: Thread-safe coroutine dispatching & zero unhandled exceptions in scopes."),
    GuardianInfo("Static Analysis Guardian", "SAST", "Code Quality", "Runs KtLint, Android Lint, code smell checks, complexity metrics, and dead code elimination.", "Policy SAST-01: Zero fatal lint errors & cyclomatic complexity threshold <= 10."),
    GuardianInfo("Test Guardian", "TEST", "Verification", "Enforces unit/integration test coverage (≥90%), test pass rates (100%), and mutation tests.", "Policy TEST-01: 100% test pass rate & strict coverage verification requirement."),
    GuardianInfo("Chaos Guardian", "CHAOS", "Resilience", "Simulates network drops, memory pressure, random crashes, and unexpected state mutations.", "Policy CHAOS-01: System resilience against network drop & memory stress faults."),
    GuardianInfo("Recovery Guardian", "RECOV", "Resilience", "Validates automatic crash recovery, circuit breakers, self-diagnostics, and rollback safety.", "Policy RECOV-01: Mean-Time-To-Recover (MTTR) < 5s & auto-healing verification."),
    GuardianInfo("Deployment Guardian", "DEPL", "Release & Operations", "Audits automated deployment scripts, health check readiness, containerization, and uninstallation.", "Policy DEPL-01: Automated multi-environment container & script qualification."),
    GuardianInfo("Documentation Guardian", "DOCS", "Knowledge", "Ensures API docs, README, ADRs, architecture diagrams, and release notes are up-to-date.", "Policy DOCS-01: 100% documentation completeness & ADR trace linkage."),
    GuardianInfo("SBOM Guardian", "SBOM", "Supply Chain", "Generates and validates CycloneDX/SPDX Software Bill of Materials for all dependencies.", "Policy SBOM-01: Complete, verifiable CycloneDX SBOM manifest generation."),
    GuardianInfo("Supply Chain Guardian", "CHAIN", "Supply Chain", "Audits third-party library signatures, dependency license compatibility, and hash locks.", "Policy CHAIN-01: Dependency hash locking & permissible open-source license check."),
    GuardianInfo("Compliance Guardian", "COMP", "Governance", "Validates Executable Engineering Constitution rules, regulatory policies, and zero trust rules.", "Policy COMP-01: 100% Executable Constitution compliance (PASS or DENY)."),
    GuardianInfo("Evidence Guardian", "EVID", "Governance", "Binds code commits to test reports, build artifacts, SBOMs, and cryptographic signatures.", "Policy EVID-01: Immutable evidence graph binding for every commit hash."),
    GuardianInfo("Verification Guardian", "VERIF", "Verification", "Executes independent verification scripts to re-prove build artifacts and test claims.", "Policy VERIF-01: Independent replay package verification (100% pass required)."),
    GuardianInfo("Release Guardian", "RELS", "Release Authority", "Final Release Authority that grants or denies GA release based on 100% PASS consensus across all 28 Guardians.", "Policy RELS-01: Release Authority decision rule (ALL 28 PASS -> RELEASE ALLOW, else DENY).")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardiansDashboardComponent(
    guardianVotes: Map<String, String>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PASS, FAIL, PENDING, WARN
    var selectedGuardianInfo by remember { mutableStateOf<GuardianInfo?>(null) }

    // Map votes for all 28 guardians (defaults to PENDING if not voted yet)
    val guardianStatusMap = remember(guardianVotes) {
        ALL_28_GUARDIANS.associate { info ->
            info.name to (guardianVotes[info.name] ?: "PENDING")
        }
    }

    // Counts
    val passCount = guardianStatusMap.values.count { it == "PASS" }
    val failCount = guardianStatusMap.values.count { it == "FAIL" }
    val warnCount = guardianStatusMap.values.count { it == "WARN" }
    val pendingCount = guardianStatusMap.values.count { it == "PENDING" || it == "IDLE" }

    // Filtered list
    val filteredGuardians = remember(searchQuery, selectedFilter, guardianStatusMap) {
        ALL_28_GUARDIANS.filter { info ->
            val status = guardianStatusMap[info.name] ?: "PENDING"
            val matchesFilter = when (selectedFilter) {
                "PASS" -> status == "PASS"
                "FAIL" -> status == "FAIL"
                "WARN" -> status == "WARN"
                "PENDING" -> status == "PENDING" || status == "IDLE"
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    info.name.contains(searchQuery, ignoreCase = true) ||
                    info.tag.contains(searchQuery, ignoreCase = true) ||
                    info.category.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }

    val overallConsensusStatus = when {
        failCount > 0 -> "DENIED (CRITICAL BLOCKER DETECTED)"
        passCount == 28 -> "GA APPROVED (100% CONSENSUS REACHED)"
        warnCount > 0 && passCount + warnCount == 28 -> "CONDITIONAL PASS (WARNINGS DETECTED)"
        else -> "VERIFICATION IN PROGRESS ($passCount/28 VOTED)"
    }

    val consensusColor = when {
        failCount > 0 -> TechCrimson
        passCount == 28 -> TechLime
        warnCount > 0 && passCount + warnCount == 28 -> TechAmber
        else -> TechCyan
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, TechBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("guardians_dashboard_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Title & Consensus Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(TechCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "28 AI Guardians",
                            tint = TechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "داشبورد نگهبانان هوش مصنوعی (28 AI Guardians Army)",
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Layer 2 Consensus Engine & Policy Enforcement",
                            fontSize = 10.sp,
                            color = TechTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Total Count Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSlateSurfaceVariant,
                    border = BorderStroke(1.dp, TechBorder)
                ) {
                    Text(
                        text = "28 / 28",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechCyan,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Consensus Status Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = consensusColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, consensusColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                failCount > 0 -> Icons.Default.Cancel
                                passCount == 28 -> Icons.Default.Verified
                                warnCount > 0 -> Icons.Default.Warning
                                else -> Icons.Default.Sync
                            },
                            contentDescription = null,
                            tint = consensusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = overallConsensusStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = consensusColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "Layer 10 Authority Rule",
                        fontSize = 9.sp,
                        color = TechTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Counters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusCounterBadge(
                    label = "PASS",
                    count = passCount,
                    color = TechLime,
                    isSelected = selectedFilter == "PASS",
                    onClick = { selectedFilter = if (selectedFilter == "PASS") "ALL" else "PASS" },
                    modifier = Modifier.weight(1f)
                )
                StatusCounterBadge(
                    label = "WARN",
                    count = warnCount,
                    color = TechAmber,
                    isSelected = selectedFilter == "WARN",
                    onClick = { selectedFilter = if (selectedFilter == "WARN") "ALL" else "WARN" },
                    modifier = Modifier.weight(1f)
                )
                StatusCounterBadge(
                    label = "FAIL",
                    count = failCount,
                    color = TechCrimson,
                    isSelected = selectedFilter == "FAIL",
                    onClick = { selectedFilter = if (selectedFilter == "FAIL") "ALL" else "FAIL" },
                    modifier = Modifier.weight(1f)
                )
                StatusCounterBadge(
                    label = "PENDING",
                    count = pendingCount,
                    color = TechCyan,
                    isSelected = selectedFilter == "PENDING",
                    onClick = { selectedFilter = if (selectedFilter == "PENDING") "ALL" else "PENDING" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "جستجو در ۲۸ نگهبان (مثلاً SEC, Memory, SBOM)...",
                        fontSize = 11.sp,
                        color = TechTextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TechTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TechTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("guardian_search_input"),
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = TechTextPrimary),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechCyan,
                    unfocusedBorderColor = TechBorder,
                    focusedContainerColor = DarkSlateBg,
                    unfocusedContainerColor = DarkSlateBg
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 28 Guardians Grid View
            Box(modifier = Modifier.heightIn(max = 420.dp)) {
                if (filteredGuardians.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هیچ نگهبانی با این مشخصات یافت نشد.",
                            fontSize = 11.sp,
                            color = TechTextSecondary
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredGuardians, key = { it.name }) { guardian ->
                            val status = guardianStatusMap[guardian.name] ?: "PENDING"
                            GuardianCardItem(
                                info = guardian,
                                status = status,
                                onClick = { selectedGuardianInfo = guardian }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Details Dialog
    selectedGuardianInfo?.let { info ->
        val status = guardianStatusMap[info.name] ?: "PENDING"
        GuardianDetailModal(
            info = info,
            status = status,
            onDismiss = { selectedGuardianInfo = null }
        )
    }
}

@Composable
fun StatusCounterBadge(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else DarkSlateBg,
        border = BorderStroke(
            if (isSelected) 1.5.dp else 0.5.dp,
            if (isSelected) color else TechBorder
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) TechTextPrimary else TechTextSecondary
            )
        }
    }
}

@Composable
fun GuardianCardItem(
    info: GuardianInfo,
    status: String,
    onClick: () -> Unit
) {
    val statusColor = when (status) {
        "PASS" -> TechLime
        "FAIL" -> TechCrimson
        "WARN" -> TechAmber
        else -> TechCyan
    }

    val statusIcon = when (status) {
        "PASS" -> Icons.Default.CheckCircle
        "FAIL" -> Icons.Default.Cancel
        "WARN" -> Icons.Default.Warning
        else -> Icons.Default.HourglassEmpty
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = DarkSlateBg,
        border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guardian_card_${info.tag}")
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Short Tag Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = info.tag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = status,
                        tint = statusColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = status,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = info.name,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TechTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = info.category,
                fontSize = 9.sp,
                color = TechTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GuardianDetailModal(
    info: GuardianInfo,
    status: String,
    onDismiss: () -> Unit
) {
    val statusColor = when (status) {
        "PASS" -> TechLime
        "FAIL" -> TechCrimson
        "WARN" -> TechAmber
        else -> TechCyan
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("guardian_modal_close")
            ) {
                Text("بستن", color = TechCyan, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = info.tag,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = statusColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = info.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary
                    )
                    Text(
                        text = "Category: ${info.category}",
                        fontSize = 10.sp,
                        color = TechTextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Status Bar
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "وضعیت ممیزی فعلی:",
                            fontSize = 11.sp,
                            color = TechTextPrimary
                        )
                        Text(
                            text = status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Description
                Column {
                    Text(
                        text = "مسئولیت نگهبان:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TechCyan
                    )
                    Text(
                        text = info.description,
                        fontSize = 11.sp,
                        color = TechTextSecondary,
                        lineHeight = 16.sp
                    )
                }

                // Enforced Policy
                Column {
                    Text(
                        text = "قانون اجراشده (Policy-as-Code):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TechCyan
                    )
                    Text(
                        text = info.policyRule,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSlateBg, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )
                }

                // Simulated Evidence Signature
                Column {
                    Text(
                        text = "Cryptographic Evidence Signature:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = TechTextSecondary
                    )
                    Text(
                        text = "sha256:${info.name.hashCode().toUInt().toString(16)}...${info.tag.lowercase()}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechLime
                    )
                }
            }
        },
        containerColor = DarkSlateSurface,
        shape = RoundedCornerShape(12.dp)
    )
}
