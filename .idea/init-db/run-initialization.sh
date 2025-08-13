#!/bin/bash

echo "🕒 SQL Server başlatılıyor, bekleniyor..."

until /opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P $DB_PASSWORD -Q "SELECT 1" > /dev/null 2>&1
do
  echo "⏳ Bekleniyor..."
  sleep 5
done

echo "✅ SQL Server hazır. init.sql çalıştırılıyor..."

# SQL scriptini çalıştır
/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P $DB_PASSWORD -d master -i /opt/init-db/init.sql

echo "✅ Veritabanı oluşturma tamamlandı."
