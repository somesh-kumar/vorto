/**
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.vorto.repository.upgrade;

import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.vorto.repository.domain.User;

public interface IUserUpgradeTask {

  /**
   * Performs the actual upgrade task
   * 
   * @throws UpgradeProblem
   */
  void doUpgrade(User user, Supplier<Object> upgradeContext) throws UpgradeProblem;

  /**
   * Contains the condition to be checked if the task is to be executed or not. If this condition is
   * empty, the upgrade task is always executed
   * 
   * @return task condition
   */
  Optional<IUpgradeTaskCondition> condition(User user, Supplier<Object> upgradeContext);

  /**
   * @return a short description of the task being performed
   */
  String getShortDescription();
}
