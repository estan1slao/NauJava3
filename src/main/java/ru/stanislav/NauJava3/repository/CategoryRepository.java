package ru.stanislav.NauJava3.repository;

import org.springframework.data.repository.CrudRepository;
import ru.stanislav.NauJava3.entity.Category;

public interface CategoryRepository
        extends CrudRepository<Category, Long> {
}
