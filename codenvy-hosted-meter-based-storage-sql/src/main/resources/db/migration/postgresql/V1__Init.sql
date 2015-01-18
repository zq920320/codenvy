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
  CONSTRAINT ID_PKEY PRIMARY KEY (ID)
);

CREATE UNIQUE INDEX IDX_ACCCOUNT_START ON METRICS(ACCOUNT_ID, START_TIME, STOP_TIME);