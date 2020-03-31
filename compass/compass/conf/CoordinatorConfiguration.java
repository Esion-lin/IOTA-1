/*
 * This file is part of TestnetCOO.
 *
 * Copyright (C) 2018 IOTA Stiftung
 * TestnetCOO is Copyright (C) 2017-2018 IOTA Stiftung
 *
 * TestnetCOO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * TestnetCOO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with TestnetCOO.  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     IOTA Stiftung <contact@iota.org>
 *     https://www.iota.org/
 */

package org.iota.compass.conf;

import com.beust.jcommander.Parameter;

import java.util.List;
import java.util.ArrayList;

public class CoordinatorConfiguration extends BaseConfiguration {
  @Parameter(names = "-bootstrap", description = "Bootstrap network")
  public boolean bootstrap = false;

  @Parameter(names = "-tick", description = "Milestone tick in milliseconds", required = true)
  public int tick = 15000;
  @Parameter(names = "-Mulitiple", description = "when use Mulitiple Coordinator")
  public boolean Mulitiple = false;

  @Parameter(names = "-hotstuff_port", description = "Select BFT node port.")
  public int hotstuff_port = 10600;
  @Parameter(names = "-hotstuff_host", description = "Select hotstuff host.")
  public String hotstuff_host = "127.0.0.1";

  @Parameter(names = "-hotstuff_remote", description = "trapdoor of remote connection")
  public boolean hotstuff_remote = false;

  @Parameter(names = "-hotstuff_recv_port", description = "Select BFT node port.")
  public int hotstuff_recv_port = 10080;
  @Parameter(names = "-depth", description = "Starting depth")
  public int depth = 0;

  @Parameter(names = "-cheat", description = "just for test.")
  public boolean cheat = false;

  @Parameter(names = "-cheatTrunk", description = "cheat with trunk,just for test.")
  public String cheatTrunk = "";

  @Parameter(names = "-cheatBranch", description = "cheat with branch, just for test.")
  public String cheatBranch = "";

  @Parameter(names = "-depthScale", description = "Time scale factor for depth decrease")
  public float depthScale = 1.01f;

  @Parameter(names = "-unsolidDelay", description = "Delay if node is not solid in milliseconds")
  public int unsolidDelay = 5000;

  @Parameter(names = "-inception", description = "Only use this if you know what you're doing.")
  public boolean inception = false;

  @Parameter(names = "-index", description = "Manually feed the current latest solid milestone index of IRI." +
          " So the next milestone will be index +1")
  public Integer index;

  @Parameter(names = "-validator", description = "Validator nodes to use")
  public List<String> validators = new ArrayList<>();

  @Parameter(names = "-propagationRetriesThreshold", description = "Number of milestone propagation retries we attempt before failing.")
  public int propagationRetriesThreshold = 5;

  @Parameter(names = "-allowDifferentCooAddress", description = "Don't fail on different Coordinator Addresses")
  public boolean allowDifferentCooAddress = false;

  @Parameter(names = "-statePath", description = "Path to compass state file.")
  public String statePath = "compass.state";

  @Parameter(names = "-APIRetries", description = "Number of attempts to retry failing API call.")
  public int APIRetries = 5;

  @Parameter(names = "-APIRetryInterval", description = "Interval (in milliseconds) to wait between failing API attempts.")
  public int APIRetryInterval = 1000;

  @Parameter(names = "-referenceLastMilestone", description = "Generate a milestone that references the last and then exit")
  public boolean referenceLastMilestone = false;
}
