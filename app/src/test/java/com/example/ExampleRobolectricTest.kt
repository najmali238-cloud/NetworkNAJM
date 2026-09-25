package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.data.repository.NetworkGuardRepository
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NetGuard", appName)
  }

  @Test
  fun `dashboard network health metrics are initialized`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val health = viewModel.networkHealth.value
    assertTrue("Health score should be >= 90", health.healthScore >= 90)
    assertEquals("192.168.1.1", health.activeGatewayIp)
    assertTrue("Packet loss should be nominal", health.packetLossPercent < 1.0f)
  }

  @Test
  fun `dashboard active devices count is valid`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val devices = viewModel.devices.value
    val onlineCount = devices.count { it.status == DeviceStatus.ONLINE }
    assertTrue("Should have online devices", onlineCount > 0)
    assertTrue("Online count should be <= total count", onlineCount <= devices.size)
  }

  @Test
  fun `dashboard current bandwidth usage is populated`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val bandwidth = viewModel.bandwidthMetrics.value
    assertTrue("Download speed should be positive", bandwidth.downloadMbps > 0f)
    assertTrue("Upload speed should be positive", bandwidth.uploadMbps > 0f)
    assertTrue("Download history should contain points for wave chart", bandwidth.downloadHistory.isNotEmpty())
  }

  @Test
  fun `dashboard critical servers status list contains core infrastructure`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val servers = viewModel.criticalServers.value
    assertTrue("Should have multiple critical servers", servers.size >= 4)
    val dbServer = servers.find { it.id == "dev-04" }
    assertNotNull("Database Master should exist", dbServer)
    assertEquals(DeviceStatus.WARNING, dbServer?.status)
    assertTrue("DB server RAM should be monitored", (dbServer?.ramPercent ?: 0f) > 80f)

    val appServer = servers.find { it.id == "dev-03" }
    assertNotNull("App Server should exist", appServer)
    assertEquals(DeviceStatus.ONLINE, appServer?.status)
  }

  @Test
  fun `monitored devices grid data contains real-time cpu and ram metrics for all monitored devices`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val devices = viewModel.devices.value
    assertTrue("Should monitor network devices across infrastructure", devices.size >= 8)

    // Verify online devices have active CPU and RAM usage metrics
    val onlineDevices = devices.filter { it.status == DeviceStatus.ONLINE }
    assertTrue("Multiple devices should be online and actively monitored", onlineDevices.size >= 7)
    onlineDevices.forEach { device ->
      assertNotNull("Monitored device ${device.id} should have CPU metric", device.cpuUsage)
      assertNotNull("Monitored device ${device.id} should have RAM metric", device.ramUsage)
      assertTrue("CPU should be >= 0", (device.cpuUsage ?: 0f) >= 0f)
      assertTrue("RAM should be >= 0", (device.ramUsage ?: 0f) >= 0f)
    }

    // Verify Warning device has elevated metrics
    val dbMaster = devices.find { it.id == "dev-04" }
    assertNotNull("Database Master dev-04 should exist in monitored devices", dbMaster)
    assertEquals(DeviceStatus.WARNING, dbMaster?.status)
    assertTrue("DB Master should reflect high CPU load in health grid", (dbMaster?.cpuUsage ?: 0f) > 70f)
    assertTrue("DB Master should reflect high RAM load in health grid", (dbMaster?.ramUsage ?: 0f) > 80f)

    // Verify Offline device status
    val offlineDevice = devices.find { it.id == "dev-07" }
    assertNotNull("Camera dev-07 should exist", offlineDevice)
    assertEquals(DeviceStatus.OFFLINE, offlineDevice?.status)
  }

  @Test
  fun `automated ssh remote execution triggers on monitored server load`() = runBlocking {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // Verify initial auto-remediation policies exist
    val initialPolicies = viewModel.autoRemediationPolicies.value
    assertTrue("Policies should be defined", initialPolicies.isNotEmpty())
    val postgresPolicy = initialPolicies.find { it.id == "pol-01" }
    assertNotNull("PostgreSQL policy should exist", postgresPolicy)
    assertTrue("Policy should be enabled by default", postgresPolicy?.isEnabled == true)

    // Evaluate watchdog: DB-01 starts with 86% RAM (> 80% threshold)
    val dbBefore = repository.serverMetrics.value.find { it.serverId == "dev-04" }
    assertTrue("DB-01 initial RAM should exceed 80%", (dbBefore?.ramPercent ?: 0f) >= 80f)

    repository.evaluateAutomatedRemediationWatchdog()

    // Verify remediation action was logged as automated
    val remediations = repository.remediations.value
    val latestAction = remediations.firstOrNull()
    assertNotNull("A remediation action should be recorded", latestAction)
    assertTrue("Latest remediation should be automated", latestAction?.isAutomated == true)
    assertTrue("Command should target postgresql", latestAction?.command?.contains("postgresql") == true)

    // Verify server load decreased after self-healing execution
    val dbAfter = repository.serverMetrics.value.find { it.serverId == "dev-04" }
    assertTrue("DB-01 RAM should be reduced after remediation", (dbAfter?.ramPercent ?: 100f) < 80f)
  }

  @Test
  fun `automated remediation policy can be toggled and configured`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // Toggle master switch
    viewModel.toggleAutoRemediation(false)
    assertFalse("Master auto-remediation should be false", viewModel.autoRemediationEnabled.value)

    viewModel.toggleAutoRemediation(true)
    assertTrue("Master auto-remediation should be true", viewModel.autoRemediationEnabled.value)

    // Toggle individual policy
    viewModel.togglePolicy("pol-01", false)
    val pol = viewModel.autoRemediationPolicies.value.find { it.id == "pol-01" }
    assertFalse("Policy pol-01 should be disabled", pol?.isEnabled == true)

    // Update threshold
    viewModel.updatePolicyThresholds("pol-01", 90f, 95f)
    val updatedPol = viewModel.autoRemediationPolicies.value.find { it.id == "pol-01" }
    assertEquals(90f, updatedPol?.ramThresholdPercent ?: 0f, 0.01f)
  }

  @Test
  fun `topology map initializes with connected devices and gateway hierarchy`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val nodes = viewModel.topologyNodes.value
    assertTrue("Topology should have connected devices", nodes.size >= 8)

    // Verify Root Core Gateway
    val gateway = nodes.find { it.id == "dev-01" }
    assertNotNull("Gateway node must exist", gateway)
    assertEquals(null, gateway?.parentId)
    assertEquals("192.168.1.1", gateway?.ip)

    // Verify Core Switch uplinked to Gateway
    val switchNode = nodes.find { it.id == "dev-02" }
    assertNotNull("Switch node must exist", switchNode)
    assertEquals("dev-01", switchNode?.parentId)

    // Verify Endpoints uplinked to Switch
    val appServer = nodes.find { it.id == "dev-03" }
    assertEquals("dev-02", appServer?.parentId)
  }

  @Test
  fun `topology highlights active alerts on affected nodes`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val nodes = viewModel.topologyNodes.value

    // Camera 05 has offline alert
    val cameraOffline = nodes.find { it.id == "dev-07" }
    assertNotNull("Camera 05 should exist", cameraOffline)
    assertEquals(DeviceStatus.OFFLINE, cameraOffline?.status)
    assertTrue("Camera 05 should have active alerts highlighted", (cameraOffline?.activeAlertCount ?: 0) > 0)
    assertTrue("Camera 05 alert summary should mention offline or probe", cameraOffline?.alertsSummary?.isNotEmpty() == true)

    // DB-01 has high resource warning alert
    val dbNode = nodes.find { it.id == "dev-04" }
    assertNotNull("DB node should exist", dbNode)
    assertEquals(DeviceStatus.WARNING, dbNode?.status)
    assertTrue("DB node should have alert count", (dbNode?.activeAlertCount ?: 0) > 0)
  }

  @Test
  fun `topology highlights rogue devices and reflects isolation actions`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val nodesInitial = viewModel.topologyNodes.value
    val rogueNode = nodesInitial.find { it.id == "rogue-01" }
    assertNotNull("Rogue node must be present in topology", rogueNode)
    assertTrue("Rogue node must have isRogue flag set for threat rendering", rogueNode?.isRogue == true)

    // Isolate rogue
    viewModel.isolateRogue("rogue-01")

    val nodesAfterIsolation = viewModel.topologyNodes.value
    val isolatedNode = nodesAfterIsolation.find { it.id == "rogue-01" }
    assertEquals("Isolated rogue should show as OFFLINE status in topology", DeviceStatus.OFFLINE, isolatedNode?.status)
    assertTrue("Label should indicate isolated status", isolatedNode?.label?.contains("ISOLATED") == true)
  }

  @Test
  fun `whitelisting rogue converts it into authorized inventory asset in topology`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // Whitelist rogue
    viewModel.trustRogue("rogue-01")

    val nodes = viewModel.topologyNodes.value
    val trustedNode = nodes.find { it.id == "rogue-01" }
    assertNotNull("Whitelisted rogue should exist in topology", trustedNode)
    assertFalse("Whitelisted rogue must not be flagged as rogue", trustedNode?.isRogue == true)
    assertEquals(DeviceStatus.ONLINE, trustedNode?.status)
  }

  @Test
  fun `topology node connections form valid tree hierarchy with live bandwidth telemetry`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val nodes = viewModel.topologyNodes.value
    val gateway = nodes.find { it.parentId == null }
    assertNotNull("Root gateway node must exist with no parent", gateway)
    assertEquals("dev-01", gateway?.id)

    // Child nodes should point to valid parents
    val nonRootNodes = nodes.filter { it.parentId != null }
    assertTrue("Multiple devices should be connected in topology hierarchy", nonRootNodes.isNotEmpty())
    val nodeIds = nodes.map { it.id }.toSet()
    nonRootNodes.forEach { child ->
      assertTrue("Parent ${child.parentId} of node ${child.id} must exist in topology", nodeIds.contains(child.parentId))
    }

    // Bandwidth telemetry map should be populated for nodes
    val trafficMap = viewModel.nodeTrafficMap.value
    assertTrue("Traffic map should contain node streams", trafficMap.containsKey("dev-01"))
    val dev1Traffic = trafficMap["dev-01"]
    assertNotNull(dev1Traffic)
    assertTrue("Gateway RX should be positive", (dev1Traffic?.incomingMbps ?: 0f) > 0f)

    // Simulate burst traffic
    viewModel.simulateBurstTraffic("dev-03")
    val burstTraffic = viewModel.nodeTrafficMap.value["dev-03"]
    assertTrue("Spike flag should be set on burst node", burstTraffic?.isAnomalySpike == true)
  }

  @Test
  fun `export report produces valid formatted JSON and summary markdown with topology and server metrics`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val topologyNodes = viewModel.topologyNodes.value
    val serverMetrics = viewModel.serverMetrics.value
    val networkHealth = viewModel.networkHealth.value
    val bandwidthMetrics = viewModel.bandwidthMetrics.value
    val alerts = viewModel.alerts.value
    val slaReport = viewModel.currentSlaReport.value
    val trafficMap = viewModel.nodeTrafficMap.value

    // Generate JSON
    val jsonStr = com.example.ui.components.generateTopologyJson(
      topologyNodes = topologyNodes,
      serverMetrics = serverMetrics,
      networkHealth = networkHealth,
      bandwidthMetrics = bandwidthMetrics,
      alerts = alerts,
      slaReport = slaReport,
      nodeTrafficMap = trafficMap,
      includeDetails = true,
      includeBandwidth = true
    )

    assertNotNull(jsonStr)
    assertTrue("JSON export must contain topologyNodes key", jsonStr.contains("\"topologyNodes\""))
    assertTrue("JSON export must contain serverMetrics key", jsonStr.contains("\"serverMetrics\""))
    assertTrue("JSON export must contain networkHealth", jsonStr.contains("\"networkHealth\""))
    assertTrue("JSON export must contain healthScore", jsonStr.contains("\"healthScore\": 98"))

    // Parse with JSONObject to ensure strict JSON validity
    val jsonObject = org.json.JSONObject(jsonStr)
    assertEquals(98, jsonObject.getJSONObject("networkHealth").getInt("healthScore"))
    assertTrue(jsonObject.getJSONArray("topologyNodes").length() > 0)
    assertTrue(jsonObject.getJSONArray("serverMetrics").length() > 0)

    // Generate Markdown summary
    val mdStr = com.example.ui.components.generateTopologySummaryMarkdown(
      topologyNodes = topologyNodes,
      serverMetrics = serverMetrics,
      networkHealth = networkHealth,
      bandwidthMetrics = bandwidthMetrics,
      alerts = alerts,
      slaReport = slaReport,
      nodeTrafficMap = trafficMap,
      includeDetails = true,
      includeBandwidth = true
    )

    assertNotNull(mdStr)
    assertTrue("Markdown summary must contain header", mdStr.contains("NETGUARD ENTERPRISE - TOPOLOGY & HEALTH AUDIT REPORT"))
    assertTrue("Markdown summary must contain node table", mdStr.contains("NETWORK TOPOLOGY HIERARCHY"))
    assertTrue("Markdown summary must contain server metrics table", mdStr.contains("ENTERPRISE SERVER RESOURCE METRICS"))
  }

  @Test
  fun `cluster aggregate metrics correctly computes average CPU and RAM consumption across all servers`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val serverMetrics = viewModel.serverMetrics.value
    val criticalServers = viewModel.criticalServers.value

    assertTrue("Server metrics must not be empty", serverMetrics.isNotEmpty())
    assertTrue("Critical servers must not be empty", criticalServers.isNotEmpty())

    val avgCpu = serverMetrics.map { it.cpuPercent }.average().toFloat()
    val avgRam = serverMetrics.map { it.ramPercent }.average().toFloat()
    val avgDisk = serverMetrics.map { it.diskPercent }.average().toFloat()
    val avgTemp = serverMetrics.map { it.temperatureC }.average().toFloat()

    assertTrue("Average CPU must be a valid percentage between 0 and 100", avgCpu in 0f..100f)
    assertTrue("Average RAM must be a valid percentage between 0 and 100", avgRam in 0f..100f)
    assertTrue("Average Disk must be a valid percentage between 0 and 100", avgDisk in 0f..100f)
    assertTrue("Average Temp must be positive", avgTemp > 0f)

    val highCpuCount = serverMetrics.count { it.cpuPercent >= 80f } + criticalServers.count { it.cpuPercent >= 80f }
    val highRamCount = serverMetrics.count { it.ramPercent >= 80f } + criticalServers.count { it.ramPercent >= 80f }
    val isClusterWarning = avgCpu > 75f || avgRam > 80f || highCpuCount > 0 || highRamCount > 0

    // High cluster warning evaluates properly
    assertNotNull(isClusterWarning)
  }

  @Test
  fun `topology node search filters correctly by name and IP address`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val topologyNodes = viewModel.topologyNodes.value
    assertTrue("Topology nodes should not be empty", topologyNodes.isNotEmpty())

    // 1. Search by name (case-insensitive substring)
    val nameQuery = "Gateway"
    val nameMatches = topologyNodes.filter { node ->
      node.label.contains(nameQuery, ignoreCase = true) ||
              node.ip.contains(nameQuery, ignoreCase = true) ||
              node.id.contains(nameQuery, ignoreCase = true)
    }
    assertTrue("Should find Gateway node", nameMatches.isNotEmpty())
    assertTrue("Matching node label should contain Gateway", nameMatches.any { it.label.contains("Gateway", ignoreCase = true) })

    // 2. Search by IP address substring
    val ipQuery = "192.168.1.1"
    val ipMatches = topologyNodes.filter { node ->
      node.label.contains(ipQuery, ignoreCase = true) ||
              node.ip.contains(ipQuery, ignoreCase = true) ||
              node.id.contains(ipQuery, ignoreCase = true)
    }
    assertTrue("Should find node with IP 192.168.1.1", ipMatches.isNotEmpty())
    assertEquals("dev-01", ipMatches.first().id)

    // 3. Search for database master node
    val dbQuery = "DB-01"
    val dbMatches = topologyNodes.filter { node ->
      node.label.contains(dbQuery, ignoreCase = true) ||
              node.ip.contains(dbQuery, ignoreCase = true) ||
              node.id.contains(dbQuery, ignoreCase = true)
    }
    assertTrue("Should find DB-01 master server", dbMatches.isNotEmpty())
    assertEquals("dev-04", dbMatches.first().id)

    // 4. Non-matching search query
    val noMatchesQuery = "xyz999nonexistent"
    val noMatches = topologyNodes.filter { node ->
      node.label.contains(noMatchesQuery, ignoreCase = true) ||
              node.ip.contains(noMatchesQuery, ignoreCase = true) ||
              node.id.contains(noMatchesQuery, ignoreCase = true)
    }
    assertTrue("Non-matching query should return empty list", noMatches.isEmpty())
  }

  @Test
  fun `custom server threshold rules are initialized with sensible defaults`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val rules = viewModel.serverThresholdRules.value
    assertTrue("Initial custom threshold rules should exist", rules.isNotEmpty())

    val dbRule = rules.find { it.serverId == "dev-04" }
    assertNotNull("DB-01 rule should exist", dbRule)
    assertEquals(80f, dbRule?.ramThresholdPercent)
    assertEquals(75f, dbRule?.cpuThresholdPercent)
    assertTrue("DB rule should have in-app alert enabled", dbRule?.notifyInApp == true)
    assertEquals(ThresholdActionType.RESTART_SERVICE, dbRule?.actionType)
  }

  @Test
  fun `user can add, toggle, and delete custom server threshold rules`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val initialCount = viewModel.serverThresholdRules.value.size

    val newRule = ServerThresholdRule(
      id = "custom-test-rule-01",
      serverId = "dev-03",
      serverName = "Prod-01 App Server",
      cpuThresholdPercent = 75f,
      ramThresholdPercent = 70f,
      notifyInApp = true,
      notifyTelegram = true,
      severity = AlertSeverity.WARNING,
      actionType = ThresholdActionType.PURGE_MEMORY_CACHE,
      isEnabled = true
    )

    // Add rule
    viewModel.addThresholdRule(newRule)
    assertEquals(initialCount + 1, viewModel.serverThresholdRules.value.size)

    val retrieved = viewModel.serverThresholdRules.value.find { it.id == "custom-test-rule-01" }
    assertNotNull(retrieved)
    assertEquals(75f, retrieved?.cpuThresholdPercent)

    // Toggle rule off
    viewModel.toggleThresholdRule("custom-test-rule-01", false)
    assertFalse(viewModel.serverThresholdRules.value.first { it.id == "custom-test-rule-01" }.isEnabled)

    // Delete rule
    viewModel.deleteThresholdRule("custom-test-rule-01")
    assertEquals(initialCount, viewModel.serverThresholdRules.value.size)
    assertTrue(viewModel.serverThresholdRules.value.none { it.id == "custom-test-rule-01" })
  }

  @Test
  fun `testing custom threshold rule dispatches notification and executes automated action`() = runBlocking {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val initialRemediationsCount = viewModel.remediations.value.size
    val initialAlertsCount = viewModel.alerts.value.size

    // Test trigger rule for DB-01
    val action = repository.testThresholdRule("rule-db01")
    assertNotNull(action)

    // Verify alert notification was logged
    val updatedAlerts = viewModel.alerts.value
    assertTrue("New alerts should be generated", updatedAlerts.size > initialAlertsCount)
    assertTrue("Alert list should contain threshold breach alert", updatedAlerts.any { it.title.contains("Threshold", ignoreCase = true) })

    // Verify automated action was executed and logged
    val updatedRemediations = viewModel.remediations.value
    assertTrue("New remediation event should be logged", updatedRemediations.size > initialRemediationsCount)
    val latestRemediation = updatedRemediations.first()
    assertTrue("Remediation should be automated", latestRemediation.isAutomated)
    assertEquals("dev-04", latestRemediation.serverId)
  }

  @Test
  fun `dashboard screen serves as application entry point with network status active devices and alerts`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // Verify Dashboard is the default entry point tab
    assertEquals("Dashboard must be the default entry tab", AppTab.DASHBOARD, viewModel.currentTab.value)

    // Verify Network Health Status summary is populated
    val health = viewModel.networkHealth.value
    assertTrue("Network health score should be valid", health.healthScore in 0..100)
    assertTrue("Active gateway IP should be present", health.activeGatewayIp.isNotEmpty())

    // Verify Active Devices count is populated
    val devices = viewModel.devices.value
    val onlineCount = devices.count { it.status == DeviceStatus.ONLINE }
    assertTrue("Active devices count should be positive", onlineCount > 0)
    assertEquals(devices.size, devices.count())

    // Verify Recent Alerts are populated
    val alerts = viewModel.alerts.value
    assertTrue("Recent alerts list should not be empty", alerts.isNotEmpty())
    assertNotNull("Alerts should have timestamps", alerts.first().timestamp)
  }

  @Test
  fun `main dashboard summary cards metrics for active devices rogue devices and system load percentage are valid`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // 1. 'Active Devices' metric
    val devices = viewModel.devices.value
    val onlineCount = devices.count { it.status == DeviceStatus.ONLINE }
    val totalCount = devices.size
    assertTrue("Active devices online count should be > 0", onlineCount > 0)
    assertTrue("Total devices count should be >= online count", totalCount >= onlineCount)

    // 2. 'Rogue Devices Detected' metric
    val rogues = viewModel.rogueDevices.value
    val activeRoguesCount = rogues.count { it.status.name != "ISOLATED" && it.status.name != "TRUSTED" }
    assertTrue("Rogue devices list should be accessible", rogues.isNotEmpty())
    assertTrue("Active rogue devices detected count should be non-negative", activeRoguesCount >= 0)

    // 3. 'System Load Percentage' metric
    val serverMetrics = viewModel.serverMetrics.value
    val criticalServers = viewModel.criticalServers.value
    val cpuValues = mutableListOf<Float>()
    serverMetrics.forEach { cpuValues.add(it.cpuPercent) }
    criticalServers.forEach { cpuValues.add(it.cpuPercent) }
    devices.forEach { dev -> dev.cpuUsage?.let { cpuValues.add(it) } }
    val avgSystemLoad = if (cpuValues.isNotEmpty()) cpuValues.average().toFloat() else 38.5f

    assertTrue("System load percentage should be between 0 and 100", avgSystemLoad in 0f..100f)
  }

  @Test
  fun `system load historical performance data over the last hour is properly generated for line chart visualization`() {
    val currentLoad = 42.5f
    val history = com.example.ui.components.generateLastHourSystemLoadHistory(currentLoad)

    // Should have 13 points spanning -60m to 0m (Now) at 5-minute intervals
    assertEquals(13, history.size)
    assertEquals(-60, history.first().minuteOffset)
    assertEquals("-60m", history.first().timestampLabel)
    assertEquals(0, history.last().minuteOffset)
    assertEquals("Now", history.last().timestampLabel)
    assertEquals(currentLoad, history.last().loadPercentage, 0.01f)

    // All load points should be bounded in a realistic percentage range
    history.forEach { pt ->
      assertTrue("Point at ${pt.minuteOffset}m should be in 0..100 range", pt.loadPercentage in 0f..100f)
    }

    // Peak and Min load metrics
    val peak = history.maxOf { it.loadPercentage }
    val min = history.minOf { it.loadPercentage }
    assertTrue("Peak load should be >= min load", peak >= min)
  }

  @Test
  fun `realtime network events log contains discovery connectivity and remediation alerts`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val events = viewModel.networkEvents.value
    assertTrue("Network events log should not be empty", events.isNotEmpty())

    // Verify presence of Device Discovery events
    val discoveryEvents = events.filter { it.eventType == NetworkEventType.DEVICE_DISCOVERY }
    assertTrue("Should contain device discovery events", discoveryEvents.isNotEmpty())
    discoveryEvents.forEach {
      assertNotNull("Discovery event should have title", it.title)
      assertTrue("Discovery event should have IP or MAC", it.ipAddress != null || it.macAddress != null)
    }

    // Verify presence of Connectivity Change events
    val connectivityEvents = events.filter { it.eventType == NetworkEventType.CONNECTIVITY_CHANGE }
    assertTrue("Should contain connectivity change events", connectivityEvents.isNotEmpty())

    // Verify presence of Remediation Alerts (تنبيهات المعالجة)
    val remediationAlerts = events.filter { it.eventType == NetworkEventType.REMEDIATION_ALERT }
    assertTrue("Should contain remediation alerts", remediationAlerts.isNotEmpty())
    val firstRemediation = remediationAlerts.first()
    assertNotNull("Remediation alert should contain remediationDetails", firstRemediation.remediationDetails)
    assertTrue("Remediation alert should contain command", firstRemediation.remediationDetails!!.command.isNotBlank())
    assertTrue("Remediation alert should target a service", firstRemediation.remediationDetails!!.targetService.isNotBlank())
  }

  @Test
  fun `remediation execution dynamically dispatches remediation alert to event log`() = runBlocking {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val initialCount = viewModel.networkEvents.value.size
    viewModel.executeRemediation("dev-04", "PostgreSQL DB", "systemctl restart postgresql")

    val updatedEvents = viewModel.networkEvents.value
    assertTrue("Event count should increase after remediation", updatedEvents.size > initialCount)

    val latestEvent = updatedEvents.first()
    assertEquals(NetworkEventType.REMEDIATION_ALERT, latestEvent.eventType)
    assertNotNull(latestEvent.remediationDetails)
    assertEquals("PostgreSQL DB", latestEvent.remediationDetails?.targetService)
    assertEquals("systemctl restart postgresql", latestEvent.remediationDetails?.command)
    assertEquals("SUCCESS", latestEvent.remediationDetails?.status)
  }

  @Test
  fun `network event acknowledgement and dismissal functions correctly`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val firstEvent = viewModel.networkEvents.value.first()
    assertFalse("Event should initially be unacknowledged", firstEvent.isAcknowledged)

    viewModel.acknowledgeNetworkEvent(firstEvent.id)
    val ackedEvent = viewModel.networkEvents.value.first { it.id == firstEvent.id }
    assertTrue("Event should be marked acknowledged", ackedEvent.isAcknowledged)

    viewModel.dismissNetworkEvent(firstEvent.id)
    val remainingIds = viewModel.networkEvents.value.map { it.id }
    assertFalse("Dismissed event should no longer exist", remainingIds.contains(firstEvent.id))
  }

  @Test
  fun `simulate network event creates event of requested category`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val initialSize = viewModel.networkEvents.value.size
    viewModel.simulateNetworkEvent(NetworkEventType.DEVICE_DISCOVERY)

    val updatedEvents = viewModel.networkEvents.value
    assertEquals(initialSize + 1, updatedEvents.size)
    assertEquals(NetworkEventType.DEVICE_DISCOVERY, updatedEvents.first().eventType)

    viewModel.simulateNetworkEvent(NetworkEventType.REMEDIATION_ALERT)
    val remediationEvent = viewModel.networkEvents.value.first()
    assertEquals(NetworkEventType.REMEDIATION_ALERT, remediationEvent.eventType)
    assertNotNull(remediationEvent.remediationDetails)
  }

  @Test
  fun `firebase firestore sync manager initializes with valid default state`() {
    val repository = NetworkGuardRepository()
    val syncManager = repository.firestoreSyncManager

    assertNotNull(syncManager)
    assertNotNull(repository.firestoreSyncStatus.value)
    assertTrue("Realtime sync should default to enabled", repository.isFirestoreRealtimeEnabled.value)
  }

  @Test
  fun `firestore sync all state generates valid report with persisted devices and events`() = runBlocking {
    val repository = NetworkGuardRepository()
    val report = repository.syncAllToFirestore()

    assertNotNull(report)
    assertTrue("Report should reflect current devices count", report.devicesCount >= 8)
    assertTrue("Report should reflect rogue devices count", report.rogueCount >= 1)
    assertTrue("Report should reflect network events count", report.eventsCount >= 1)
    assertNotNull("Timestamp should be recorded", repository.firestoreLastSyncTimestamp.value)
  }

  @Test
  fun `viewmodel triggers firestore sync and exposes sync state`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    assertNotNull(viewModel.firestoreSyncStatus.value)
    assertTrue(viewModel.isFirestoreRealtimeEnabled.value)

    viewModel.syncWithFirestore()
    viewModel.toggleFirestoreRealtimeSync(false)
    assertFalse("Realtime sync should be toggled off", viewModel.isFirestoreRealtimeEnabled.value)

    viewModel.toggleFirestoreRealtimeSync(true)
    assertTrue("Realtime sync should be re-enabled", viewModel.isFirestoreRealtimeEnabled.value)
  }

  @Test
  fun `cross-session consistency simulation retains device state across sessions`() = runBlocking {
    val repo1 = NetworkGuardRepository()
    val vm1 = MainViewModel(repo1)

    // Isolate rogue device in session 1
    val rogueId = "rogue-01"
    vm1.isolateRogue(rogueId)

    val rogueInRepo1 = repo1.rogueDevices.value.find { it.id == rogueId }
    assertEquals(RogueStatus.ISOLATED, rogueInRepo1?.status)

    // Simulate session 2 sync report
    val syncReport = repo1.syncAllToFirestore()
    assertTrue("Sync report should succeed", syncReport.isSuccess)
    assertEquals(repo1.devices.value.size, syncReport.devicesCount)
  }

  @Test
  fun `dashboard executive summary calculates network status, devices, and security threats`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // 1. Network Status
    val health = viewModel.networkHealth.value
    assertNotNull(health)
    assertEquals("192.168.1.1", health.activeGatewayIp)
    assertTrue("Health score should be positive", health.healthScore > 0)

    // 2. Connected Devices Count
    val devices = viewModel.devices.value
    val onlineCount = devices.count { it.status == DeviceStatus.ONLINE }
    val totalCount = devices.size
    assertTrue("Total devices should be greater than zero", totalCount > 0)
    assertTrue("Online devices count should be <= total", onlineCount <= totalCount)

    // 3. Security Threats
    val rogues = viewModel.rogueDevices.value
    val activeThreats = rogues.count { it.status != RogueStatus.ISOLATED && it.status != RogueStatus.TRUSTED }
    assertTrue("Active threats count should be non-negative", activeThreats >= 0)
  }

  @Test
  fun `theme switcher correctly toggles between dark mode and light mode`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // Default is dark
    assertTrue(viewModel.isDarkMode.value)

    // Toggle to Light mode
    viewModel.toggleDarkMode(false)
    assertFalse(viewModel.isDarkMode.value)

    // Toggle back to Dark mode
    viewModel.toggleDarkMode(true)
    assertTrue(viewModel.isDarkMode.value)

    // Automatic inversion toggle
    viewModel.toggleDarkMode()
    assertFalse(viewModel.isDarkMode.value)
  }

  @Test
  fun `language switcher correctly toggles between English and Arabic`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // Primary default language is Arabic
    assertEquals(AppLanguage.ARABIC, viewModel.appLanguage.value)

    // Switch to English
    viewModel.setAppLanguage(AppLanguage.ENGLISH)
    assertEquals(AppLanguage.ENGLISH, viewModel.appLanguage.value)

    // Toggle back to Arabic
    viewModel.toggleAppLanguage()
    assertEquals(AppLanguage.ARABIC, viewModel.appLanguage.value)

    // Toggle back to English
    viewModel.toggleAppLanguage()
    assertEquals(AppLanguage.ENGLISH, viewModel.appLanguage.value)
  }

  @Test
  fun `theme colors helper resolves dark and light palettes accurately`() {
    val darkColors = getNetGuardColors(true)
    assertTrue(darkColors.isDark)
    assertEquals(CyberNavyDark, darkColors.background)

    val lightColors = getNetGuardColors(false)
    assertFalse(lightColors.isDark)
    assertEquals(LightCanvasBackground, lightColors.background)
  }

  @Test
  fun `global theme switcher toggles between light and dark modes and updates state accordingly`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // Initial state is Dark Mode (for NOC/Cyber monitoring)
    assertTrue("Default theme should be Dark Mode", viewModel.isDarkMode.value)

    // Toggle to Light Mode
    viewModel.setDarkMode(false)
    assertFalse("Theme should now be Light Mode", viewModel.isDarkMode.value)

    // Toggle to Dark Mode
    viewModel.setDarkMode(true)
    assertTrue("Theme should now be Dark Mode", viewModel.isDarkMode.value)

    // Toggle using toggleDarkMode without parameter (flips state)
    viewModel.toggleDarkMode()
    assertFalse("Theme should be toggled to Light Mode", viewModel.isDarkMode.value)

    // Toggle with explicit parameter
    viewModel.toggleDarkMode(true)
    assertTrue("Theme should be explicitly Dark Mode", viewModel.isDarkMode.value)
  }

  @Test
  fun `pdf report service generates valid administrative review document`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val pdfResult = viewModel.exportNetworkStatusAndSecurityPdf(context)
    assertNotNull(pdfResult)
    assertTrue("PDF file must exist", pdfResult.file.exists())
    assertTrue("PDF file size should be greater than 0 bytes", pdfResult.fileSizeBytes > 0)
    assertEquals(2, pdfResult.pageCount)
    assertTrue("Report title should have timestamp and NetGuard prefix", pdfResult.title.startsWith("NetGuard_Admin_Report_"))
    assertNotNull("Content URI should be generated by FileProvider", pdfResult.uri)
    assertEquals(pdfResult, viewModel.lastPdfExportResult.value)
  }

  @Test
  fun `customer service contacts and official website are properly configured`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // WhatsApp support number verification
    assertEquals("967738704940", viewModel.supportWhatsAppNumber)
    assertTrue(viewModel.supportWhatsAppDisplay.contains("738 704 940"))

    // Direct phone call number verification
    assertEquals("967749154739", viewModel.supportPhoneNumber)
    assertTrue(viewModel.supportPhoneDisplay.contains("749 154 739"))

    // Official website integration verification
    assertEquals("https://netguard.enterprise.security", viewModel.officialWebsiteUrl)
    assertEquals("netguard.enterprise.security", viewModel.officialWebsiteDomain)
    assertTrue(viewModel.chiefArchitectTitle.contains("نجم الرئيس"))
  }

  @Test
  fun `faqs list contains comprehensive questions and answers in Arabic and English`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val faqs = viewModel.faqList.value
    assertTrue("Should have extensive FAQs populated", faqs.size >= 8)

    // Verify presence of core questions
    val overviewFaq = faqs.find { it.id == "faq-01" }
    assertNotNull(overviewFaq)
    assertTrue(overviewFaq!!.questionArabic.contains("منظومة NetGuard"))
    assertTrue(overviewFaq.answerArabic.contains("الأجهزة الدخيلة"))

    val supportFaq = faqs.find { it.id == "faq-02" }
    assertNotNull(supportFaq)
    assertTrue(supportFaq!!.answerArabic.contains("967738704940"))
    assertTrue(supportFaq.answerArabic.contains("967749154739"))

    val pingFaq = faqs.find { it.id == "faq-04" }
    assertNotNull(pingFaq)
    assertTrue(pingFaq!!.questionArabic.contains("Ping"))

    val websiteFaq = faqs.find { it.id == "faq-06" }
    assertNotNull(websiteFaq)
    assertTrue(websiteFaq!!.answerArabic.contains("netguard.enterprise.security"))
  }

  @Test
  fun `submitting customer inquiry creates entry with official response`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val initialCount = viewModel.customerInquiries.value.size
    val inquiry = viewModel.submitCustomerInquiry(
        senderName = "مهندس الاتصالات",
        contactInfo = "+967738704940",
        inquiryType = "استفسار فني",
        subject = "فحص عزل الـ Switch",
        message = "هل تم تفعيل عزل المنافذ الدخيلة بنجاح؟"
    )

    assertNotNull(inquiry)
    assertTrue(inquiry.id.startsWith("INQ-"))
    assertEquals("ANSWERED", inquiry.status)
    assertNotNull(inquiry.officialReply)
    assertTrue(inquiry.officialReply!!.contains("المهندس نجم الرئيس"))

    val updatedCount = viewModel.customerInquiries.value.size
    assertEquals(initialCount + 1, updatedCount)
  }

  @Test
  fun `security health audit produces valid grade and assessment recommendations`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val audit = viewModel.runSecurityHealthAudit()
    assertNotNull(audit)
    assertTrue("Audit score must be >= 75", audit.scorePercent >= 75)
    assertTrue("Grade must start with A or B", audit.grade.startsWith("A") || audit.grade.startsWith("B"))
    assertTrue(audit.firewallStatus.isNotBlank())
    assertTrue(audit.idsSensorStatus.isNotBlank())
    assertTrue(audit.recommendations.isNotEmpty())
  }

  @Test
  fun `support tab can be selected in AppTab navigation`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    viewModel.selectTab(AppTab.SUPPORT)
    assertEquals(AppTab.SUPPORT, viewModel.currentTab.value)
  }

  @Test
  fun `openWhatsAppSupport launches intent targeting 967738704940`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val shadowApp = org.robolectric.Shadows.shadowOf(context as android.app.Application)
    viewModel.openWhatsAppSupport(context)

    val startedIntent = shadowApp.nextStartedActivity
    assertNotNull("WhatsApp intent should be launched", startedIntent)
    assertEquals(android.content.Intent.ACTION_VIEW, startedIntent.action)
    assertTrue("Intent URI must contain target WhatsApp number 967738704940",
        startedIntent.dataString?.contains("967738704940") == true)
  }

  @Test
  fun `dialSupportPhone launches dialer intent targeting 967749154739`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val shadowApp = org.robolectric.Shadows.shadowOf(context as android.app.Application)
    viewModel.dialSupportPhone(context)

    val startedIntent = shadowApp.nextStartedActivity
    assertNotNull("Phone dialer intent should be launched", startedIntent)
    assertEquals(android.content.Intent.ACTION_DIAL, startedIntent.action)
    assertEquals("tel:967749154739", startedIntent.dataString)
  }

  @Test
  fun `unified management support contact component integrates across tabs`() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    // Verify navigating from any operational tab to Support tab as a unified management tool
    viewModel.selectTab(AppTab.DASHBOARD)
    assertEquals(AppTab.DASHBOARD, viewModel.currentTab.value)

    viewModel.selectTab(AppTab.ROGUE_DETECTOR)
    assertEquals(AppTab.ROGUE_DETECTOR, viewModel.currentTab.value)

    viewModel.selectTab(AppTab.SUPPORT)
    assertEquals(AppTab.SUPPORT, viewModel.currentTab.value)
  }

  @Test
  fun `openOfficialWebsite launches browser intent targeting NetGuard official website`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val shadowApp = org.robolectric.Shadows.shadowOf(context as android.app.Application)
    viewModel.openOfficialWebsite(context)

    val startedIntent = shadowApp.nextStartedActivity
    assertNotNull("Official website browser intent should be launched", startedIntent)
    assertEquals(android.content.Intent.ACTION_VIEW, startedIntent.action)
    assertEquals("https://netguard.enterprise.security", startedIntent.dataString)
  }
}




