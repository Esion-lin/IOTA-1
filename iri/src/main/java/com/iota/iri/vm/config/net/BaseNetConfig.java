/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.iota.iri.vm.config.net;

import com.iota.iri.vm.config.BlockchainConfig;
import com.iota.iri.vm.config.BlockchainNetConfig;
import com.iota.iri.vm.config.Constants;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class BaseNetConfig implements BlockchainNetConfig {
    private long[] blockNumbers = new long[64];
    private BlockchainConfig[] configs = new BlockchainConfig[64];
    private int count = 1;

    public void add(long startBlockNumber, BlockchainConfig config) {
        // if (count >= blockNumbers.length) throw new RuntimeException();
        // if (count > 0 && blockNumbers[count] >= startBlockNumber)
        //     throw new RuntimeException("Block numbers should increase");
        // if (count == 0 && startBlockNumber > 0) throw new RuntimeException("First config should start from block 0");
        blockNumbers[0] = 0;
        configs[0] = config;
        // count++;
    }

    @Override
    public BlockchainConfig getConfigForBlock(long blockNumber) {
        for (int i = 0; i < count; i++) {
            if (blockNumber < blockNumbers[i]) return configs[i - 1];
        }
        return configs[0];
    }

    @Override
    public Constants getCommonConstants() {
        // TODO make a guard wrapper which throws exception if the requested constant differs among configs
        return configs[0].getConstants();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append("BaseNetConfig{")
                .append("blockNumbers= ");

        for (int i = 0; i < count; ++i) {
            res.append("#").append(blockNumbers[i]).append(" => ");
            res.append(configs[i]);
            if (i != count - 1) {
                res.append(", ");
            }
        }

        res.append(" (total: ").append(count).append(")}");

        return res.toString();
    }
}
