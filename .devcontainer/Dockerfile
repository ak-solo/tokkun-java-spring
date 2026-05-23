FROM mcr.microsoft.com/devcontainers/java:21

# PostgreSQL クライアントツールをインストール
RUN apt-get update && apt-get install -y \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*
