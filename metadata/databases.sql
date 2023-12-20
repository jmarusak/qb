/* DATABASES */
/*
delete from databases where database_name = 'SYSTEM';
*/

insert into databases values ('SYSTEM', 'SQLITE', 'org.sqlite.JDBC', 'jdbc:sqlite:QueryBuilder.sqlite', 'admin', NULL);
insert into databases values ('CHINOOK', 'SQLITE', 'org.sqlite.JDBC', 'jdbc:sqlite:chinook.db', 'admin', NULL);

/*
JDBC list
dir C:\Sandbox\jdbc\* > C:\Sandbox\QueryBuilder\QueryBuilder_JDBC.txt 
*/


/*
export database info
.header on
.mode csv
.output C:\\Sandbox\\QueryBuilder\\QueryBuilder_Databases.csv
select database_name, dbms, driver, url from databases order by dbms, database_name;
.quit 
*/

/* SYSTEM */

/*
./sqlite3 QueryBuilder.sqlite
delete from columns where database_name = 'SYSTEM';
.separator "|"
.import C:\\Sandbox\\QueryBuilder\\QueryBuilder_Metadata.txt columns
*/


/* METADATA */

/*
C:\Sandbox\QueryBuilder\metadata\metadata.txt
C:\Sandbox\QueryBuilder\metadata\metadata_sqlite.bat
delete from columns where database_name = 'CAMPPR' AND schema_name = 'X219686';
.separator "|"
.import C:\\Sandbox\\QueryBuilder\\metadata\\metadata.txt columns
*/  
  
/* ORACLE */
select cl.database_name||'|'||cl.schema_name||'|'||cl.table_name||'|'||cl.column_name||'|'||cl.data_type||'|'||cl.nullable||'|'||cl.column_id metadata
from (
    select (select replace(global_name, '.WORLD', '') as cur_db from global_name) as database_name,
        upper(b.column_name) as column_name,
        upper(a.table_name) as table_name,
        upper(a.owner) as schema_name,
        LOWER(b.data_type)||DECODE(b.data_type, 'NUMBER', REPLACE('('||b.data_precision||','||b.data_scale||')', '(,)', ''), 'VARCHAR2', '('||b.data_length||')', 'CHAR', '('||b.data_length||')', NULL) as data_type,
        DECODE(b.nullable, 'Y', 'NULL', 'NOT NULL') as nullable,
        b.column_id as column_id
      from all_tables a, all_tab_columns b
      where a.table_name = b.table_name
        and a.owner = b.owner
        --and a.owner IN ('X219686') and a.table_name IN ('CLICKSTREAM_URLS') --'BILLING_ACCOUNT_PROFL_VIEW', 'CUSTOMER_PROFL_VIEW', 'PRODUCT_INSTANCE_PROFL_VIEW', 'PROSPECT_PROFL_ORC', 'Q12019_MSS') --CAMPPR
        --and a.owner IN ('TSETTINO') and a.table_name IN ('REF_BAN_CBUCID', 'REF_BAN_NAMES') --'BUSINESS_MKTG_DOMAINS','WLS_CAMPAIGN_BUILD') --CAMPPR
        --and a.owner IN ('NCR') and a.table_name IN ('TMP_LMS_TO_POS_PROMOTION') --CAMPPR
        and a.owner IN ('DMADM') and a.table_name IN  ('TEAM_MEMBER_DIM') -- 'MBIS_CUST_ACCT_TMP', 'MBIS_PRODUCT_USE_MO_TMP', 'MBIS_LONG_DIST_USE_MO_TMP', 'MBIS_SAP_PRODUCT_HIERARCHY_TMP') -- 'CUSTOMER_HIERARCHY_DIM', 'MASTER_SOURCE_DIM', 'PRODUCT_HIERARCHY_DIM', 'CORP_CUSTOMER_DIM', 'PRODUCT_REPORT_HIERARCHY_DIM', 'CALENDAR_DIM', 'CORP_CUST_PROD_REV_MTHLY_SUM', 'CORP_CUSTOMER_PROFL', 'CBU_SEGMENT_TYPE_DIM') --HPBIPR
        --and a.owner IN ('DISTADM') and a.table_name IN ('PCS_EQUIP_WARRANTY','WARRANTY_TYPE') --'EXTND_WRNTY_CNTRCT_STATUS', 'EXTND_WRNTY_CONTRACT', 'EXTND_WRNTY_CONTRACT_DVC', a.INI_TRANS 'MKTG_PROMOTION_PROGRAM', 'NON_TELUS_PCS_EQUIP', 'PRODUCT', 'PCS_EQUIPMENT', 'USIM_SUBSCRIBER_PROFILE', 'USIM_PCS_DEVICE_ASSOC') --DISTPRR
        --and a.owner IN ('MI_TEMPADM') and a.table_name IN ('REFERENCE_CHANNELS_TM_V1', 'REFERENCE_COMMUNITY_TM','REFERENCE_PRICE_PLAN_TM_V3')-- 'MONTHEND_CUMBASE_BIB_202005', 'REFERENCE_DEVICE_TM', 'PCS_EQUIPMENT', 'USIM_SUBSCRIBER_PROFILE', 'USIM_PCS_DEVICE_ASSOC') --DISTPRR
        --and a.owner IN ('NCR_ADM') and a.table_name IN ('NCR_OFFERING_ASSIGNMENT') -- NCREPR
        --and a.owner IN ('WCBM_PERMADM') AND A.TABLE_NAME IN ('DAILY_BUSINESS_POST_CANCELS') -- BUSWSPR
        --and a.owner IN ('MODELING_TEMPADM') AND A.TABLE_NAME IN ('SBS_BAN_CHURN_SV_CURRENT') -- BUSWSPR
        --and a.owner IN ('RWRDADM') AND A.TABLE_NAME IN ('CUSTOMER_SERVICE_AGREEMENT','REWARD_ACCOUNT_AGREEMENT', 'REWARD_ACCOUNT') -- RWRDPR
        --and a.owner IN ('EPI_STAGING') AND A.TABLE_NAME IN ('APS_SMS_OPTIN','APS_SMS_OPTOUT')--'EMAIL_BILLC28_CURRENT','EM_HARDBOUNCED_V1') -- CAMPPR
        --and a.owner IN ('PRM_ADM') and a.table_name IN ('PORT_REQUEST', 'PORT_REQUEST_STEP', 'PORT_REQUEST_STEP_TEL_NO') -- LNPPR
        --and a.owner IN ('CPH_WS') and a.table_name IN ('CANADIAN_DATA_RESTRICTIONS') -- HPBI
) cl
order by cl.column_name, cl.table_name, cl.schema_name;

