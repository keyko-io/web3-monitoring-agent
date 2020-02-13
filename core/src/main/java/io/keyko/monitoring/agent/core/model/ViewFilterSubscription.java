package io.keyko.monitoring.agent.core.model;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractViewFilter;
import io.reactivex.disposables.Disposable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class ViewFilterSubscription {

    private ContractViewFilter filter;

    private Disposable subscription;

    private BigInteger startBlock;

    public ViewFilterSubscription(ContractViewFilter filter, Disposable subscription) {
        this.filter = filter;
        this.subscription = subscription;
    }

    public ViewFilterSubscription(ContractViewFilter filter)    {
        this.filter= filter;
    }
}
