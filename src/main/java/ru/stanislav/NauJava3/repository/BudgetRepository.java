package ru.stanislav.NauJava3.repository;

import org.springframework.data.repository.CrudRepository;
import ru.stanislav.NauJava3.entity.Budget;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "budgets")
public interface BudgetRepository
        extends CrudRepository<Budget, Long> {
}
