#!/bin/sh
set -euf

SQLCMD="/opt/mssql-tools18/bin/sqlcmd"   # server imajında yol bu
if [ ! -x "$SQLCMD" ]; then
  echo "❌ sqlcmd bulunamadı: $SQLCMD"
  exit 2
fi

DB_HOST="${DB_HOST:-mssql}"
DB_PORT="${DB_PORT:-1433}"
DB_USER="${DB_USER:-sa}"
DB_NAME="${DB_NAME:-crmdb}"
DB_PASS="${MSSQL_SA_PASSWORD:-${DB_PASSWORD:-}}"

if [ -z "${DB_PASS}" ]; then
  echo "❌ MSSQL_SA_PASSWORD/DB_PASSWORD boş."
  exit 2
fi

echo "⏳ SQL Server bekleniyor... host=${DB_HOST}:${DB_PORT}"
i=0; max=60
# -C: v18 sertifika trust, -l 2: 2 sn login timeout, -b: hata olursa exit
until "$SQLCMD" -S "${DB_HOST},${DB_PORT}" -U "${DB_USER}" -P "${DB_PASS}" -C -l 2 -b -Q "SELECT 1" >/dev/null 2>&1; do
  i=$((i+1))
  [ $i -ge $max ] && { echo "❌ SQL Server timeout (2dk)."; exit 2; }
  sleep 2
done

echo "✅ SQL hazır. Veritabanı: ${DB_NAME}"
# Tek satırlık T-SQL ile DB oluştur (varsa dokunmaz)
DB_SQL="
IF DB_ID(N'$DB_NAME') IS NULL
BEGIN
  DECLARE @sql NVARCHAR(MAX) = N'CREATE DATABASE [' + REPLACE(N'$DB_NAME', '''', '''''') + N']';
  EXEC sp_executesql @sql;
END
"

# -d master: master üzerinden oluştur
"$SQLCMD" -S "${DB_HOST},${DB_PORT}" -U "${DB_USER}" -P "${DB_PASS}" -C -b -d master -Q "$DB_SQL"

echo "✅ '$DB_NAME' hazır."
