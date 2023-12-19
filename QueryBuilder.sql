CREATE TABLE databases
(
  database_name        VARCHAR(50) NOT NULL,
  dbms                 VARCHAR(50) NOT NULL,
  driver               VARCHAR(50) NOT NULL,
  url                  VARCHAR(100) NOT NULL,
  user                 VARCHAR(50) NULL,
  password             VARCHAR(100) NULL
);

CREATE UNIQUE INDEX databases_pk_ix ON databases
(
  database_name
);

CREATE TABLE columns
(
  database_name        VARCHAR(50) NOT NULL,
  schema_name          VARCHAR(50) NOT NULL,
  table_name           VARCHAR(50) NOT NULL,
  column_name          VARCHAR(50) NOT NULL,
  data_type            VARCHAR(20) NOT NULL,
  nullable             VARCHAR(10) NOT NULL,
  column_id            INTEGER NOT NULL
);

CREATE UNIQUE INDEX columns_pk_ix ON columns
(
  column_name,
  table_name,
  schema_name,
  database_name
);

CREATE TABLE sqllog 
(
  sqltime TIMESTAMP,
  sqltext VARCHAR(2000)
);


create table relationships
(
    fk_table_name varchar(50),
    fk_column_name varchar(50),
    pk_table_name varchar(50),
    pk_column_name varchar(50)
);

CREATE UNIQUE INDEX relationships_pk_ix ON relationships
(
  fk_table_name,
  fk_column_name
);
