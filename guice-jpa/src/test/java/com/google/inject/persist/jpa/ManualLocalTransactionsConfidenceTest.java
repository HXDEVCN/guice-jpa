/**
 * Copyright (C) 2010 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.persist.jpa;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import junit.framework.TestCase;

import com.github.sclassen.guicejpa.EntityManagerProvider;
import com.github.sclassen.guicejpa.Transactional;
import com.github.sclassen.guicejpa.PersistenceModule;
import com.github.sclassen.guicejpa.PersistenceService;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This class was copied from guice-persist v3.0 and adopted to fit the API of guice-jpa
 * 
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class ManualLocalTransactionsConfidenceTest extends TestCase {
  private Injector injector;
  private static final String UNIQUE_TEXT_3 =
      ManualLocalTransactionsConfidenceTest.class.getSimpleName()
          + "CONSTRAINT_VIOLATING some other unique text" + new Date();

  @Override
  public void setUp() {
    PersistenceModule pm = new PersistenceModule();
    pm.addApplicationManagedPersistenceUnit("testUnit");
    injector = Guice.createInjector(pm);

    //startup persistence
    injector.getInstance(PersistenceService.class).start();
  }

  @Override
  public final void tearDown() {
    injector.getInstance(PersistenceService.class).stop();
  }

  public void testThrowingCleanupInterceptorConfidence() {
    Exception e = null;
    try {
      System.out.println(
          "\n\n******************************* EXPECTED EXCEPTION NORMAL TEST BEHAVIOR **********");
      injector.getInstance(TransactionalObject.class).runOperationInTxn();
      fail();
    } catch (RuntimeException re) {
      e = re;
      System.out.println(
          "\n\n******************************* EXPECTED EXCEPTION NORMAL TEST BEHAVIOR **********");
      re.printStackTrace(System.out);
      System.out.println(
          "\n\n**********************************************************************************");
    }

    assertNotNull("No exception was thrown!", e);
    assertTrue("Exception thrown was not what was expected (i.e. commit-time)",
        e instanceof PersistenceException);
  }

  public static class TransactionalObject {
    @Inject EntityManagerProvider emProvider;

    @Transactional
    public void runOperationInTxn() {
      EntityManager em = emProvider.get();
      JpaParentTestEntity entity = new JpaParentTestEntity();
      JpaTestEntity child = new JpaTestEntity();

      child.setText(UNIQUE_TEXT_3);
      em.persist(child);

      entity.getChildren().add(child);
      em.persist(entity);

      entity = new JpaParentTestEntity();
      entity.getChildren().add(child);
      em.persist(entity);
    }
  }
}
