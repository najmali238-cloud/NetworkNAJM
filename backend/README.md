# 🚀 NetGuard Enterprise Sentinel Backend - Quickstart Guide

## ⏱️ 1-Minute Launch (تشغيل فوري خلال دقيقة واحدة)

### Method 1: Direct Python Runner (تشغيل مباشر)
```bash
# 1. انتقل إلى مجلد الخلفية وثبت المتطلبات
cd backend
python3 -m venv venv
source venv/bin/activate   # On Windows: venv\Scripts\activate
pip install -r requirements.txt

# 2. تشغيل السيرفر مباشرة
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### Method 2: Docker Compose (تشغيل عبر الحاويات)
```bash
cd backend
docker-compose up -d --build
```

---

## 📡 API Endpoints Summary

- **Health Check:** `GET /api/health`
- **Device Inventory:** `GET /api/devices`
- **Ping Device:** `POST /api/devices/{id}/ping`
- **Subnet Audit:** `POST /api/scan`
- **Rogue Devices:** `GET /api/rogue-devices`
- **Isolate Rogue:** `POST /api/rogue-devices/{id}/isolate`
- **Trust Rogue:** `POST /api/rogue-devices/{id}/trust`
- **Live Server Metrics:** `GET /api/servers/metrics`
- **Interactive Topology Graph:** `GET /api/topology`
- **SSH Auto-Remediation:** `POST /api/remediation/execute`
- **SLA Uptime Report:** `GET /api/reports/sla`
- **Interactive Swagger Docs:** `http://localhost:8000/docs`
