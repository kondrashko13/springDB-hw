package com.springdbhw.features.cat;

import com.springdbhw.features.owner.Owner;
import com.springdbhw.features.owner.OwnerRuntimeException;
import com.springdbhw.features.owner.OwnerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@AllArgsConstructor
public class CatService{

    @PersistenceContext
    private EntityManager em;

    private final OwnerService ownerService;

    @Transactional
    public boolean verifyActiveTransactionState() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    // 1. Checked Exception w/ Rollback --------------------------------------------------------------------------------
    @Transactional(rollbackFor = CatCheckedException.class)
    public void saveCatWithRollback(Cat cat) throws CatCheckedException {
        em.persist(cat);
        throw new CatCheckedException("Checked cat exception occurred.");
    }

    // 2. Runtime Exception w/o Rollback -------------------------------------------------------------------------------
    @Transactional(noRollbackFor = OwnerRuntimeException.class)
    public void saveCatWithoutRollback(Cat cat) {
        em.persist(cat);
        throw new OwnerRuntimeException("Unchecked owner exception occurred!");
    }

    // 3.1. Transaction inside doesn't work ----------------------------------------------------------------------------
    public boolean triggerInternalTransaction() {
        return verifyActiveTransactionState();
    }

    // 3.2. Transaction inside works -----------------------------------------------------------------------------------
    public boolean triggerExternalTransaction() {
        return ownerService.verifyActiveTransactionState();
    }

    // 4. Serializable conflict ----------------------------------------------------------------------------------------
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void renameOwner(Long ownerId, String newName) {
        Owner owner = em.find(Owner.class, ownerId);
        owner.setName(newName);
        em.flush();
    }

    // 8.1. Required propagation ---------------------------------------------------------------------------------------
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveTogether(Cat cat, Owner owner){
        em.persist(cat);
        ownerService.saveAutonomously(owner);
    }
}