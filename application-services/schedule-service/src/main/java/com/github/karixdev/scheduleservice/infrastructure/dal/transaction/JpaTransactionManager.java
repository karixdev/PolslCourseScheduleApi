package com.github.karixdev.scheduleservice.infrastructure.dal.transaction;

import com.github.karixdev.scheduleservice.application.dal.TransactionCallback;
import com.github.karixdev.scheduleservice.application.dal.TransactionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
public class JpaTransactionManager implements TransactionManager {

    private final TransactionTemplate transactionTemplate;

    @Override
    public void execute(TransactionCallback callback) {
        transactionTemplate.execute(status -> {
            callback.execute();
            return 1;
        });
    }

}
