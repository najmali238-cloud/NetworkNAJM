import socket
import subprocess
import platform
import time
import psutil
from typing import Dict, List, Tuple

def ping_host(ip: str, timeout_sec: float = 1.0) -> Tuple[bool, float]:
    """Pings an IP address and returns (is_online, latency_ms)"""
    param = "-n" if platform.system().lower() == "windows" else "-c"
    timeout_param = "-w" if platform.system().lower() == "windows" else "-W"
    
    start_time = time.time()
    try:
        cmd = ["ping", param, "1", timeout_param, str(int(timeout_sec)), ip]
        res = subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=timeout_sec + 0.5)
        elapsed_ms = (time.time() - start_time) * 1000.0
        return (res.returncode == 0, round(elapsed_ms, 2))
    except Exception:
        # Fallback to socket probe
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.settimeout(timeout_sec)
            s.connect((ip, 80))
            s.close()
            elapsed_ms = (time.time() - start_time) * 1000.0
            return (True, round(elapsed_ms, 2))
        except Exception:
            return (False, 0.0)

def scan_ports(ip: str, ports: List[int] = None, timeout: float = 0.5) -> List[int]:
    """Scans common TCP ports on the given IP and returns list of open ports."""
    if ports is None:
        ports = [21, 22, 23, 25, 53, 80, 110, 135, 139, 443, 445, 1433, 3306, 3389, 5432, 6379, 8080]
    
    open_ports = []
    for port in ports:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.settimeout(timeout)
        try:
            res = s.connect_ex((ip, port))
            if res == 0:
                open_ports.append(port)
        except Exception:
            pass
        finally:
            s.close()
    return open_ports

def get_system_hardware_metrics() -> Dict:
    """Collects CPU, RAM, and Disk metrics via psutil."""
    cpu_percent = psutil.cpu_percent(interval=0.2)
    ram = psutil.virtual_memory()
    disk = psutil.disk_usage('/')
    
    temp_c = 45.0
    try:
        temps = psutil.sensors_temperatures()
        if temps:
            for name, entries in temps.items():
                if entries:
                    temp_c = entries[0].current
                    break
    except Exception:
        pass

    return {
        "cpu_percent": cpu_percent,
        "ram_percent": ram.percent,
        "disk_percent": disk.percent,
        "temperature_c": temp_c,
        "processes_count": len(psutil.pids())
    }

def discover_arp_devices(subnet: str = "192.168.1.0/24") -> List[Dict]:
    """Scans network for active devices via Scapy ARP ping or system ARP table."""
    devices = []
    try:
        from scapy.all import Ether, ARP, srp
        ans, _ = srp(Ether(dst="ff:ff:ff:ff:ff:ff") / ARP(pdst=subnet), timeout=1.5, verbose=False)
        for snd, rcv in ans:
            devices.append({
                "ip": rcv.psrc,
                "mac": rcv.hwsrc
            })
    except Exception:
        # Fallback reading /proc/net/arp on Linux systems
        try:
            with open("/proc/net/arp", "r") as f:
                lines = f.readlines()[1:]
                for line in lines:
                    parts = line.split()
                    if len(parts) >= 4 and parts[3] != "00:00:00:00:00:00":
                        devices.append({
                            "ip": parts[0],
                            "mac": parts[3]
                        })
        except Exception:
            pass
    return devices