/* ORACLE DB LINK */
select cl.database_name||'|'||cl.schema_name||'|'||cl.table_name||'|'||cl.column_name||'|'||cl.data_type||'|'||cl.nullable||'|'||cl.column_id metadata
from (
      select
        'CAMPPR' as database_name,
        upper(b.column_name) as column_name,
        upper(a.table_name)||'@COHE' as table_name,
        upper(a.owner) as schema_name,
        LOWER(b.data_type)||DECODE(b.data_type, 'NUMBER', REPLACE('('||b.data_precision||','||b.data_scale||')', '(,)', ''), 'VARCHAR2', '('||b.data_length||')', 'CHAR', '('||b.data_length||')', NULL) as data_type,
        DECODE(b.nullable, 'Y', 'NULL', 'NOT NULL') as nullable,
        b.column_id as column_id
      from all_tables@cohe a, all_tab_columns@cohe b
      where a.table_name = b.table_name
        and a.owner = b.owner
        and a.owner IN ('REFADM') and a.table_name IN ('MARKET_NPA_NXX_LR') --'SOC') -- CAMPPR
      --  and a.owner IN ('KBADM') and a.table_name IN ('SERVICE_AGREEMENT')--'SUBSCRIBER_RSOURCE', 'ADDRESS_NAME_LINK', 'NAME_DATA', 'ADDRESS_DATA', 'CUSTOMER', 'SUBSCRIBER', 'BILLING_ACCOUNT')
) cl
order by cl.column_name, cl.table_name, cl.schema_name;


/* SQLSERVER */
select cl.database_name+'|'+cl.schema_name+'|'+cl.table_name+'|'+cl.column_name+'|'+cl.data_type+'|'+cl.nullable+'|'+cl.column_id
from (
    select upper(db_name()) as database_name,
        upper(s.name) as schema_name,
        upper(o.name) as table_name,
        upper(c.name) as column_name,
        rtrim(t.name + case when t.name not in ('date','int','shortint','tinyint') then '(' + case when t.name='decimal' then cast(c.precision as varchar)+','+cast(c.scale as varchar) else case when t.name = 'datetime2' then cast(c.scale as varchar) else cast(c.max_length as varchar) end end + ') ' else ' ' end) as data_type,
        case when c.is_nullable = 1 then 'NULL' else 'NOT NULL' end as nullable,
        cast(c.column_id as varchar) as column_id
    from sys.schemas s, sys.objects o, sys.columns c, sys.types t
    where s.schema_id = o.schema_id
     and o.object_id = c.object_id
	   and c.system_type_id = t.system_type_id
     and t.name not in ('sysname')
     and o.name in ('PVT_PORTING_ALLSTATUS_STG_V3') --'DAILY_BIB_LOADING', 'PVT_ACTSDEACTS_STG_DERE_V4',  'V_RENEWALS_WOME_V4', 'V_RENEWALS_WOME_V3')  -- VISION
     -- and o.name in ('HSIA_ADDDELETE_BILLSYS' ,'SBS_PRODUCT_CBUCID')  -- VISION_WLN
     -- and upper(o.name) in ('ATOZ_EXTRACT') -- ATOZ
) cl
order by cl.column_name, cl.table_name, cl.schema_name

