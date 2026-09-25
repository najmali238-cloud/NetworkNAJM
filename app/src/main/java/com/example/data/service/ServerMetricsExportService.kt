package com.example.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.CriticalServer
import com.example.data.model.ServerMetric
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Format options for exporting server metrics telemetry.
 */
enum class ServerMetricsExportFormat(
    val title: String,
    val titleAr: String,
    val extension: String,
    val mimeType: String
) {
    CSV("CSV Spreadsheet", "جدول بيانات CSV", ".csv", "text/csv"),
    JSON("Structured JSON", "ملف JSON مهيكل", ".json", "application/json")
}

/**
 * Encapsulates the exported file metadata, content, and file provider URI.
 */
data class ServerMetricsExportResult(
    val file: File,
    val uri: Uri,
    val format: ServerMetricsExportFormat,
    val fileSizeBytes: Long,
    val generatedTimestamp: String,
    val fileName: String,
    val rawContent: String,
    val serverCount: Int
)

/**
 * Service to generate, format, save, and share server metrics in CSV and JSON formats
 * allowing users to archive their telemetry reports.
 */
class ServerMetricsExportService(private val context: Context) {

    /**
     * Generates a fully compliant RFC-4180 CSV representation of the current server metrics data.
     */
    fun generateCsv(
        serverMetrics: List<ServerMetric>,
        criticalServers: List<CriticalServer> = emptyList(),
        includeServices: Boolean = true,
        includeSummary: Boolean = true,
        filterServerId: String? = null
    ): String {
        val filtered = if (filterServerId != null && filterServerId != "ALL") {
            serverMetrics.filter { it.serverId == filterServerId }
        } else {
            serverMetrics
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val isoTimestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date())

        val totalHosts = filtered.size.coerceAtLeast(1)
        val avgCpu = if (filtered.isNotEmpty()) filtered.map { it.cpuPercent }.average().toFloat() else 0f
        val avgRam = if (filtered.isNotEmpty()) filtered.map { it.ramPercent }.average().toFloat() else 0f
        val avgDisk = if (filtered.isNotEmpty()) filtered.map { it.diskPercent }.average().toFloat() else 0f
        val avgTemp = if (filtered.isNotEmpty()) filtered.map { it.temperatureC }.average().toFloat() else 0f
        val totalProcesses = filtered.sumOf { it.processesCount }
        val highLoadCount = filtered.count { it.cpuPercent >= 80f || it.ramPercent >= 80f }
        val totalRunningServices = filtered.flatMap { it.services }.count { it.isRunning }
        val totalServicesCount = filtered.flatMap { it.services }.size

        return buildString {
            // Header metadata comments
            appendLine("# ==============================================================================")
            appendLine("# NETGUARD ENTERPRISE - SERVER METRICS & TELEMETRY ARCHIVE REPORT")
            appendLine("# Exported At: $timestamp ($isoTimestamp)")
            appendLine("# Scope: ${if (filterServerId != null && filterServerId != "ALL") "Server $filterServerId" else "All Monitored Server Nodes ($totalHosts servers)"}")
            appendLine("# Overall Status: ${if (highLoadCount > 0) "WARNING ($highLoadCount hosts high load)" else "OPTIMAL (All nodes normal)"}")
            appendLine("# ==============================================================================")

            // CSV Column Headers
            appendLine(
                listOf(
                    "Server ID",
                    "Server Name",
                    "IP Address",
                    "Status",
                    "CPU Usage (%)",
                    "RAM Usage (%)",
                    "Disk Usage (%)",
                    "Temperature (C)",
                    "Active Processes",
                    "Running Services",
                    "Total Services",
                    "Services Detail",
                    "Last Updated",
                    "Export Timestamp"
                ).joinToString(",") { escapeCsv(it) }
            )

            // Data Rows for ServerMetric
            for (server in filtered) {
                val status = when {
                    server.cpuPercent >= 85f || server.ramPercent >= 90f -> "CRITICAL"
                    server.cpuPercent >= 75f || server.ramPercent >= 80f -> "HIGH_LOAD"
                    else -> "HEALTHY"
                }

                val runningSvcs = server.services.count { it.isRunning }
                val totalSvcs = server.services.size
                val servicesDetail = if (includeServices) {
                    server.services.joinToString(" | ") { svc ->
                        "${svc.name}:${if (svc.isRunning) "RUNNING" else "DOWN"}(port:${svc.port})"
                    }
                } else {
                    "$runningSvcs/$totalSvcs running"
                }

                val rowValues = listOf(
                    server.serverId,
                    server.serverName,
                    server.ip,
                    status,
                    String.format(Locale.US, "%.1f", server.cpuPercent),
                    String.format(Locale.US, "%.1f", server.ramPercent),
                    String.format(Locale.US, "%.1f", server.diskPercent),
                    String.format(Locale.US, "%.1f", server.temperatureC),
                    server.processesCount.toString(),
                    runningSvcs.toString(),
                    totalSvcs.toString(),
                    servicesDetail,
                    server.lastUpdated,
                    timestamp
                )

                appendLine(rowValues.joinToString(",") { escapeCsv(it) })
            }

            // Optional inclusion of critical servers if applicable
            if (criticalServers.isNotEmpty() && (filterServerId == null || filterServerId == "ALL")) {
                for (cs in criticalServers) {
                    if (filtered.none { it.serverId == cs.id }) {
                        val servicesFormatted = if (cs.services.isNotEmpty()) {
                            cs.services.joinToString("; ") { "${it.name}:${if (it.isRunning) "RUNNING" else "DOWN"}(port:${it.port})" }
                        } else {
                            "${cs.role}:RUNNING"
                        }
                        val rowValues = listOf(
                            cs.id,
                            cs.name,
                            cs.ip,
                            cs.status.name,
                            String.format(Locale.US, "%.1f", cs.cpuPercent),
                            String.format(Locale.US, "%.1f", cs.ramPercent),
                            String.format(Locale.US, "%.1f", cs.diskPercent),
                            String.format(Locale.US, "%.1f", cs.temperatureC),
                            "N/A",
                            cs.services.size.toString(),
                            cs.services.count { it.isRunning }.toString(),
                            servicesFormatted,
                            "Uptime ${cs.uptimePercent}% (Ping ${cs.latencyMs}ms, ${cs.lastPing})",
                            timestamp
                        )
                        appendLine(rowValues.joinToString(",") { escapeCsv(it) })
                    }
                }
            }

            // Cluster Aggregate Summary Row
            if (includeSummary) {
                appendLine()
                appendLine("# --- CLUSTER AGGREGATE SUMMARY ---")
                val summaryRow = listOf(
                    "AGGREGATE",
                    "Cluster Aggregate (${filtered.size} nodes)",
                    "Pooled Subnet",
                    if (highLoadCount > 0) "WARNING" else "OPTIMAL",
                    String.format(Locale.US, "%.1f", avgCpu),
                    String.format(Locale.US, "%.1f", avgRam),
                    String.format(Locale.US, "%.1f", avgDisk),
                    String.format(Locale.US, "%.1f", avgTemp),
                    totalProcesses.toString(),
                    totalRunningServices.toString(),
                    totalServicesCount.toString(),
                    "$totalRunningServices/$totalServicesCount Operational Services across cluster",
                    timestamp,
                    timestamp
                )
                appendLine(summaryRow.joinToString(",") { escapeCsv(it) })
            }
        }
    }

