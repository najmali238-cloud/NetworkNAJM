package com.example

import com.example.data.repository.NetworkGuardRepository
import com.example.ui.MainViewModel
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying repository data flows and Recharts telemetry trends.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun rechartsTrendPoints_initializeAndSimulateSurge() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val points = viewModel.rechartsTrendPoints.value
    assertTrue("Recharts points buffer should not be empty", points.isNotEmpty())

    val firstPoint = points.first()
    assertTrue("Download throughput should be positive", firstPoint.downloadMbps > 0f)
    assertTrue("Server CPU load should be positive", firstPoint.serverCpuPercent > 0f)

    // Simulate traffic surge
    viewModel.simulateRechartsTrafficSurge()
    val updatedPoints = viewModel.rechartsTrendPoints.value
    val latest = updatedPoints.last()
    assertTrue("Latest point after burst should be marked as spike", latest.isSpike)
    assertTrue("Bursted download throughput should exceed 800 Mbps", latest.downloadMbps >= 800f)
  }

  @Test
  fun rogueRemediation_isolateAndSetupMonitoring() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val rogues = viewModel.rogueDevices.value
    assertTrue("Rogue devices list should have detected items", rogues.isNotEmpty())

    val targetRogue = rogues.first()
    // Trigger isolation
    viewModel.isolateRogue(targetRogue.id)

    val isolatedRogue = viewModel.rogueDevices.value.find { it.id == targetRogue.id }
    assertNotNull(isolatedRogue)
    assertEquals(com.example.data.model.RogueStatus.ISOLATED, isolatedRogue?.status)

    // Trigger restore / unblock
    viewModel.restoreRogue(targetRogue.id)
    val restoredRogue = viewModel.rogueDevices.value.find { it.id == targetRogue.id }
    assertEquals(com.example.data.model.RogueStatus.INVESTIGATING, restoredRogue?.status)

    // Save Device Monitoring Config (اعداد مراقبة الجهاز)
    val config = com.example.data.model.DeviceMonitoringConfig(
        deviceId = targetRogue.id,
        deviceName = "Prober Test Node",
        ip = targetRogue.ip,
        mac = targetRogue.mac,
        monitoringIntervalSeconds = 5,
        autoIsolateOnThreat = true,
        deepPacketInspection = true,
        arpSpoofDefense = true,
        portScanWatchdog = true,
        packetRateThreshold = 400
    )
    viewModel.updateDeviceMonitoringConfig(config)

    val savedConfig = viewModel.deviceMonitoringConfigs.value[targetRogue.id]
    assertNotNull(savedConfig)
    assertEquals(5, savedConfig?.monitoringIntervalSeconds)
    assertTrue(savedConfig?.autoIsolateOnThreat == true)
    assertTrue(savedConfig?.deepPacketInspection == true)
  }

  @Test
  fun deviceThresholds_updateAndEvaluate() {
    val repository = com.example.data.repository.NetworkGuardRepository()
    val viewModel = com.example.ui.MainViewModel(repository)

    val currentThresholds = viewModel.deviceThresholds.value
    assertTrue("Default thresholds should be seeded", currentThresholds.isNotEmpty())

    // Update threshold for dev-01
    val customThreshold = com.example.data.model.DeviceThreshold(
        deviceId = "dev-01",
        deviceName = "Core Gateway Router",
        ipAddress = "192.168.1.1",
        maxLatencyMs = 12L,
        maxPacketLossPercent = 1.0f,
        maxCpuPercent = 70f,
        maxRamPercent = 65f,
        alertOnBreach = true,
        autoIsolateOnBreach = false
    )
    viewModel.updateDeviceThreshold(customThreshold)

    val updated = viewModel.deviceThresholds.value.find { it.deviceId == "dev-01" }
    assertNotNull(updated)
    assertEquals(12L, updated?.maxLatencyMs)
    assertEquals(1.0f, updated?.maxPacketLossPercent)
    assertEquals(70f, updated?.maxCpuPercent)

    // Remove threshold
    viewModel.removeDeviceThreshold("dev-06")
    val dev06 = viewModel.deviceThresholds.value.find { it.deviceId == "dev-06" }
    assertNull(dev06)

    // Reset thresholds
    viewModel.resetDeviceThresholds()
    val resetDev06 = viewModel.deviceThresholds.value.find { it.deviceId == "dev-06" }
    assertNotNull(resetDev06)
  }

  @Test
  fun networkDiagnostic_pingTargetSession() {
    val repository = com.example.data.repository.NetworkGuardRepository()
    val viewModel = com.example.ui.MainViewModel(repository)

    // Start ping target
    viewModel.pingTarget("192.168.1.1", count = 3)
    val session = viewModel.diagnosticSession.value
    assertEquals("192.168.1.1", session.targetIp)
    assertTrue(session.isRunning)

    // Stop ping
    viewModel.stopPing()
    assertFalse(viewModel.diagnosticSession.value.isRunning)

    // Clear session
    viewModel.clearDiagnosticSession()
    assertEquals("", viewModel.diagnosticSession.value.targetIp)
    assertTrue(viewModel.diagnosticSession.value.results.isEmpty())
  }

  @Test
  fun pushNotificationService_trafficThresholdAlerts() {
    val repository = com.example.data.repository.NetworkGuardRepository()
    val viewModel = com.example.ui.MainViewModel(repository)

    // Verify initial config
    val initialConfig = viewModel.trafficNotificationConfig.value
    assertTrue("Traffic notification service should be enabled by default", initialConfig.isEnabled)
    assertEquals(600f, initialConfig.bandwidthThresholdMbps)

    // Adjust threshold to 500 Mbps
    viewModel.updateTrafficThreshold(500f)
    assertEquals(500f, viewModel.trafficNotificationConfig.value.bandwidthThresholdMbps)

    val historyBefore = viewModel.trafficNotificationHistory.value.size

    // Trigger test push alert
    viewModel.triggerTrafficPushNotificationTest()
    val historyAfter = viewModel.trafficNotificationHistory.value.size
    assertTrue("History should record triggered push notification", historyAfter > historyBefore)
    val latest = viewModel.trafficNotificationHistory.value.first()
    assertTrue(latest.trafficMbps >= 500f)

    // Simulate traffic surge (845 Mbps) which triggers push notification
    viewModel.simulateRechartsTrafficSurge()
    val afterSurgeHistory = viewModel.trafficNotificationHistory.value.size
    assertTrue("Surge should record another push alert", afterSurgeHistory >= historyAfter)

    // Toggle service pause
    viewModel.toggleTrafficNotifications(false)
    assertFalse(viewModel.trafficNotificationConfig.value.isEnabled)

    // Clear history
    viewModel.clearTrafficNotificationHistory()
    assertTrue(viewModel.trafficNotificationHistory.value.isEmpty())
  }

  @Test
  fun internetCardCredit_dataModelAndBalanceCalculations() {
    val card = com.example.data.model.InternetCardCredit(
        cardId = "CARD-TEST-01",
        networkId = "net-corp-01",
        cardSerialNumber = "NET-1234-5678-2026",
        holderName = "Test User",
        totalCreditGB = 100f,
        usedCreditGB = 30f,
        remainingCreditGB = 70f,
        tier = com.example.data.model.InternetCardTier.GOLD
    )

    assertEquals(70f, card.remainingCreditGB)
    assertEquals(100f, card.totalCreditGB)
    assertEquals(30f, card.usedCreditGB)
    assertEquals(30f, card.consumptionPercent, 0.01f)
    assertEquals(0.70f, card.remainingFraction, 0.01f)
    assertFalse("70% remaining should not be flagged as low balance", card.isLowBalance)

    val depletedCard = card.copy(
        usedCreditGB = 95f,
        remainingCreditGB = 5f
    )
    assertTrue("5% remaining should be flagged as low balance", depletedCard.isLowBalance)
  }

  @Test
  fun voucherValidator_evaluatesCodesCorrectly() {
    // Test preset vouchers
    val (validGold, goldAmounts) = com.example.data.model.VoucherValidator.evaluate("VCH-GOLD-50GB")
    assertTrue(validGold)
    assertEquals(50f, goldAmounts.first)
    assertEquals(5f, goldAmounts.second) // 5GB bonus

    val (validTurbo, turboAmounts) = com.example.data.model.VoucherValidator.evaluate("vch-turbo-100gb")
    assertTrue("Should be case-insensitive", validTurbo)
    assertEquals(100f, turboAmounts.first)
    assertEquals(15f, turboAmounts.second) // 15GB bonus

    // Test blank or invalid codes
    val (invalidEmpty, _) = com.example.data.model.VoucherValidator.evaluate("")
    assertFalse(invalidEmpty)

    val (invalidShort, _) = com.example.data.model.VoucherValidator.evaluate("123")
    assertFalse(invalidShort)
  }

  @Test
  fun viewModel_redeemVoucherToCardCreditSuccessfully() {
    val repository = NetworkGuardRepository()
    val viewModel = MainViewModel(repository)

    val networkId = "net-corp-01"
    val initialCard = viewModel.internetCardCredits.value[networkId]
    assertNotNull(initialCard)
    val initialBalance = initialCard!!.remainingCreditGB

    // Redeem valid voucher
    val result = viewModel.redeemCardVoucher(networkId, "VCH-GOLD-50GB")
    assertTrue("Result should be success", result is com.example.data.model.RedemptionResult.Success)

    val success = result as com.example.data.model.RedemptionResult.Success
    assertEquals(50f, success.creditedGB)
    assertEquals(5f, success.bonusGB)

    val updatedCard = viewModel.internetCardCredits.value[networkId]
    assertNotNull(updatedCard)
    assertEquals(initialBalance + 55f, updatedCard!!.remainingCreditGB, 0.01f)
    assertTrue(updatedCard.redemptionHistory.isNotEmpty())
    assertEquals("VCH-GOLD-50GB", updatedCard.redemptionHistory.first().voucherCode)
  }
}


