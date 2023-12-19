-- import
--delete from columns where database_name = 'np-deep-ingestion' /* and schema_name = 'work_asfi' */;
.separator "|"
.import metadata.csv columns
