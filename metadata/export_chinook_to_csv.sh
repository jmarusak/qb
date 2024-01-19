sqlite3 -header -csv chinook.db 'select * from albums;' > albums.csv
sqlite3 -header -csv chinook.db 'select * from artists;' > artists.csv
sqlite3 -header -csv chinook.db 'select * from customers;' > customers.csv
sqlite3 -header -csv chinook.db 'select * from employees;' > employees.csv
sqlite3 -header -csv chinook.db 'select * from genres;' > genres.csv
sqlite3 -header -csv chinook.db 'select * from invoice_items;' > invoice_items.csv
sqlite3 -header -csv chinook.db 'select * from invoices;' > invoices.csv
sqlite3 -header -csv chinook.db 'select * from media_types;' > media_types.csv
sqlite3 -header -csv chinook.db 'select * from playlist_track;' > playlist_track.csv
sqlite3 -header -csv chinook.db 'select * from playlists;' > playlists.csv
sqlite3 -header -csv chinook.db 'select * from tracks;' > tracks.csv


sed -i '1s/.*/\L&/' albums.csv
sed -i '1s/.*/\L&/' artists.csv
sed -i '1s/.*/\L&/' customers.csv
sed -i '1s/.*/\L&/' employees.csv
sed -i '1s/.*/\L&/' genres.csv
sed -i '1s/.*/\L&/' invoice_items.csv
sed -i '1s/.*/\L&/' invoices.csv
sed -i '1s/.*/\L&/' media_types.csv
sed -i '1s/.*/\L&/' playlist_track.csv
sed -i '1s/.*/\L&/' playlists.csv
sed -i '1s/.*/\L&/' tracks.csv
