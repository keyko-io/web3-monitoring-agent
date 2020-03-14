package io.keyko.monitoring.agent.core.chain.block.tx.criteria;

import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AllTransactionsMatchingCriteria<T> implements TransactionMatchingCriteria {

    private String nodeName;

    private List<TransactionStatus> statuses;

    @Override
    public boolean isAMatch(TransactionDetails tx) {
        return true;
    }

    @Override
    public boolean isOneTimeMatch() {
        return false;
    }

}
