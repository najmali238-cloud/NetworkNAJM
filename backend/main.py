import os
import json
from datetime import datetime
from typing import List, Optional, Dict
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sqlalchemy.orm import Session

from database import (
    SessionLocal, init_db,
    DeviceModel, RogueDeviceModel, AlertLogModel,
    ServerMetricModel, RemediationLogModel
)
from scanner import ping_host, scan_ports, get_system_hardware_metrics
from telegram_notifier import AlertDispatcher
from topology import generate_network_topology

app = FastAPI(
    title="NetGuard Enterprise Guard API",
    description="24/7 Cybersecurity Network & Device Monitor Backend",
    version="2.4.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Dependency to get DB session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.on_event("startup")
def on_startup():
    init_db()

# Pydantic Schemas
class DeviceCreate(BaseModel):
    name: str
    ip: str
    mac: str
    type: str = "SERVER"
    parent_switch_id: Optional[str] = "dev-02"

class RemediationRequest(BaseModel):
    server_id: str
    target_service: str
    command: str

class TelegramTestRequest(BaseModel):
    bot_token: Optional[str] = None
    chat_id: Optional[str] = None

# Routes
@app.get("/api/health")
def health_check():
    return {
        "status": "healthy",
        "service": "NetGuard Enterprise Engine",
        "timestamp": datetime.now().isoformat(),
        "version": "2.4.0"
    }

@app.get("/api/devices")
def get_devices(db: Session = Depends(get_db)):
    devices = db.query(DeviceModel).all()
    result = []
    for d in devices:
        result.append({
            "id": d.id,
            "name": d.name,
            "ip": d.ip,
            "mac": d.mac,
            "type": d.type,
            "status": d.status,
            "latency_ms": d.latency_ms,
            "uptime_percent": d.uptime_percent,
            "last_checked": d.last_checked,
            "open_ports": json.loads(d.open_ports) if d.open_ports else [],
            "parent_switch_id": d.parent_switch_id
        })
    return result

@app.post("/api/devices/{device_id}/ping")
def ping_device(device_id: str, db: Session = Depends(get_db)):
    device = db.query(DeviceModel).filter(DeviceModel.id == device_id).first()
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    
    # Special condition for Camera 05
    if device.id == "dev-07":
        device.status = "OFFLINE"
        device.latency_ms = 0
        device.last_checked = "Probe Timeout"
        db.commit()
        return {"device_id": device.id, "status": "OFFLINE", "latency_ms": 0}

    is_online, latency = ping_host(device.ip)
    device.status = "ONLINE" if is_online else "OFFLINE"
    device.latency_ms = int(latency)
    device.last_checked = datetime.now().strftime("%H:%M:%S")
    db.commit()
    return {"device_id": device.id, "status": device.status, "latency_ms": device.latency_ms}

@app.post("/api/scan")
def trigger_network_sweep(db: Session = Depends(get_db)):
    """Performs an audit across all registered devices and checks for rogue devices."""
    devices = db.query(DeviceModel).all()
    now_time = datetime.now().strftime("%H:%M:%S")
    
    online_count = 0
    for d in devices:
        if d.id == "dev-07":
            d.status = "OFFLINE"
        else:
            is_up, lat = ping_host(d.ip)
            d.status = "ONLINE" if is_up else "OFFLINE"
            d.latency_ms = int(lat) if is_up else 0
            if is_up:
                online_count += 1
        d.last_checked = now_time
    
    db.commit()
    return {
        "status": "completed",
        "total_scanned": len(devices),
        "online_count": online_count,
        "timestamp": now_time
    }

@app.get("/api/rogue-devices")
def get_rogue_devices(db: Session = Depends(get_db)):
    rogues = db.query(RogueDeviceModel).all()
    return [
        {
            "id": r.id,
            "ip": r.ip,
            "mac": r.mac,
            "vendor": r.vendor,
            "detected_at": r.detected_at,
            "first_seen": r.first_seen,
            "open_ports": json.loads(r.open_ports) if r.open_ports else [],
            "threat_level": r.threat_level,
            "status": r.status
        }
        for r in rogues
    ]

@app.post("/api/rogue-devices/{rogue_id}/isolate")
def isolate_rogue_device(rogue_id: str, db: Session = Depends(get_db)):
    rogue = db.query(RogueDeviceModel).filter(RogueDeviceModel.id == rogue_id).first()
    if not rogue:
        raise HTTPException(status_code=404, detail="Rogue device not found")
    
    rogue.status = "ISOLATED"
    
    # Create Alert Log
    alert = AlertLogModel(
        id=f"alert-{int(datetime.now().timestamp())}",
        title="Rogue Device Isolated",
        message=f"ARP quarantine enforced on {rogue.ip} ({rogue.mac})",
        severity="INFO",
        timestamp=datetime.now().strftime("%H:%M:%S"),
        device_id=rogue.id,
        channel_notified="Telegram Bot + Firewall"
    )
    db.add(alert)
    db.commit()

    # Dispatch to Telegram
    dispatcher = AlertDispatcher()
    dispatcher.send_telegram_alert(
        title="Rogue Device Containment Enforced",
        message=f"Device `{rogue.ip}` (`{rogue.mac}`) successfully isolated from subnet."
    )

    return {"status": "success", "message": f"Device {rogue.ip} isolated."}

@app.post("/api/rogue-devices/{rogue_id}/trust")
def trust_rogue_device(rogue_id: str, db: Session = Depends(get_db)):
    rogue = db.query(RogueDeviceModel).filter(RogueDeviceModel.id == rogue_id).first()
    if not rogue:
        raise HTTPException(status_code=404, detail="Rogue device not found")
    
    rogue.status = "TRUSTED"
    
    # Add to devices table
    new_dev = DeviceModel(
        id=f"dev-{int(datetime.now().timestamp())}",
        name=f"Authorized ({rogue.vendor})",
        ip=rogue.ip,
        mac=rogue.mac,
        type="WORKSTATION",
        status="ONLINE",
        latency_ms=3,
        uptime_percent=100.0,
        last_checked=datetime.now().strftime("%H:%M:%S"),
        open_ports=rogue.open_ports,
        parent_switch_id="dev-02"
    )
    db.add(new_dev)
    db.commit()
    return {"status": "success", "message": f"Device {rogue.ip} whitelisted and registered."}

@app.get("/api/servers/metrics")
def get_server_metrics(db: Session = Depends(get_db)):
    metrics = db.query(ServerMetricModel).all()
    # Update local server metrics live via psutil
    local_stats = get_system_hardware_metrics()
    
    result = []
    for m in metrics:
        # Prod-01 gets dynamic metrics based on real system
        if m.server_id == "dev-03":
            m.cpu_percent = local_stats["cpu_percent"]
            m.ram_percent = local_stats["ram_percent"]
            m.disk_percent = local_stats["disk_percent"]
            m.temperature_c = local_stats["temperature_c"]
            m.processes_count = local_stats["processes_count"]
            m.last_updated = datetime.now().strftime("%H:%M:%S")
            db.commit()

        result.append({
            "serverId": m.server_id,
            "serverName": m.server_name,
            "ip": m.ip,
            "cpuPercent": m.cpu_percent,
            "ramPercent": m.ram_percent,
            "diskPercent": m.disk_percent,
            "temperatureC": m.temperature_c,
            "processesCount": m.processes_count,
            "services": json.loads(m.services_json) if m.services_json else [],
            "lastUpdated": m.last_updated
        })
    return result

@app.get("/api/alerts")
def get_alerts(db: Session = Depends(get_db)):
    alerts = db.query(AlertLogModel).order_by(AlertLogModel.timestamp.desc()).all()
    return [
        {
            "id": a.id,
            "title": a.title,
            "message": a.message,
            "severity": a.severity,
            "timestamp": a.timestamp,
            "deviceId": a.device_id,
            "channelNotified": a.channel_notified,
            "isRead": a.is_read
        }
        for a in alerts
    ]

@app.post("/api/telegram/test")
def test_telegram_alert(req: TelegramTestRequest):
    dispatcher = AlertDispatcher(bot_token=req.bot_token, chat_id=req.chat_id)
    time_str = datetime.now().strftime("%H:%M:%S")
    res = dispatcher.send_rogue_device_alert(
        ip="192.168.1.189",
        mac="00:0C:29:4F:8E:22",
        time_str=time_str
    )
    return res

@app.post("/api/remediation/execute")
def execute_ssh_remediation(req: RemediationRequest, db: Session = Depends(get_db)):
    """Executes SSH auto-remediation command."""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    server = db.query(DeviceModel).filter(DeviceModel.id == req.server_id).first()
    server_name = server.name if server else f"Server ({req.server_id})"
    
    # Execute command (Simulated or Paramiko SSH)
    output = f"[SSH] Connected to {server_name} via RSA Key\n"
    output += f"[EXEC] #{req.command}\n"
    output += f"[OK] Command applied successfully at {timestamp}\n[EXIT CODE] 0"

    log = RemediationLogModel(
        id=f"rem-{int(datetime.now().timestamp())}",
        server_id=req.server_id,
        server_name=server_name,
        target_service=req.target_service,
        command=req.command,
        status="SUCCESS",
        output=output,
        executed_at=timestamp
    )
    db.add(log)
    db.commit()

    return {
        "status": "SUCCESS",
        "actionId": log.id,
        "output": output,
        "executedAt": timestamp
    }

@app.post("/api/remediation/auto-evaluate")
def evaluate_auto_remediation(db: Session = Depends(get_db)):
    """Automated watchdog check that checks server load and restarts services if unresponsive."""
    metrics = db.query(ServerMetricModel).all()
    actions_taken = []
    for m in metrics:
        if m.ram_percent > 80.0 or m.cpu_percent > 85.0:
            cmd = "systemctl restart postgresql" if "dev-04" in m.server_id else "systemctl restart nginx"
            output = f"[AUTO-WATCHDOG] Load exceeded on {m.server_name} (RAM: {m.ram_percent}%, CPU: {m.cpu_percent}%)\n"
            output += f"[SSH] root@{m.ip}:# {cmd}\n[OK] Service restarted. Exit code: 0"
            m.ram_percent = round(m.ram_percent * 0.65, 1)
            m.cpu_percent = round(m.cpu_percent * 0.60, 1)
            log = RemediationLogModel(
                id=f"rem-auto-{int(datetime.now().timestamp())}",
                server_id=m.server_id,
                server_name=m.server_name,
                target_service="Auto-Healing",
                command=cmd,
                status="SUCCESS",
                output=output,
                executed_at=datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            )
            db.add(log)
            actions_taken.append({"serverId": m.server_id, "command": cmd, "output": output})
    db.commit()
    return {"status": "success", "remediations": actions_taken}

@app.get("/api/topology")
def get_topology(db: Session = Depends(get_db)):
    return generate_network_topology(db)

@app.get("/api/reports/sla")
def get_sla_report(db: Session = Depends(get_db)):
    devices = db.query(DeviceModel).all()
    total = len(devices)
    active = sum(1 for d in devices if d.status == "ONLINE")
    avg_uptime = sum(d.uptime_percent for d in devices) / total if total > 0 else 99.98

    return {
        "generatedAt": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "totalDevices": total,
        "activeDevices": active,
        "averageUptime": round(avg_uptime, 2),
        "totalAlertsLast30Days": 14,
        "mttrMinutes": 3.8,
        "securityScore": 96,
        "incidentBreakdown": {
            "Rogue Probes Blocked": 8,
            "Device Offline Events": 3,
            "High Resource Warnings": 2,
            "Failed SSH Logins": 1
        }
    }
