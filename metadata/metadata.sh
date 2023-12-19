sed -i '' -e '1d' metadata.csv
/usr/local/opt/sqlite/bin/sqlite3 ../QueryBuilder.sqlite < metadata.sql