    /**
     * Generates a structured JSON archive containing server telemetry, cluster summaries,
     * service breakdowns, and metadata.
     */
    fun generateJson(
        serverMetrics: List<ServerMetric>,
        criticalServers: List<CriticalServer> = emptyList(),
        includeServices: Boolean = true,
        includeSummary: Boolean = true,
        filterServerId: String? = null
    ): String {
        val filtered = if (filterServerId != null && filterServerId != "ALL") {
            serverMetrics.filter { it.serverId == filterServerId }
        } else {
            serverMetrics
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date())
        val root = JSONObject()

        // Metadata section
        val metadata = JSONObject()
        metadata.put("reportType", "NetGuard Server Telemetry & Metrics Archive")
        metadata.put("exportTimestamp", timestamp)
        metadata.put("schemaVersion", "2.4-enterprise")
        metadata.put("generator", "NetGuard Enterprise Sentinel v2.4")
        metadata.put("filteredServer", filterServerId ?: "ALL")
        metadata.put("totalServersMonitored", filtered.size)
        val highLoadCount = filtered.count { it.cpuPercent >= 80f || it.ramPercent >= 80f }
        metadata.put("healthStatus", if (highLoadCount > 0) "WARNING" else "OPTIMAL")
        root.put("metadata", metadata)

        // Cluster KPI Summary section
        if (includeSummary) {
            val summary = JSONObject()
            val avgCpu = if (filtered.isNotEmpty()) filtered.map { it.cpuPercent }.average().toFloat() else 0f
            val avgRam = if (filtered.isNotEmpty()) filtered.map { it.ramPercent }.average().toFloat() else 0f
            val avgDisk = if (filtered.isNotEmpty()) filtered.map { it.diskPercent }.average().toFloat() else 0f
            val avgTemp = if (filtered.isNotEmpty()) filtered.map { it.temperatureC }.average().toFloat() else 0f
            val totalProcesses = filtered.sumOf { it.processesCount }
            val runningSvcs = filtered.flatMap { it.services }.count { it.isRunning }
            val totalSvcs = filtered.flatMap { it.services }.size

            summary.put("averageCpuPercent", Math.round(avgCpu * 10.0) / 10.0)
            summary.put("averageRamPercent", Math.round(avgRam * 10.0) / 10.0)
            summary.put("averageDiskPercent", Math.round(avgDisk * 10.0) / 10.0)
            summary.put("averageTemperatureC", Math.round(avgTemp * 10.0) / 10.0)
            summary.put("totalProcesses", totalProcesses)
            summary.put("highLoadServersCount", highLoadCount)
            summary.put("runningServicesCount", runningSvcs)
            summary.put("totalServicesCount", totalSvcs)
            summary.put("clusterLoadStatus", if (avgCpu > 75f || avgRam > 80f || highLoadCount > 0) "HIGH_LOAD" else "BALANCED")
            root.put("clusterSummary", summary)
        }

        // Servers array
        val serversArray = JSONArray()
        for (server in filtered) {
            val serverObj = JSONObject()
            serverObj.put("serverId", server.serverId)
            serverObj.put("serverName", server.serverName)
            serverObj.put("ip", server.ip)
            serverObj.put("cpuPercent", server.cpuPercent)
            serverObj.put("ramPercent", server.ramPercent)
            serverObj.put("diskPercent", server.diskPercent)
            serverObj.put("temperatureC", server.temperatureC)
            serverObj.put("processesCount", server.processesCount)
            serverObj.put("lastUpdated", server.lastUpdated)

            val healthStatus = when {
                server.cpuPercent >= 85f || server.ramPercent >= 90f -> "CRITICAL"
                server.cpuPercent >= 75f || server.ramPercent >= 80f -> "HIGH_LOAD"
                else -> "HEALTHY"
            }
            serverObj.put("healthStatus", healthStatus)

            if (includeServices) {
                val servicesArray = JSONArray()
                for (svc in server.services) {
                    val svcObj = JSONObject()
                    svcObj.put("name", svc.name)
                    svcObj.put("isRunning", svc.isRunning)
                    svcObj.put("port", svc.port)
                    servicesArray.put(svcObj)
                }
                serverObj.put("services", servicesArray)
            }
            serversArray.put(serverObj)
        }

        // Append critical servers if available
        if (criticalServers.isNotEmpty() && (filterServerId == null || filterServerId == "ALL")) {
            for (cs in criticalServers) {
                if (filtered.none { it.serverId == cs.id }) {
                    val csObj = JSONObject()
                    csObj.put("serverId", cs.id)
                    csObj.put("serverName", cs.name)
                    csObj.put("ip", cs.ip)
                    csObj.put("role", cs.role)
                    csObj.put("cpuPercent", cs.cpuPercent)
                    csObj.put("ramPercent", cs.ramPercent)
                    csObj.put("diskPercent", cs.diskPercent)
                    csObj.put("temperatureC", cs.temperatureC)
                    csObj.put("uptimePercent", cs.uptimePercent)
                    csObj.put("latencyMs", cs.latencyMs)
                    csObj.put("lastPing", cs.lastPing)
                    csObj.put("status", cs.status.name)
                    if (includeServices && cs.services.isNotEmpty()) {
                        val svcArr = JSONArray()
                        cs.services.forEach { svc ->
                            val sObj = JSONObject()
                            sObj.put("name", svc.name)
                            sObj.put("isRunning", svc.isRunning)
                            sObj.put("port", svc.port)
                            svcArr.put(sObj)
                        }
                        csObj.put("services", svcArr)
                    }
                    serversArray.put(csObj)
                }
            }
        }

        root.put("servers", serversArray)
        return root.toString(2)
    }

