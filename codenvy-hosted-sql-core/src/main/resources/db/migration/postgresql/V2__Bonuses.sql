--
-- CODENVY CONFIDENTIAL
-- __________________
--
--  [2012] - [2016] Codenvy, S.A.
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

CREATE TABLE BONUSES
(
  FID         BIGSERIAL      NOT NULL,
  FACCOUNT_ID VARCHAR(128)   NOT NULL,
  FAMOUNT     NUMERIC(20, 6) NOT NULL,
  FPERIOD     INT8RANGE      NOT NULL,
  FCAUSE      VARCHAR(128)   NOT NULL,
  FADDED      TIMESTAMP      NOT NULL,
  CONSTRAINT PKEY_BONUSES_ID PRIMARY KEY (FID)
);
CREATE UNIQUE INDEX IDX_BONUSES_ID ON BONUSES (FID);
CREATE INDEX IDX_BONUSES_ACCOUNT_PERIOD ON BONUSES (FACCOUNT_ID, FPERIOD);
CREATE INDEX IDX_BONUSES_CAUSE ON BONUSES (FCAUSE);

ALTER TABLE CHARGES ADD COLUMN FPROVIDED_FREE_AMOUNT NUMERIC(20, 6) NOT NULL;
ALTER TABLE CHARGES ADD COLUMN FPROVIDED_PREPAID_AMOUNT NUMERIC(20, 6) NOT NULL;