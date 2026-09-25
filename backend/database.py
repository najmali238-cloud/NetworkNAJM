import os
import json
from datetime import datetime
from sqlalchemy import create_engine, Column, String, Float, Integer, Boolean, Text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./netguard.db")

engine = create_engine(
    DATABASE_URL,
    connect_args={"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {}
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

class DeviceModel(Base):
    __tablename__ = "devices"
    id = Column(String, primary_key=True, index=True)
    name = Column(String, nullable=False)
    ip = Column(String, nullable=False, unique=True, index=True)
    mac = Column(String, nullable=False)
    type = Column(String, default="SERVER")
    status = Column(String, default="ONLINE")
    latency_ms = Column(Integer, default=2)
    uptime_percent = Column(Float, default=99.98)
    last_checked = Column(String, default="")
    open_ports = Column(Text, default="[]") # JSON string
    parent_switch_id = Column(String, nullable=True)

class RogueDeviceModel(Base):
    __tablename__ = "rogue_devices"
    id = Column(String, primary_key=True, index=True)
    ip = Column(String, nullable=False, unique=True)
    mac = Column(String, nullable=False)
    vendor = Column(String, default="Unknown Hardware OUI")
    detected_at = Column(String, default="")
    first_seen = Column(String, default="")
    open_ports = Column(Text, default="[22, 80, 445, 3389]")
    threat_level = Column(String, default="HIGH")
    status = Column(String, default="NEW") # NEW, ISOLATED, TRUSTED

class AlertLogModel(Base):
    __tablename__ = "alerts"
    id = Column(String, primary_key=True, index=True)
    title = Column(String, nullable=False)
    message = Column(Text, nullable=False)
    severity = Column(String, default="INFO")
    timestamp = Column(String, default="")
    device_id = Column(String, nullable=True)
    channel_notified = Column(String, default="Telegram Bot")
    is_read = Column(Boolean, default=False)

class ServerMetricModel(Base):
    __tablename__ = "server_metrics"
    server_id = Column(String, primary_key=True, index=True)
    server_name = Column(String, nullable=False)
    ip = Column(String, nullable=False)
    cpu_percent = Column(Float, default=25.0)
    ram_percent = Column(Float, default=45.0)
    disk_percent = Column(Float, default=50.0)
    temperature_c = Column(Float, default=45.0)
    processes_count = Column(Integer, default=120)
    services_json = Column(Text, default="[]")
    last_updated = Column(String, default="")

class RemediationLogModel(Base):
    __tablename__ = "remediations"
    id = Column(String, primary_key=True, index=True)
    server_id = Column(String, nullable=False)
    server_name = Column(String, nullable=False)
    target_service = Column(String, nullable=False)
    command = Column(String, nullable=False)
    status = Column(String, default="SUCCESS")
    output = Column(Text, default="")
    executed_at = Column(String, default="")

class SettingModel(Base):
    __tablename__ = "settings"
    key = Column(String, primary_key=True, index=True)
    value = Column(Text, default="")

def init_db():
    Base.metadata.create_all(bind=engine)
    session = SessionLocal()
    try:
        # Seed initial data if table is empty
        if session.query(DeviceModel).count() == 0:
            now_str = datetime.now().strftime("%H:%M:%S")
            demo_devices = [
                DeviceModel(
                    id="dev-01", name="Core Gateway Router", ip="192.168.1.1", mac="E4:8D:8C:11:A4:01",
                    type="ROUTER", status="ONLINE", latency_ms=2, uptime_percent=99.99, last_checked=now_str,
                    open_ports=json.dumps([53, 80, 443])
                ),
                DeviceModel(
                    id="dev-02", name="HQ Core Switch 48P", ip="192.168.1.2", mac="00:1A:2B:3C:4D:5E",
                    type="SWITCH", status="ONLINE", latency_ms=1, uptime_percent=99.98, last_checked=now_str,
                    open_ports=json.dumps([22, 161]), parent_switch_id="dev-01"
                ),
                DeviceModel(
                    id="dev-03", name="Production App Server (Prod-01)", ip="192.168.1.10", mac="52:54:00:AB:12:34",
                    type="SERVER", status="ONLINE", latency_ms=3, uptime_percent=99.95, last_checked=now_str,
                    open_ports=json.dumps([22, 80, 443, 8080]), parent_switch_id="dev-02"
                ),
                DeviceModel(
                    id="dev-04", name="Database Cluster Master (DB-01)", ip="192.168.1.12", mac="52:54:00:CD:56:78",
                    type="SERVER", status="WARNING", latency_ms=5, uptime_percent=99.91, last_checked=now_str,
                    open_ports=json.dumps([22, 5432, 6379]), parent_switch_id="dev-02"
                ),
                DeviceModel(
                    id="dev-05", name="Backup & Storage Server (NAS-01)", ip="192.168.1.20", mac="00:11:32:9F:8A:10",
                    type="SERVER", status="ONLINE", latency_ms=4, uptime_percent=99.89, last_checked=now_str,
                    open_ports=json.dumps([22, 445, 2049]), parent_switch_id="dev-02"
                ),
                DeviceModel(
                    id="dev-06", name="Security Camera 01 (Main Gate)", ip="192.168.1.41", mac="B8:A3:86:77:21:01",
                    type="CAMERA", status="ONLINE", latency_ms=11, uptime_percent=99.70, last_checked=now_str,
                    open_ports=json.dumps([554, 8000]), parent_switch_id="dev-02"
                ),
                DeviceModel(
                    id="dev-07", name="Security Camera 05 (HQ Entrance)", ip="192.168.1.45", mac="B8:A3:86:99:43:05",
                    type="CAMERA", status="OFFLINE", latency_ms=0, uptime_percent=94.20, last_checked="12 min ago",
                    open_ports=json.dumps([]), parent_switch_id="dev-02"
                ),
                DeviceModel(
                    id="dev-08", name="HQ Enterprise Laser Printer", ip="192.168.1.60", mac="3C:D9:2B:10:55:F1",
                    type="PRINTER", status="ONLINE", latency_ms=8, uptime_percent=98.50, last_checked=now_str,
                    open_ports=json.dumps([80, 515, 9100]), parent_switch_id="dev-02"
                ),
                DeviceModel(
                    id="dev-09", name="SecOps Lead Workstation", ip="192.168.1.101", mac="90:B1:1C:33:AA:88",
                    type="WORKSTATION", status="ONLINE", latency_ms=3, uptime_percent=99.40, last_checked=now_str,
                    open_ports=json.dumps([135, 445]), parent_switch_id="dev-02"
                )
            ]
            session.add_all(demo_devices)

        if session.query(RogueDeviceModel).count() == 0:
            now_time = datetime.now().strftime("%H:%M:%S")
            rogue = RogueDeviceModel(
                id="rogue-01",
                ip="192.168.1.189",
                mac="00:0C:29:4F:8E:22",
                vendor="VMware / Unauthorized MAC OUI",
                detected_at=now_time,
                first_seen=f"Today at {now_time}",
                open_ports=json.dumps([22, 80, 445, 3389]),
                threat_level="CRITICAL",
                status="NEW"
            )
            session.add(rogue)

        if session.query(ServerMetricModel).count() == 0:
            now_str = datetime.now().strftime("%H:%M:%S")
            metrics = [
                ServerMetricModel(
                    server_id="dev-03", server_name="Production App Server (Prod-01)", ip="192.168.1.10",
                    cpu_percent=38.0, ram_percent=64.0, disk_percent=52.0, temperature_c=46.5, processes_count=142,
                    services_json=json.dumps([
                        {"name": "Nginx Reverse Proxy", "isRunning": True, "port": 80},
                        {"name": "Docker Engine", "isRunning": True, "port": 2375},
                        {"name": "OpenSSH Daemon", "isRunning": True, "port": 22}
                    ]),
                    last_updated=now_str
                ),
                ServerMetricModel(
                    server_id="dev-04", server_name="Database Cluster Master (DB-01)", ip="192.168.1.12",
                    cpu_percent=76.0, ram_percent=86.0, disk_percent=88.0, temperature_c=68.2, processes_count=210,
                    services_json=json.dumps([
                        {"name": "PostgreSQL DB Engine", "isRunning": True, "port": 5432},
                        {"name": "Redis In-Memory Cache", "isRunning": True, "port": 6379},
                        {"name": "OpenSSH Daemon", "isRunning": True, "port": 22}
                    ]),
                    last_updated=now_str
                )
            ]
            session.add_all(metrics)

        if session.query(AlertLogModel).count() == 0:
            now_str = datetime.now().strftime("%H:%M:%S")
            alerts = [
                AlertLogModel(
                    id="alert-01",
                    title="Camera 05 Offline!",
                    message="HQ Entrance Camera at 192.168.1.45 failed 5 consecutive ping probes.",
                    severity="CRITICAL",
                    timestamp=now_str,
                    device_id="dev-07",
                    channel_notified="Telegram Bot"
                ),
                AlertLogModel(
                    id="alert-02",
                    title="Rogue Device Detected",
                    message=f"Unauthorized device discovered: 192.168.1.189 | 00:0C:29:4F:8E:22 | {now_str}",
                    severity="CRITICAL",
                    timestamp=now_str,
                    device_id="rogue-01",
                    channel_notified="Telegram Bot + Push"
                )
            ]
            session.add_all(alerts)

        session.commit()
    finally:
        session.close()
