INSERT INTO NPLATFORM_TYPE ( ID , CREATED , CREATED_BY , NAME , STATUS , UPDATED ,UPDATED_BY ,VERSION )
VALUES (1, current_date, 'MASTER_DATA', 'ios', 'ACTIVE',current_date,'MASTER_DATA',1 );
INSERT INTO NPLATFORM_TYPE ( ID , CREATED , CREATED_BY , NAME , STATUS , UPDATED ,UPDATED_BY ,VERSION )
VALUES (2, current_date, 'MASTER_DATA', 'android', 'ACTIVE',current_date,'MASTER_DATA',1 );

UPDATE SEQUENCE SET SEQ_COUNT = 2 WHERE SEQ_NAME = 'NPLATFORMTYPE_SEQ';
