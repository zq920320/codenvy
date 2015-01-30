--
-- CODENVY CONFIDENTIAL
-- __________________
--
--  [2012] - [2015] Codenvy, S.A.
--  All Rights Reserved.
--
-- NOTICE:  All information contained herein is, and remains
-- the property of Codenvy S.A. and its suppliers,
-- if any.  The intellectual and technical concepts contained
-- herein are proprietary to Codenvy S.A.
-- and its suppliers and may be covered by U.S. and Foreign Patents,
-- patents in process, and are protected by trade secret or copyright law.
-- Dissemination of this information or reproduction of this material
-- is strictly forbidden unless prior written permission is obtained
-- from Codenvy S.A..
--

/* Single line comment */
CREATE TABLE METRICS
(
  ID bigserial NOT NULL,
  AMOUNT integer NOT NULL,
  START_TIME  bigint NOT NULL,
  STOP_TIME bigint NOT NULL,
  USER_ID   VARCHAR(128) NOT NULL,
  ACCOUNT_ID   VARCHAR(128) NOT NULL,
  WORKSPACE_ID   VARCHAR(128) NOT NULL,
  RUN_ID   VARCHAR(128) NOT NULL,
  CONSTRAINT PKEY_METRICS_ID PRIMARY KEY (ID)
);
CREATE INDEX IDX_METRICS_ID ON METRICS(ID);
CREATE INDEX IDX_METRICS_ACCCOUNT_START_STOP ON METRICS(ACCOUNT_ID, START_TIME, STOP_TIME);


/* agregated memory metrics*/
CREATE TABLE MEMORY_CHARGES
(
  ID bigserial NOT NULL,
  AMOUNT NUMERIC(20,5) NOT NULL,
  PRICE NUMERIC(20,5) NOT NULL,
  ACCOUNT_ID   VARCHAR(128) NOT NULL,
  WORKSPACE_ID   VARCHAR(128) NOT NULL,
  CONSTRAINT PKEY_MEMORY_CHARGES_ID PRIMARY KEY (ID)
);
CREATE INDEX IDX_MEMORY_CHARGES_ID ON MEMORY_CHARGES(ID);
CREATE INDEX IDX_MEMORY_CHARGES_AMOUNT ON MEMORY_CHARGES(AMOUNT);

/* agregated memory metrics*/
CREATE TABLE RECEIPTS
(
  ID bigserial NOT NULL,
  TOTAL NUMERIC(20,5) NOT NULL,
  ACCOUNT_ID   VARCHAR(128) NOT NULL,
  CREDIT_CARD   VARCHAR(128),
  PAYMENT_TIME  bigint ,
  PAYMENT_STATUS  smallint NOT NULL,
  MAILING_TIME bigint,
  CONSTRAINT PKEY_RECEIPTS_ID PRIMARY KEY (ID)
);
CREATE INDEX IDX_RECEIPTS_ID ON RECEIPTS(ID);


