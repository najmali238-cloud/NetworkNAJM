import os
import requests
import json
from datetime import datetime
from typing import Optional, Dict

class AlertDispatcher:
    def __init__(self, bot_token: Optional[str] = None, chat_id: Optional[str] = None):
        self.bot_token = bot_token or os.getenv("TELEGRAM_BOT_TOKEN", "")
        self.chat_id = chat_id or os.getenv("TELEGRAM_CHAT_ID", "")
        self.slack_webhook = os.getenv("SLACK_WEBHOOK_URL", "")

    def send_telegram_alert(self, title: str, message: str, severity: str = "CRITICAL") -> Dict:
        """Dispatches an alert to Telegram Bot channel."""
        icon = "🚨" if severity == "CRITICAL" else ("⚠️" if severity == "WARNING" else "ℹ️")
        time_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        formatted_text = (
            f"{icon} *[NetGuard Enterprise Alert - {severity}]*\n"
            f"━━━━━━━━━━━━━━━━━━━\n"
            f"*Event:* {title}\n"
            f"*Details:* {message}\n"
            f"*Timestamp:* `{time_str}`\n"
            f"━━━━━━━━━━━━━━━━━━━\n"
            f"_NetGuard Automated Cybersecurity Guard_"
        )

        if not self.bot_token or not self.chat_id:
            # Simulated delivery output when tokens are not configured in environment
            print(f"[ALERT LOG] No Telegram credentials provided. Simulated payload:\n{formatted_text}")
            return {"status": "simulated", "message": "Telegram alert generated and logged (Token/ChatId missing)."}

        url = f"https://api.telegram.org/bot{self.bot_token}/sendMessage"
        payload = {
            "chat_id": self.chat_id,
            "text": formatted_text,
            "parse_mode": "Markdown"
        }

        try:
            res = requests.post(url, json=payload, timeout=5)
            if res.status_code == 200:
                return {"status": "success", "response": res.json()}
            else:
                return {"status": "error", "code": res.status_code, "detail": res.text}
        except Exception as e:
            return {"status": "failed", "error": str(e)}

    def send_rogue_device_alert(self, ip: str, mac: str, time_str: str) -> Dict:
        """Specific alert according to specification: IP | MAC | Time"""
        title = "Rogue Device Detected On Subnet"
        message = f"`{ip} | {mac} | {time_str}`\nImmediate ARP isolation initiated."
        return self.send_telegram_alert(title, message, severity="CRITICAL")

    def send_slack_webhook(self, title: str, message: str) -> Dict:
        if not self.slack_webhook:
            return {"status": "skipped", "message": "Slack webhook URL not set."}
        try:
            res = requests.post(self.slack_webhook, json={"text": f"*{title}*\n{message}"}, timeout=5)
            return {"status": "success", "code": res.status_code}
        except Exception as e:
            return {"status": "failed", "error": str(e)}
