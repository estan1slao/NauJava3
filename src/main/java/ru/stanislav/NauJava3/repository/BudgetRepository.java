package ru.stanislav.NauJava3.repository;

import org.springframework.data.repository.CrudRepository;
import ru.stanislav.NauJava3.entity.Budget;

public interface BudgetRepository
        extends CrudRepository<Budget, Long> {
}