/* SQLLITE */
.separator |
.header off
.output C:\\Sandbox\\QueryBuilder\\metadata\\metadata.txt
SELECT 'CHINOOK' AS database_name,
  'MAIN' AS schema_name,
  UPPER(m.name) AS table_name, 
  UPPER(p.name) as column_name,
  UPPER(p.type) AS data_type,
  'NULL',
  p.cid
FROM 
  sqlite_master AS m
JOIN 
  pragma_table_info(m.name) AS p
WHERE m.name NOT LIKE 'SQLITE%'
ORDER BY 
  m.name, 
  p.cid;
.exit


/* DB2 */
SELECT
'CORPDW|' CONCAT RTRIM(TBCREATOR) CONCAT '|' CONCAT RTRIM(TBNAME) CONCAT '|' CONCAT RTRIM(NAME) CONCAT '|' CONCAT RTRIM(COLTYPE) CONCAT '(' CONCAT RTRIM(CAST(LENGTH AS CHAR(5))) CONCAT ')' CONCAT '|NULL' CONCAT '|' CONCAT RTRIM(CAST(COLNO AS CHAR(3))) AS METADATA
FROM
  SYSIBM.SYSCOLUMNS
WHERE
  RTRIM(TBCREATOR) CONCAT '.' CONCAT TBNAME = 'SND.CBU_CID_DUNS'
ORDER BY TBNAME, COLNO;


/* ORACLE - metadata on metadata :-) */
select 'SYSTEM'||'|'||cl.schema_name||'|'||cl.table_name||'|'||cl.column_name||'|'||cl.data_type||'|'||cl.nullable||'|'||cl.column_id metadata
from (
    select (select replace(global_name, '.WORLD', '') as cur_db from global_name) as database_name,
        lower(b.column_name) as column_name,
        lower(b.table_name) as table_name,
        lower(b.owner) as schema_name,
        LOWER(b.data_type)||DECODE(b.data_type, 'NUMBER', REPLACE('('||b.data_precision||','||b.data_scale||')', '(,)', ''), 'VARCHAR2', '('||b.data_length||')', 'CHAR', '('||b.data_length||')', NULL) as data_type,
        DECODE(b.nullable, 'Y', 'NULL', 'NOT NULL') as nullable,
        b.column_id as column_id
      from all_tab_columns b
      where b.table_name = 'ALL_TAB_COLUMNS'
) cl
order by cl.column_id, cl.table_name, cl.schema_name;


/* MySQL */
select concat(cl.database_name,'|',cl.schema_name,'|',cl.table_name,'|',cl.column_name,'|',cl.data_type,'|',cl.nullable,'|',cl.column_id) metadata
from (
  select
    'NGSP' as database_name,
    upper(column_name) as column_name,
    upper(table_name) as table_name,
    upper(table_schema) as schema_name,
    upper(column_type) as data_type,
    if(is_nullable='YES', 'NULL', 'NOT NULL') as nullable,
    ordinal_position as column_id
  from information_schema.columns
  where table_schema = 'ngsp_order_pr' and table_name = 'ngsp_order'
  ) cl
  order by cl.column_name, cl.table_name, cl.schema_name;

/* BigQuery */
SELECT
    CONCAT(table_catalog, '|', table_schema, '|', table_name, '|', column_name, '|', 
    REPLACE(data_type, ',',' '), '|', is_nullable, '|', ordinal_position) as metadata
FROM
   `project_id.dataset.INFORMATION_SCHEMA.COLUMNS`
WHERE
  TRUE OR  
  table_name IN
(
    NULL
-- 'account_metrics_monthly'
)
ORDER BY
  table_name,
  ordinal_position;

/* BigQuery */
SELECT
    CONCAT(table_catalog, '|', table_schema, '|', table_name, '|', column_name, '|', 
    REPLACE(data_type, ',',' '), '|', is_nullable, '|', ordinal_position) as metadata
FROM
   `project_id.dataset.INFORMATION_SCHEMA.COLUMNS`
WHERE
  TRUE OR  
  table_name IN
(
    NULL
-- 'account_metrics_monthly'
)
ORDER BY
  table_name,
  ordinal_position;
