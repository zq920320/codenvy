/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.dashboard.pig.store;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class BaseMongoTest
{
   protected DB db;

   private MongodProcess mongoProcess;

   private MongoClient mongoClient;
   
   protected final String serverUrl = "mongodb://localhost:12345/test";

   @BeforeClass
   public void setUp() throws Exception
   {
      startMongoServer();
      initClient();
   }

   @AfterClass
   public void tearDown() throws Exception
   {
      mongoClient.close();
      mongoProcess.stop();
   }

   private void startMongoServer() throws IOException, UnknownHostException
   {
      MongodStarter starter = MongodStarter.getDefaultInstance();
      MongodExecutable mongodExe = starter.prepare(new MongodConfig(Version.V2_3_0, 12345, false));
      mongoProcess = mongodExe.start();
   }

   private void initClient() throws UnknownHostException
   {
      MongoClientURI uri = new MongoClientURI(serverUrl);
      mongoClient = new MongoClient(uri);
      db = mongoClient.getDB(uri.getDatabase());
   }
}
