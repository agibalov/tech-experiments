package me.loki2302.springrepositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PersonRepository extends PagingAndSortingRepository<Person, String> {
    List<Person> findByName(String name);
}
