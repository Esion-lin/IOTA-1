#!/bin/bash
rep=({0..3})
if [[ $# -gt 0 ]]; then
    rep=($@)
fi
for i in "${rep[@]}"; do
    echo "starting coordinator $i"
    ./docs/private_tangle/03_run_coordinator.sh -inception -Mulitiple -broadcast -hotstuff_port 1006${i} -hotstuff_recv_port 1008${i} -cheat -cheatTrunk EXZYPHIQPHLSO9FDDPAAHQZBYJULRYOAUQJHFLGRRESFNLANZOW9SDCV9RLRNPMNBBHV9HGWUDY9NT999 -cheatBranch JDXSPMZBMOOGITONSOL9YXRTNEMETTGGGNWQKLONADAJTQVWKRKYURFOPVXQCTGDBAOKLEFPJFJYGD999 -host "http://localhost:1426$i" > log${i} 2>&1 &
done
wait

