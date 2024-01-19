delete from databases;

insert into databases values ('SYSTEM', 'SQLITE', 'org.sqlite.JDBC', 'jdbc:sqlite:QueryBuilder.sqlite', 'admin', NULL);
insert into databases values ('martinview4', 'SQLITE', 'org.sqlite.JDBC', 'jdbc:sqlite:chinook.db', 'admin', NULL);

/*
export database info
.header on
.mode csv
.output C:\\Sandbox\\QueryBuilder\\QueryBuilder_Databases.csv
select database_name, dbms, driver, url from databases order by dbms, database_name;
.quit 
*/