    /**
     * Exports the server metrics to a local file in the app cache / reports directory
     * and returns a [ServerMetricsExportResult] with a FileProvider URI.
     */
    fun exportToFile(
        format: ServerMetricsExportFormat,
        serverMetrics: List<ServerMetric>,
        criticalServers: List<CriticalServer> = emptyList(),
        includeServices: Boolean = true,
        includeSummary: Boolean = true,
        filterServerId: String? = null
    ): ServerMetricsExportResult {
        val rawContent = if (format == ServerMetricsExportFormat.CSV) {
            generateCsv(
                serverMetrics = serverMetrics,
                criticalServers = criticalServers,
                includeServices = includeServices,
                includeSummary = includeSummary,
                filterServerId = filterServerId
            )
        } else {
            generateJson(
                serverMetrics = serverMetrics,
                criticalServers = criticalServers,
                includeServices = includeServices,
                includeSummary = includeSummary,
                filterServerId = filterServerId
            )
        }

        val reportsDir = File(context.cacheDir, "reports").apply {
            if (!exists()) mkdirs()
        }

        val fileTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val fileName = "server_metrics_archive_$fileTimestamp${format.extension}"
        val exportFile = File(reportsDir, fileName)

        exportFile.writeText(rawContent, Charsets.UTF_8)

        val contentUri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )
        } catch (e: Exception) {
            Uri.fromFile(exportFile)
        }

        val filteredCount = if (filterServerId != null && filterServerId != "ALL") {
            serverMetrics.count { it.serverId == filterServerId }
        } else {
            serverMetrics.size
        }

        return ServerMetricsExportResult(
            file = exportFile,
            uri = contentUri,
            format = format,
            fileSizeBytes = exportFile.length(),
            generatedTimestamp = formattedDate,
            fileName = fileName,
            rawContent = rawContent,
            serverCount = filteredCount
        )
    }

    /**
     * Writes exported content directly to a destination URI selected via Storage Access Framework (SAF).
     */
    fun writeToUri(targetUri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens Android system sharesheet to share or send the exported metrics archive.
     */
    fun shareExport(result: ServerMetricsExportResult) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = result.format.mimeType
            putExtra(Intent.EXTRA_SUBJECT, "NetGuard Server Metrics Archive (${result.fileName})")
            putExtra(Intent.EXTRA_TEXT, result.rawContent)
            putExtra(Intent.EXTRA_STREAM, result.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Archive / Share Server Metrics Telemetry")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Escapes values for CSV compatibility (RFC 4180).
     */
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            "\"$value\""
        }
    }
}
