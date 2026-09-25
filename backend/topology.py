from typing import Dict, List
from sqlalchemy.orm import Session
from database import DeviceModel, RogueDeviceModel

def generate_network_topology(session: Session) -> Dict:
    """Builds a hierarchical network topology graph for interactive visualization."""
    devices = session.query(DeviceModel).all()
    rogues = session.query(RogueDeviceModel).all()

    nodes = []
    links = []

    # 1. Identify Core Gateway and Switches
    core_router = next((d for d in devices if d.type == "ROUTER"), None)
    core_switch = next((d for d in devices if d.type == "SWITCH"), None)

    router_id = core_router.id if core_router else "dev-01"
    switch_id = core_switch.id if core_switch else "dev-02"

    for d in devices:
        # Assign coordinates based on hierarchy
        if d.type == "ROUTER":
            x, y = 0.50, 0.12
        elif d.type == "SWITCH":
            x, y = 0.50, 0.32
        elif d.type == "SERVER":
            idx = ["dev-03", "dev-04", "dev-05"].index(d.id) if d.id in ["dev-03", "dev-04", "dev-05"] else 0
            x, y = 0.15 + (idx * 0.24), 0.55
        else: # Cameras, Printers, Workstations
            offset_map = {"dev-06": 0.86, "dev-07": 0.20, "dev-08": 0.44, "dev-09": 0.68}
            x = offset_map.get(d.id, 0.50)
            y = 0.55 if d.id == "dev-06" else 0.78

        nodes.append({
            "id": d.id,
            "label": d.name,
            "ip": d.ip,
            "mac": d.mac,
            "type": d.type,
            "status": d.status,
            "latency_ms": d.latency_ms,
            "uptime_percent": d.uptime_percent,
            "x": x,
            "y": y,
            "parent_id": d.parent_switch_id
        })

        if d.parent_switch_id:
            links.append({
                "source": d.parent_switch_id,
                "target": d.id,
                "status": d.status
            })

    # Add rogues to topology
    for r in rogues:
        nodes.append({
            "id": r.id,
            "label": f"ROGUE: {r.ip}",
            "ip": r.ip,
            "mac": r.mac,
            "type": "ROGUE",
            "status": r.status,
            "x": 0.88,
            "y": 0.78,
            "parent_id": switch_id
        })
        links.append({
            "source": switch_id,
            "target": r.id,
            "status": "CRITICAL"
        })

    return {
        "nodes": nodes,
        "links": links,
        "total_nodes": len(nodes),
        "total_links": len(links)
    }
