FROM python:3.11-slim

WORKDIR /app

# Install system utilities for ping and raw network sockets
RUN apt-get update && apt-get install -y --no-install-recommends \
    iputils-ping \
    net-tools \
    curl \
    gcc \
    libcap2-bin \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Grant python permission to use raw sockets for ICMP ping without root
RUN setcap cap_net_raw+ep $(readlink -f $(which python3)) || true

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
