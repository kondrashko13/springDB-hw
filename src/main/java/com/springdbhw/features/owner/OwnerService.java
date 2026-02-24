package com.springdbhw.features.owner;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OwnerService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public boolean verifyActiveTransactionState() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    // 8.2. Propagation Requires new -----------------------------------------------------------------------------------
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAutonomously(Owner owner) {
        em.persist(owner);
    }

    // 8.3. Propagation Mandatory --------------------------------------------------------------------------------------
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveInAnotherTransaction(Owner owner) {
        em.persist(owner);
    }
}
